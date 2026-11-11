package com.ensodai.avalon.data.repository.local

import com.ensodai.avalon.data.repository.local.base.BaseRepository
import com.ensodai.avalon.data.repository.local.base.DatabaseExecutor

import com.ensodai.avalon.data.db.AppDataBase
import com.ensodai.avalon.data.db.dao.ChatHistoryDao
import com.ensodai.avalon.data.db.dao.ChatSessionDao
import com.ensodai.avalon.data.db.dao.ChatVariantDao
import com.ensodai.avalon.data.db.dto.ChatVariant
import com.ensodai.avalon.data.db.dto.chat.CreateSlotDto
import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.avalon.data.db.models.chat.ChatSessionEntity
import com.ensodai.avalon.data.db.models.chat.ChatVariantEntity
import com.ensodai.avalon.data.db.models.chat.SlotWithContainers
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.avalon.data.db.models.message.MessageContainerEntity
import com.ensodai.avalon.data.db.models.message.MessageContentEntity
import com.ensodai.avalon.data.db.models.message.MessageSlotEntity
import com.ensodai.avalon.data.db.models.message.SlotInfo
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import com.ensodai.shared.avalon.chat.models.MessageContainerResponse
import com.ensodai.shared.avalon.chat.models.MessageContentResponse
import com.ensodai.shared.avalon.chat.models.MessageSlotResponse
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.shared.avalon.chat.models.MessageStatus
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.getUTCTimeNow
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

class LocalChatHistoryRepositoryImpl(
    dbExecutor: DatabaseExecutor,
    private val appDatabase: AppDataBase,
    private val chatHistoryDao: ChatHistoryDao,
    private val chatSessionDao: ChatSessionDao,
    private val chatVariantDao: ChatVariantDao,
    private val logger: AppLogger
) : BaseRepository(dbExecutor), LocalChatHistoryRepository {

    override suspend fun saveNewMessage(dto: CreateSlotDto) {
        dbWrite {
            val existingSlot = chatHistoryDao.getSlotsByIds(listOf(dto.slotId)).firstOrNull()
            val baseCreatedAt = existingSlot?.createdAt ?: dto.createdAt
            val actualCreatedAt: Instant
            if (existingSlot == null) {
                val lastMessageTime = chatHistoryDao.getLastMessageTime(dto.variantId)
                actualCreatedAt = if (lastMessageTime != null && baseCreatedAt <= lastMessageTime) {
                    lastMessageTime.plus(1.milliseconds)
                } else {
                    baseCreatedAt
                }
            } else {
                actualCreatedAt = baseCreatedAt
            }

            ensureSessionAndVariantExist(
                sessionId = dto.sessionId,
                variantId = dto.variantId,
                characterId = dto.characterId,
                createdAt = actualCreatedAt
            )

            val slot = MessageSlotEntity(
                id = dto.slotId,
                chatVariantId = dto.variantId,
                activeContainerId = dto.activeContainerId,
                createdAt = actualCreatedAt,
                updatedAt = if (dto.updatedAt < actualCreatedAt) actualCreatedAt else dto.updatedAt,
                syncAction = dto.syncAction
            )

            val existingContainers = chatHistoryDao.getContainersByIds(dto.containers.map { it.id })
                .associateBy { it.id }

            val containers = dto.containers.map { containerDto ->
                val containerCreatedAt =
                    existingContainers[containerDto.id]?.createdAt ?: actualCreatedAt
                val containerUpdatedAt =
                    if (containerDto.updatedAt < containerCreatedAt) containerCreatedAt else containerDto.updatedAt
                MessageContainerEntity(
                    id = containerDto.id,
                    slotId = dto.slotId,
                    role = containerDto.role,
                    rarity = containerDto.rarity,
                    status = containerDto.status,
                    syncAction = containerDto.syncAction,
                    userReaction = containerDto.userReaction,
                    characterReaction = containerDto.characterReaction,
                    isRead = (containerDto.role == MessageRole.USER || containerDto.role == MessageRole.SYSTEM),
                    createdAt = containerCreatedAt,
                    updatedAt = containerUpdatedAt
                )
            }

            val contents = dto.containers.flatMap { containerDto ->
                containerDto.contents.map { contentDto ->
                    MessageContentEntity(
                        id = contentDto.id,
                        containerId = containerDto.id,
                        type = contentDto.type,
                        content = contentDto.content,
                        orderIndex = contentDto.orderIndex
                    )
                }
            }

            chatHistoryDao.saveChatHistoryFlat(
                slots = listOf(slot),
                containers = containers,
                contents = contents
            )

            val hasModelMessage = dto.containers.any { it.role == MessageRole.MODEL }
            if (hasModelMessage) {
                chatVariantDao.incrementUnreadCount(dto.variantId, 1)
            }

            // Session preview update usually happens here if it's the latest message
            val activeContainerId = dto.activeContainerId
            if (activeContainerId != null) {
                val activeContainerDto = dto.containers.find { it.id == activeContainerId }
                if (activeContainerDto != null) {
                    val firstContent = activeContainerDto.contents.minByOrNull { it.orderIndex }
                    if (firstContent != null) {
                        chatSessionDao.updateSessionPreview(
                            sessionId = dto.sessionId,
                            content = firstContent.content.take(200),
                            time = actualCreatedAt,
                            type = firstContent.type,
                            role = activeContainerDto.role
                        )
                    }
                }
            }
        }
    }

    private suspend fun ensureSessionAndVariantExist(
        sessionId: Uuid,
        variantId: Uuid,
        characterId: Uuid,
        createdAt: Instant
    ) {

        if (chatVariantDao.exists(variantId)) return

        val sessionExists = chatSessionDao.exists(sessionId)

        if (!sessionExists) {

            val newSession = ChatSessionEntity(
                id = sessionId,
                characterId = characterId,
                createdAt = createdAt,
                updatedAt = createdAt,
                isArchived = false,
                syncAction = SyncAction.NONE,
                activeChatVariantId = null,
                lastMessageContent = null,
                lastMessageType = null,
                lastMessageRole = null,
                lastMessageTime = null
            )
            chatSessionDao.upsertChatSessions(listOf(newSession))
            logger.d(
                "LocalChatHistoryRepositoryImpl",
                "🆕 Lazy created missing SESSION (temp null variant): $sessionId"
            )
        }

        val newVariant = ChatVariantEntity(
            id = variantId,
            syncAction = SyncAction.NONE,
            chatSessionId = sessionId,
            name = "New Chat",
            createdAt = createdAt,
            updatedAt = createdAt
        )
        chatVariantDao.upsertChatVariant(newVariant)
        logger.d("LocalChatHistoryRepositoryImpl", "🆕 Lazy created missing VARIANT: $variantId")

        chatVariantDao.updateActiveSessionVariant(
            sessionId = sessionId,
            variantId = variantId,
            updatedAt = createdAt
        )
    }

    override suspend fun addMessageContent(
        containerId: Uuid,
        contentId: Uuid,
        type: AvalonMessageContentType,
        content: String,
        orderIndex: Int
    ) {
        dbWrite {
            val contentEntity = MessageContentEntity(
                id = contentId,
                containerId = containerId,
                type = type,
                content = content,
                orderIndex = orderIndex
            )
            chatHistoryDao.insertContents(listOf(contentEntity))
        }
    }

    override suspend fun addNewContainerForSlot(
        container: MessageContainerResponse,
        sessionId: Uuid
    ) {
        dbWrite {
            val containerEntity = MessageContainerEntity(
                id = container.containerId,
                slotId = container.slotId,
                role = container.role,
                rarity = container.rarity,
                status = container.status,
                syncAction = SyncAction.NONE,
                userReaction = container.userReaction,
                characterReaction = container.characterReaction,
                isRead = container.isRead,
                showOutreachFeedback = container.showOutreachFeedback,
                createdAt = container.createdAt,
                updatedAt = container.updatedAt
            )

            val contentEntities = container.contents.map { contentDto ->
                MessageContentEntity(
                    id = contentDto.id,
                    containerId = container.containerId,
                    type = contentDto.type,
                    content = contentDto.content,
                    orderIndex = contentDto.orderIndex
                )
            }

            val firstContent = container.contents.minByOrNull { it.orderIndex }
            val previewContent = firstContent?.content ?: ""
            val previewType = firstContent?.type ?: AvalonMessageContentType.TEXT

            chatHistoryDao.handleNewContainerEvent(
                sessionId = sessionId,
                container = containerEntity,
                contents = contentEntities,
                previewContent = previewContent,
                previewType = previewType
            )
        }
    }

    override suspend fun saveChatHistoryResponse(
        variantId: Uuid,
        slots: List<MessageSlotResponse>
    ) {
        dbWrite {
            saveChatHistoryResponseInternal(variantId, slots)
        }
    }

    private suspend fun saveChatHistoryResponseInternal(
        variantId: Uuid,
        slots: List<MessageSlotResponse>
    ) {
        // 1. Подготовка сущностей (Mapping DTO -> Entity)
        val incomingSlots = ArrayList<MessageSlotEntity>(slots.size)
        val incomingContainers = ArrayList<MessageContainerEntity>()
        val incomingContents = ArrayList<MessageContentEntity>()

        for (slotDto in slots) {
            incomingSlots.add(slotDto.toEntity(variantId))
            for (containerDto in slotDto.containers) {
                incomingContainers.add(containerDto.toEntity())
                for (contentDto in containerDto.contents) {
                    incomingContents.add(contentDto.toEntity(containerDto.containerId))
                }
            }
        }

        // 2. Загружаем локальное состояние для сравнения
        val incomingSlotIds = incomingSlots.map { it.id }
        val incomingContainerIds = incomingContainers.map { it.id }

        val localSlotsMap = chatHistoryDao.getSlotsByIds(incomingSlotIds).associateBy { it.id }
        val localContainersMap =
            chatHistoryDao.getContainersByIds(incomingContainerIds).associateBy { it.id }

        // === ЭТАП 1: ФИЛЬТРАЦИЯ СЛОТОВ ===
        val slotsToInsert = ArrayList<MessageSlotEntity>()
        val safeSlotIds = HashSet<Uuid>()

        for (incomingEntity in incomingSlots) {
            val localSlot = localSlotsMap[incomingEntity.id]

            val shouldInsertSlot = if (localSlot == null) {
                true
            } else {
                when (localSlot.syncAction) {
                    SyncAction.DELETE -> {
                        logger.d(
                            "LocalChatHistoryRepositoryImpl",
                            "🛡️ Slot Ignored (Local DELETE): ${incomingEntity.id}"
                        )
                        false
                    }

                    SyncAction.CREATE -> true
                    SyncAction.UPDATE -> localSlot.updatedAt < incomingEntity.updatedAt
                    SyncAction.NONE -> true
                }
            }

            if (shouldInsertSlot) {
                slotsToInsert.add(incomingEntity)
                safeSlotIds.add(incomingEntity.id)
            }
        }

        if (slotsToInsert.isNotEmpty()) {
            chatHistoryDao.insertSlots(slotsToInsert)
        }

        // === ЭТАП 2: ФИЛЬТРАЦИЯ КОНТЕЙНЕРОВ ===
        val containersToInsert = ArrayList<MessageContainerEntity>()
        val safeContainerIds = HashSet<Uuid>()

        for (incoming in incomingContainers) {
            if (!safeSlotIds.contains(incoming.slotId)) {
                continue
            }

            val local = localContainersMap[incoming.id]

            val shouldOverwrite = if (local == null) {
                true
            } else {
                when (local.syncAction) {
                    SyncAction.DELETE -> false
                    SyncAction.CREATE -> true
                    SyncAction.UPDATE -> {
                        logger.d(
                            "LocalChatHistoryRepositoryImpl",
                            "🛡️ Container Ignored (Local EDIT): ${incoming.id}"
                        )
                        false
                    }

                    SyncAction.NONE -> incoming.updatedAt >= local.updatedAt
                }
            }

            if (shouldOverwrite) {
                containersToInsert.add(incoming)
                safeContainerIds.add(incoming.id)
            }
        }

        // === ЭТАП 3: ВСТАВКА КОНТЕЙНЕРОВ И КОНТЕНТА ===
        if (containersToInsert.isNotEmpty()) {
            val contentsToInsert =
                incomingContents.filter { safeContainerIds.contains(it.containerId) }
            chatHistoryDao.insertContainers(containersToInsert)
            chatHistoryDao.insertContents(contentsToInsert)
            logger.d(
                "LocalChatHistoryRepositoryImpl",
                "✅ Merged ${containersToInsert.size} containers from REST"
            )
        }
    }

    private fun MessageSlotResponse.toEntity(variantId: Uuid): MessageSlotEntity {
        return MessageSlotEntity(
            id = this.id,
            chatVariantId = variantId,
            activeContainerId = this.activeContainerId,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            syncAction = SyncAction.NONE
        )
    }

    private fun MessageContainerResponse.toEntity(): MessageContainerEntity {
        return MessageContainerEntity(
            id = this.containerId,
            slotId = this.slotId,
            role = this.role,
            rarity = this.rarity,
            status = this.status,
            userReaction = this.userReaction,
            characterReaction = this.characterReaction,
            isRead = this.isRead,
            showOutreachFeedback = this.showOutreachFeedback,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            syncAction = SyncAction.NONE
        )
    }

    private fun MessageContentResponse.toEntity(parentContainerId: Uuid): MessageContentEntity {
        return MessageContentEntity(
            id = this.id,
            containerId = parentContainerId,
            type = this.type,
            content = this.content,
            orderIndex = this.orderIndex
        )
    }


    override fun getChatHistoryPagingSource(chatVariantId: Uuid) {
        TODO("Not yet implemented")
    }

    override fun getChatHistoryFlow(
        variantId: Uuid,
        limit: Int
    ): Flow<List<SlotWithContainers>> {
        return cachedObserve("getChatHistoryFlow_${variantId}_$limit") { chatHistoryDao.getChatHistoryFlow(variantId, limit) }
    }

    override fun getSharedMediaFlow(
        characterId: Uuid
    ): Flow<List<String>> {
        return cachedObserve("getSharedMediaFlow_$characterId") { chatHistoryDao.getSharedMediaFlow(characterId) }
    }

    override suspend fun deleteMessagesByIds(ids: List<Uuid>) {
        TODO("Not yet implemented")
    }

    override suspend fun updateMessageTextContentByContainerId(
        messageContainerId: Uuid,
        newMessage: String,
        syncAction: SyncAction,
        updatedAt: Instant?
    ) {
        dbWrite {
            chatHistoryDao.updateLocalMessageTextAndSyncState(
                containerId = messageContainerId,
                content = newMessage,
                syncAction = syncAction,
                updatedAt = updatedAt
            )
        }
    }

    override suspend fun updateMessageSystemInstructionContentByContainerId(
        messageContainerId: Uuid,
        newSystemInstruction: String,
        syncAction: SyncAction,
        updatedAt: Instant?
    ) {
        dbWrite {
            chatHistoryDao.updateContentByType(
                containerId = messageContainerId,
                content = newSystemInstruction,
                type = AvalonMessageContentType.MESSAGE_SYSTEM_INSTRUCTION
            )
            chatHistoryDao.updateContainerState(
                containerId = messageContainerId,
                syncAction = syncAction,
                updatedAt = updatedAt
            )
        }
    }

    override suspend fun processMessageUpdateEvent(event: ServerMessage.MessageUpdated) {
        dbWrite {
            chatHistoryDao.handleMessageUpdateEvent(
                containerId = event.containerId,
                content = event.newText,
                systemInstruction = event.newMessageSystemInstruction,
                syncAction = SyncAction.NONE,
                updatedAt = event.updatedAt,
                sessionId = event.sessionId,
                isLastMessage = event.isLastMessage
            )
        }
    }

    override suspend fun deleteAllChatMessagesByChatSessionId(chatSessionId: Uuid) {
        TODO("Not yet implemented")
    }

    override suspend fun searchMessages(
        query: String,
        chatVariantId: Uuid
    ): List<Uuid> {
        TODO("Not yet implemented")
    }

    override suspend fun getLastTwoMessagesFlow(chatVariantId: Uuid) {
        TODO("Not yet implemented")
    }

    override suspend fun getLastMessagesFlow(chatVariantId: Uuid) {
        TODO("Not yet implemented")
    }

    override suspend fun getLastSlotWithMessagesFlow(chatVariantId: Uuid) {
        TODO("Not yet implemented")
    }

    override suspend fun getChatVariants(sessionId: Uuid?): Flow<List<ChatVariant>> {
        return cachedObserve("getChatVariants_$sessionId") { chatVariantDao.getChatVariants(sessionId) }
    }

    override suspend fun getChatVariantsCountFlow(sessionId: Uuid): Flow<Int> {
        TODO("Not yet implemented")
    }

    override suspend fun updateActiveMessageInSlot(
        messageSlotId: Uuid,
        newActiveContainerId: Uuid,
        updatedAt: Instant
    ) {
        dbWrite {
            val currentSlotUpdatedAt = chatHistoryDao.getSlotUpdatedAt(messageSlotId)

            val safeUpdatedAt = if (currentSlotUpdatedAt != null && updatedAt <= currentSlotUpdatedAt) {
                currentSlotUpdatedAt.plus(1.milliseconds)
            } else {
                updatedAt
            }
            val preview = chatHistoryDao.getContainerContentPreview(newActiveContainerId)

            val content = preview?.content ?: "Message"
            val contentType = preview?.type ?: AvalonMessageContentType.TEXT
            chatHistoryDao.switchActiveMessageAndRefreshSession(
                slotId = messageSlotId,
                newActiveMessageId = newActiveContainerId,
                content = content,
                contentType = contentType,
                safeUpdatedAt = safeUpdatedAt
            )
        }
    }

    override suspend fun softDeleteSlotWithMessagesBySlotIds(slotIds: List<Uuid>) {
        dbWrite { chatHistoryDao.markSlotsAsDeleted(slotIds) }
    }

    override suspend fun rpcHardDeleteSlotWithMessagesBySlotIds(event: ServerMessage.MessagesDeleted) {
        dbWrite {
            chatHistoryDao.handleMessageDeletionEvent(
                slotIds = event.slotIds,
                sessionId = event.sessionId,
                previewContent = event.previewContent,
                previewType = event.previewType,
                previewRole = event.previewRole,
                previewTime = event.previewTime,
                eventTime = event.deletedAt
            )
        }
    }

    override suspend fun hardDeleteSlotWithMessagesBySlotIds(slotIds: List<Uuid>) {
        dbWrite { chatHistoryDao.hardDeleteSlotsPermanently(slotIds) }
    }

    override suspend fun clearChatVariant(chatVariantServerId: Uuid) {
        TODO("Not yet implemented")
    }

    override suspend fun confirmMessageSent(
        containerId: Uuid,
        serverTimestamp: Instant,
        slotId: Uuid
    ) {
        dbWrite {
            chatHistoryDao.markMessageAsSent(
                containerId = containerId,
                serverTimestamp = serverTimestamp,
                status = MessageStatus.SENT,
                syncAction = SyncAction.NONE,
                slotId = slotId
            )
        }
    }

    override suspend fun confirmMessageSentError(
        containerId: Uuid,
        serverTimestamp: Instant,
        slotId: Uuid
    ) {
        dbWrite {
            val status = MessageStatus.FAILED(reason = "Network Timeout")
            chatHistoryDao.markMessageAsError(
                containerId = containerId,
                slotId = slotId,
                status = status,
                syncAction = SyncAction.NONE,
                serverTimestamp = serverTimestamp
            )
        }
    }

    override suspend fun clearHistory(variantId: Uuid) {
        dbWrite { chatHistoryDao.clearHistory(variantId) }
    }

    override suspend fun getNewestSlot(variantId: Uuid): MessageSlotEntity? {
        return dbRead { chatHistoryDao.getNewestSlot(variantId) }
    }

    override suspend fun getLastMessageTime(variantId: Uuid): Instant? {
        return dbRead { chatHistoryDao.getLastMessageTime(variantId) }
    }

    override suspend fun getLastSlotInfoFlow(variantId: Uuid): Flow<SlotInfo?> {
        return cachedObserve("getLastSlotInfoFlow_$variantId") { chatHistoryDao.getLastSlotInfoFlow(variantId) }
    }

    override suspend fun updateActiveMessageFromRPCEvent(
        event: ServerMessage.ActiveContainerChanged
    ) {
        dbWrite {
            chatHistoryDao.processActiveContainerEvent(
                slotId = event.slotId,
                sessionId = event.sessionId,
                newContainerId = event.newContainerId,
                content = event.content,
                contentType = event.contentType,
                role = event.role,
                serverUpdatedAt = event.updatedAt,
                syncAction = SyncAction.NONE
            )
        }
    }

    override suspend fun variantHasUnsyncedMessages(variantId: Uuid): Boolean {
        return dbRead { chatHistoryDao.variantHasUnsyncedMessages(variantId) }
    }

    override suspend fun isVariantExists(variantId: Uuid): Boolean {
        return dbRead { chatVariantDao.isVariantExists(variantId) }
    }

    override suspend fun getSlotsByIds(ids: List<Uuid>): List<MessageSlotEntity> {
        return dbRead { chatHistoryDao.getSlotsByIds(ids) }
    }

    override suspend fun updateSlotsSyncAction(ids: List<Uuid>, action: SyncAction) {
        dbWrite { chatHistoryDao.markSlotsAsDeleted(ids, action) }
    }

    override suspend fun toggleMessageUserReaction(
        messageContainerId: Uuid,
        reaction: String?
    ): String? {
        return dbWrite {
            val currentReaction = chatHistoryDao.getContainersByIds(listOf(messageContainerId))
                .firstOrNull()?.userReaction

            val finalReaction = if (currentReaction == reaction) null else reaction

            chatHistoryDao.updateMessageUserReaction(
                containerId = messageContainerId,
                reaction = finalReaction,
                updatedAt = getUTCTimeNow()
            )

            finalReaction
        }
    }

    override suspend fun toggleMessageCharacterReaction(
        messageContainerId: Uuid,
        reaction: String?
    ): String? {
        return dbWrite {
            val currentReaction = chatHistoryDao.getContainersByIds(listOf(messageContainerId))
                .firstOrNull()?.characterReaction

            val finalReaction = if (currentReaction == reaction) null else reaction

            chatHistoryDao.updateMessageCharacterReaction(
                containerId = messageContainerId,
                reaction = finalReaction,
                updatedAt = getUTCTimeNow()
            )

            finalReaction
        }
    }

    override suspend fun updateMessageUserReaction(
        messageContainerId: Uuid,
        reaction: String?
    ) {
        dbWrite {
            chatHistoryDao.updateMessageUserReaction(
                containerId = messageContainerId,
                reaction = reaction,
                updatedAt = getUTCTimeNow()
            )
        }
    }

    override suspend fun updateMessageCharacterReaction(
        messageContainerId: Uuid,
        reaction: String?
    ) {
        dbWrite {
            chatHistoryDao.updateMessageCharacterReaction(
                containerId = messageContainerId,
                reaction = reaction,
                updatedAt = getUTCTimeNow()
            )
        }
    }

    override suspend fun markMessagesAsReadLocally(containerIds: List<Uuid>) {
        if (containerIds.isEmpty()) return
        dbWrite { chatHistoryDao.markMessagesAsReadWithUnreadCountUpdate(containerIds) }
    }

    override suspend fun hideOutreachFeedbackForContainer(containerId: Uuid) {
        dbWrite { chatHistoryDao.hideOutreachFeedbackForContainer(containerId) }
    }

    override suspend fun getLastServerUpdateTime(variantId: Uuid): Instant? {
        return dbRead {
            chatHistoryDao.getLastServerUpdateTime(
                variantId = variantId
            )
        }
    }

    override suspend fun getOldestSlot(variantId: Uuid): MessageSlotEntity? {
        return dbRead { chatHistoryDao.getOldestSlot(variantId) }
    }

    override suspend fun getSlotsCount(variantId: Uuid): Int {
        return dbRead { chatHistoryDao.getSlotsCount(variantId) }
    }

    override suspend fun clearAndSaveChatHistory(
        variantId: Uuid,
        upsertedMessages: List<MessageSlotResponse>
    ) {
        dbWrite {
            chatHistoryDao.clearHistory(variantId)
            saveChatHistoryResponseInternal(variantId, upsertedMessages)
        }
    }

    override suspend fun getUnreadModelContainerIds(variantId: Uuid): List<Uuid> {
        return dbRead { chatHistoryDao.getUnreadModelContainerIds(variantId) }
    }
}
