package app.data.repository

import app.data.db.tables.aiCharacter.AICharacters
import app.data.db.tables.aiCharacter.AICharacterAlternateGreetings
import app.data.db.tables.chatSessions.*
import app.data.db.tables.aiCharacter.OutreachHistoryTable
import app.data.db.tables.aiCharacter.FeedbackStatus
import app.data.db.tables.gifts.Gifts
import app.data.db.tables.users.AppUsers
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageStatus
import com.ensodai.shared.avalon.chat.models.MessageContainerResponse
import com.ensodai.shared.avalon.chat.models.MessageContentResponse
import com.ensodai.shared.avalon.chat.models.MessageSlotResponse
import app.data.models.PreviousGiftInfo
import app.data.models.SaveAiResponseResult
import com.ensodai.shared.avalon.chat.services.MessageContentCodec
import com.ensodai.shared.avalon.chat.services.processImageContentUrl
import com.ensodai.shared.avalon.chat.models.SystemEventType
import com.ensodai.shared.avalon.chat.models.StickerGiftPayload
import app.data.db.tables.chat.*
import app.domain.models.DomainMessage
import app.domain.models.DomainMessageContent
import app.domain.models.MessageHistoryResponse
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import app.utils.dbQuery
import app.utils.getUTCTimeNow
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import app.service.lorebook.model.EntryTimedState
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single
import app.data.db.tables.users.CreatorAffinityTable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Single
class ChatRepository {

    suspend fun verifySessionOwnership(sessionId: Uuid, userId: Uuid): Boolean {
        return dbQuery {
            ChatSessions.select(ChatSessions.id)
                .where { (ChatSessions.id eq sessionId) and (ChatSessions.userId eq userId) }
                .count() > 0
        }
    }

    suspend fun findVariantByIdAndUser(variantId: Uuid, userId: Uuid): ChatVariantInternalDto? {
        return dbQuery {
            ChatVariants.innerJoin(ChatSessions)
                .select(
                    ChatVariants.id,
                    ChatVariants.chatSessionId,
                    ChatVariants.name,
                    ChatVariants.unreadCount,
                    ChatVariants.createdAt,
                    ChatVariants.updatedAt
                )
                .where {
                    (ChatVariants.id eq variantId) and (ChatSessions.userId eq userId)
                }
                .singleOrNull()
                ?.let { row ->
                    ChatVariantInternalDto(
                        id = row[ChatVariants.id].value,
                        chatSessionId = row[ChatVariants.chatSessionId].value,
                        name = row[ChatVariants.name],
                        unreadCount = row[ChatVariants.unreadCount],
                        createdAt = row[ChatVariants.createdAt],
                        updatedAt = row[ChatVariants.updatedAt]
                    )
                }
        }
    }

    suspend fun verifyVariantOwnership(variantId: Uuid, userId: Uuid): Boolean {
        return dbQuery {
            ChatVariants.innerJoin(ChatSessions)
                .select(ChatVariants.id)
                .where { (ChatVariants.id eq variantId) and (ChatSessions.userId eq userId) }
                .count() > 0
        }
    }

    suspend fun deleteVariant(variantId: Uuid) {
        dbQuery {
            ChatVariants.update({ ChatVariants.id eq variantId }) {
                it[deletedAt] = getUTCTimeNow()
            }
        }
    }

    suspend fun createNewMessageTurn(
        messageRole: MessageRole,
        messageRarity: Rarity,
        slotId: Uuid,
        contentId: Uuid,
        containerId: Uuid,
        chatVariantId: Uuid,
        chatSessionId: Uuid,
        contentType: AvalonMessageContentType,
        text: String,
        messageSystemInstruction: String?,
        visualDescription: String? = null
    ): MessageSlotResponse {
        return dbQuery {
            val now = getUTCTimeNow()
            val systemInstructionId = if (messageSystemInstruction != null) Uuid.random() else null
            val visualDescriptionId = if (visualDescription != null) Uuid.random() else null

            MessageSlots.insert {
                it[id] = slotId
                it[this.chatVariantId] = chatVariantId
                it[activeMessageId] = null
                it[createdAt] = now
                it[updatedAt] = now
            }

            MessageContainers.insert {
                it[id] = containerId
                it[this.slotId] = slotId
                it[role] = messageRole
                it[rarity] = messageRarity
                it[createdAt] = now
                it[updatedAt] = now
            }

            MessageContents.insert {
                it[id] = contentId
                it[this.containerId] = containerId
                it[type] = contentType
                it[content] = text
                it[orderIndex] = 0
                it[createdAt] = now
                it[updatedAt] = now
            }

            var nextOrderIndex = 1
            if (visualDescription != null && visualDescriptionId != null) {
                MessageContents.insert {
                    it[id] = visualDescriptionId
                    it[this.containerId] = containerId
                    it[type] = AvalonMessageContentType.GENERATED_IMAGE_DESCRIPTION
                    it[content] = visualDescription
                    it[orderIndex] = nextOrderIndex++
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }

            if (messageSystemInstruction != null && systemInstructionId != null) {
                MessageContents.insert {
                    it[id] = systemInstructionId
                    it[this.containerId] = containerId
                    it[type] = AvalonMessageContentType.MESSAGE_SYSTEM_INSTRUCTION
                    it[content] = messageSystemInstruction
                    it[orderIndex] = nextOrderIndex++
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }

            MessageSlots.update({ MessageSlots.id eq slotId }) {
                it[activeMessageId] = containerId
            }

            ChatVariants.update({ ChatVariants.id eq chatVariantId }) {
                it[updatedAt] = now
                if (messageRole == MessageRole.MODEL) {
                    it[unreadCount] = ChatVariants.unreadCount + 1
                } else {
                    it[unreadCount] = 0
                }
            }

            ChatSessions.update({ ChatSessions.id eq chatSessionId }) {
                it[updatedAt] = now
                it[lastMessageRole] = messageRole
                it[lastMessageId] = containerId
                it[lastMessageAt] = now
                it[lastMessageType] = contentType
                it[lastMessagePreview] = visualDescription?.take(200) ?: text.take(200)
            }

            val finalContent = if (contentType == AvalonMessageContentType.IMAGE) {
                MessageContentCodec.processImageContentUrl(text)
            } else text

            val contents = mutableListOf(
                MessageContentResponse(
                    id = contentId,
                    type = contentType,
                    content = finalContent,
                    orderIndex = 0
                )
            )

            if (messageSystemInstruction != null && systemInstructionId != null) {
                contents.add(
                    MessageContentResponse(
                        id = systemInstructionId,
                        type = AvalonMessageContentType.MESSAGE_SYSTEM_INSTRUCTION,
                        content = messageSystemInstruction,
                        orderIndex = contents.size
                    )
                )
            }

            val containerResponse = MessageContainerResponse(
                containerId = containerId,
                slotId = slotId,
                role = messageRole,
                rarity = messageRarity,
                status = MessageStatus.SENT,
                userReaction = null,
                characterReaction = null,
                isRead = (messageRole == MessageRole.USER || messageRole == MessageRole.SYSTEM),
                createdAt = now,
                updatedAt = now,
                contents = contents
            )

            if (messageRole == MessageRole.USER) {
                incrementCreatorAffinity(chatSessionId)
            }

            MessageSlotResponse(
                id = slotId,
                chatVariantId = chatVariantId,
                chatSessionId = chatSessionId,
                activeContainerId = containerId,
                createdAt = now,
                updatedAt = now,
                containers = listOf(containerResponse)
            )
        }
    }

    suspend fun createSystemMessage(
        userId: Uuid,
        chatSessionId: Uuid,
        chatVariantId: Uuid,
        eventType: SystemEventType
    ): MessageSlotResponse {
        return dbQuery {
            val now = getUTCTimeNow()
            val newSlotId = Uuid.random()
            val newContainerId = Uuid.random()
            val newContentId = Uuid.random()

            MessageSlots.insert {
                it[id] = newSlotId
                it[this.chatVariantId] = chatVariantId
                it[activeMessageId] = null
                it[createdAt] = now
                it[updatedAt] = now
            }

            MessageContainers.insert {
                it[id] = newContainerId
                it[slotId] = newSlotId
                it[role] = MessageRole.SYSTEM
                it[rarity] = Rarity.COMMON
                it[createdAt] = now
                it[updatedAt] = now
            }

            MessageContents.insert {
                it[id] = newContentId
                it[containerId] = newContainerId
                it[type] = AvalonMessageContentType.SYSTEM_EVENT
                it[content] = eventType.name
                it[orderIndex] = 0
                it[createdAt] = now
                it[updatedAt] = now
            }

            MessageSlots.update({ MessageSlots.id eq newSlotId }) {
                it[activeMessageId] = newContainerId
            }

            ChatVariants.update({ ChatVariants.id eq chatVariantId }) {
                it[updatedAt] = now
            }

            ChatSessions.update({ ChatSessions.id eq chatSessionId }) {
                it[updatedAt] = now
                it[lastMessageRole] = MessageRole.SYSTEM
                it[lastMessageId] = newContainerId
                it[lastMessageAt] = now
                it[lastMessageType] = AvalonMessageContentType.SYSTEM_EVENT
                it[lastMessagePreview] = eventType.name
            }

            val contentResponse = MessageContentResponse(
                id = newContentId,
                type = AvalonMessageContentType.SYSTEM_EVENT,
                content = eventType.name,
                orderIndex = 0
            )

            val containerResponse = MessageContainerResponse(
                containerId = newContainerId,
                slotId = newSlotId,
                role = MessageRole.SYSTEM,
                rarity = Rarity.COMMON,
                status = MessageStatus.SENT,
                userReaction = null,
                characterReaction = null,
                isRead = true,
                createdAt = now,
                updatedAt = now,
                contents = listOf(contentResponse)
            )

            MessageSlotResponse(
                id = newSlotId,
                chatVariantId = chatVariantId,
                chatSessionId = chatSessionId,
                activeContainerId = newContainerId,
                createdAt = now,
                updatedAt = now,
                containers = listOf(containerResponse)
            )
        }
    }

    suspend fun addMessageToSlot(
        slotId: Uuid,
        role: MessageRole,
        text: String,
        rarity: Rarity
    ): MessageContainerInternalDto {
        return dbQuery {
            val existingSlot = MessageSlot.findById(slotId)
                ?: throw IllegalStateException("MessageSlot with id $slotId not found.")

            val newMessageContainer = MessageContainer.new {
                this.slot = existingSlot
                this.role = role
                this.rarity = rarity
            }

            MessageContent.new {
                this.messageContainer = newMessageContainer
                this.type = AvalonMessageContentType.TEXT
                this.content = text
            }

            val now = getUTCTimeNow()
            existingSlot.updatedAt = now
            existingSlot.chatVariant.updatedAt = now
            existingSlot.chatVariant.chatSession.updatedAt = now

            newMessageContainer.toInternalDto()
        }
    }

    suspend fun getHistoryForVariant(variantId: Uuid, limit: Int): MessageHistoryResponse {
        return dbQuery {
            val filterCondition = (MessageSlots.chatVariantId eq variantId) and
                    (MessageSlots.deletedAt.isNull()) and
                    (MessageSlots.isArchived eq false) and
                    (MessageContainers.role inList listOf(MessageRole.USER, MessageRole.MODEL))

            val joinQuery = MessageSlots.join(
                otherTable = MessageContainers,
                joinType = JoinType.INNER,
                onColumn = MessageSlots.activeMessageId,
                otherColumn = MessageContainers.id
            )

            val totalCount = joinQuery.select(MessageSlots.id).where { filterCondition }.count()
            if (totalCount == 0L) return@dbQuery MessageHistoryResponse(emptyList(), 0)

            val headerRows = joinQuery
                .select(
                    MessageContainers.id,
                    MessageContainers.role,
                    MessageContainers.userReaction,
                    MessageContainers.characterReaction
                )
                .where { filterCondition }
                .orderBy(MessageSlots.createdAt, SortOrder.DESC)
                .limit(limit)
                .toList()

            val containerIds = headerRows.map { it[MessageContainers.id] }
            val contentRows = MessageContents
                .select(MessageContents.containerId, MessageContents.type, MessageContents.content)
                .where { MessageContents.containerId inList containerIds }
                .orderBy(MessageContents.orderIndex, SortOrder.ASC)
                .toList()

            val contentsMap = contentRows.groupBy { it[MessageContents.containerId] }

            val messages = headerRows.asReversed().map { row ->
                val containerId = row[MessageContainers.id]
                val messageContents = contentsMap[containerId]?.map { contentRow ->
                    DomainMessageContent(
                        type = contentRow[MessageContents.type],
                        content = contentRow[MessageContents.content]
                    )
                } ?: emptyList()

                DomainMessage(
                    id = containerId.value,
                    role = row[MessageContainers.role],
                    contents = messageContents,
                    userReaction = row[MessageContainers.userReaction],
                    characterReaction = row[MessageContainers.characterReaction]
                )
            }

            MessageHistoryResponse(messages = messages, totalCount = totalCount)
        }
    }

    suspend fun getActiveSummary(variantId: Uuid): String? = dbQuery {
        ChatSummaries
            .select(ChatSummaries.content)
            .where { ChatSummaries.chatVariantId eq variantId }
            .orderBy(ChatSummaries.createdAt, SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?.get(ChatSummaries.content)
    }

    suspend fun getHistoryBeforeSlot(
        variantId: Uuid,
        targetSlotId: Uuid,
        limit: Int
    ): MessageHistoryResponse {
        return dbQuery {
            val targetSlotCreatedAt = MessageSlots
                .select(MessageSlots.createdAt)
                .where { MessageSlots.id eq targetSlotId }
                .singleOrNull()
                ?.get(MessageSlots.createdAt)
                ?: return@dbQuery MessageHistoryResponse(emptyList(), 0)

            val filterCondition = (MessageSlots.chatVariantId eq variantId) and
                    (MessageContainers.role inList listOf(MessageRole.USER, MessageRole.MODEL)) and
                    (MessageSlots.createdAt less targetSlotCreatedAt)

            val joinQuery = MessageSlots.join(
                otherTable = MessageContainers,
                joinType = JoinType.INNER,
                onColumn = MessageSlots.activeMessageId,
                otherColumn = MessageContainers.id
            )

            val totalCount = joinQuery.select(MessageSlots.id).where { filterCondition }.count()
            if (totalCount == 0L) return@dbQuery MessageHistoryResponse(emptyList(), 0)

            val headerRows = joinQuery
                .select(
                    MessageContainers.id,
                    MessageContainers.role,
                    MessageContainers.userReaction,
                    MessageContainers.characterReaction
                )
                .where { filterCondition }
                .orderBy(MessageSlots.createdAt, SortOrder.DESC)
                .limit(limit)
                .toList()

            val containerIds = headerRows.map { it[MessageContainers.id] }
            val contentRows = MessageContents
                .select(MessageContents.containerId, MessageContents.type, MessageContents.content)
                .where { MessageContents.containerId inList containerIds }
                .orderBy(MessageContents.orderIndex, SortOrder.ASC)
                .toList()

            val contentsMap = contentRows.groupBy { it[MessageContents.containerId] }

            val messages = headerRows.asReversed().map { row ->
                val containerId = row[MessageContainers.id]
                val messageContents = contentsMap[containerId]?.map { contentRow ->
                    DomainMessageContent(
                        type = contentRow[MessageContents.type],
                        content = contentRow[MessageContents.content]
                    )
                } ?: emptyList()

                DomainMessage(
                    id = containerId.value,
                    role = row[MessageContainers.role],
                    contents = messageContents,
                    userReaction = row[MessageContainers.userReaction],
                    characterReaction = row[MessageContainers.characterReaction]
                )
            }

            MessageHistoryResponse(messages = messages, totalCount = totalCount)
        }
    }

    suspend fun findSessionByIdAndUser(sessionId: Uuid, userId: Uuid): ChatSessionInternalDto? {
        return dbQuery {
            ChatSession.find {
                (ChatSessions.id eq sessionId) and (ChatSessions.userId eq userId)
            }.singleOrNull()?.toInternalDto()
        }
    }

    suspend fun createNewVariant(sessionId: Uuid, chatVariantId: Uuid): ChatVariantInternalDto {
        return dbQuery {
            val existing = ChatVariants
                .select(ChatVariants.id, ChatVariants.chatSessionId, ChatVariants.name, ChatVariants.unreadCount, ChatVariants.createdAt, ChatVariants.updatedAt)
                .where { ChatVariants.id eq chatVariantId }
                .singleOrNull()

            if (existing != null) {
                return@dbQuery ChatVariantInternalDto(
                    id = existing[ChatVariants.id].value,
                    chatSessionId = existing[ChatVariants.chatSessionId].value,
                    name = existing[ChatVariants.name],
                    unreadCount = existing[ChatVariants.unreadCount],
                    createdAt = existing[ChatVariants.createdAt],
                    updatedAt = existing[ChatVariants.updatedAt]
                )
            }

            val charRow = ChatSessions.innerJoin(AICharacters)
                .select(AICharacters.characterName, AICharacters.id)
                .where { ChatSessions.id eq sessionId }
                .singleOrNull()
                ?: throw IllegalStateException("Session $sessionId not found")
            val characterName = charRow[AICharacters.characterName]
            val charId = charRow[AICharacters.id].value

            val now = getUTCTimeNow()
            ChatVariants.insert {
                it[id] = EntityID(chatVariantId, ChatVariants)
                it[this.chatSessionId] = EntityID(sessionId, ChatSessions)
                it[name] = characterName
                it[createdAt] = now
                it[updatedAt] = now
            }

            ChatSessions.update({ ChatSessions.id eq sessionId }) {
                it[updatedAt] = now
            }

            ensureGreetingSlotExistsInternal(chatVariantId, charId)

            ChatVariantInternalDto(id = chatVariantId, chatSessionId = sessionId, name = characterName, unreadCount = 0, createdAt = now, updatedAt = now)
        }
    }

    suspend fun createSessionAndVariant(userId: Uuid, characterId: Uuid, chatSessionId: Uuid, chatVariantId: Uuid) {
        dbQuery {
            val now = getUTCTimeNow()
            val sessionExists = ChatSessions.select(ChatSessions.id).where { ChatSessions.id eq chatSessionId }.count() > 0L

            if (!sessionExists) {
                ChatSessions.insert {
                    it[id] = EntityID(chatSessionId, ChatSessions)
                    it[this.userId] = EntityID(userId, AppUsers)
                    it[this.characterId] = EntityID(characterId, AICharacters)
                    it[activeVariantId] = null
                    it[isArchived] = false
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }

            val variantExists = ChatVariants.select(ChatVariants.id).where { ChatVariants.id eq chatVariantId }.count() > 0L
            if (!variantExists) {
                val charName = AICharacters.select(AICharacters.characterName).where { AICharacters.id eq characterId }.singleOrNull()?.get(AICharacters.characterName) ?: "Chat"
                ChatVariants.insert {
                    it[id] = EntityID(chatVariantId, ChatVariants)
                    it[this.chatSessionId] = EntityID(chatSessionId, ChatSessions)
                    it[name] = charName
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }

            ChatSessions.update({ ChatSessions.id eq chatSessionId }) {
                it[activeVariantId] = EntityID(chatVariantId, ChatVariants)
                it[updatedAt] = now
            }

            ensureGreetingSlotExistsInternal(chatVariantId, characterId)
        }
    }

    fun ensureGreetingSlotExistsInternal(variantId: Uuid, characterId: Uuid) {
        val hasSlots = MessageSlots
            .select(MessageSlots.id)
            .where { (MessageSlots.chatVariantId eq variantId) and (MessageSlots.deletedAt.isNull()) }
            .count() > 0L

        if (hasSlots) return

        val charRow = AICharacters
            .select(AICharacters.firstMessage)
            .where { AICharacters.id eq characterId }
            .singleOrNull() ?: return

        val mainFirstMessage = charRow[AICharacters.firstMessage]
        val altGreetings = AICharacterAlternateGreetings
            .select(AICharacterAlternateGreetings.greetingText, AICharacterAlternateGreetings.orderIndex)
            .where { (AICharacterAlternateGreetings.characterId eq characterId) and (AICharacterAlternateGreetings.deletedAt.isNull()) }
            .orderBy(AICharacterAlternateGreetings.orderIndex, SortOrder.ASC)
            .toList()

        val allGreetings = buildList {
            if (!mainFirstMessage.isNullOrBlank()) {
                add(mainFirstMessage)
            }
            altGreetings.forEach { row ->
                val text = row[AICharacterAlternateGreetings.greetingText]
                if (text.isNotBlank() && !contains(text)) {
                    add(text)
                }
            }
        }

        if (allGreetings.isEmpty()) return

        val now = getUTCTimeNow()
        val slotId = Uuid.random()
        var primaryContainerId: Uuid? = null

        MessageSlots.insert {
            it[id] = slotId
            it[this.chatVariantId] = variantId
            it[activeMessageId] = null
            it[createdAt] = now
            it[updatedAt] = now
        }

        allGreetings.forEachIndexed { index, greetingText ->
            val containerId = Uuid.random()
            if (index == 0) primaryContainerId = containerId

            MessageContainers.insert {
                it[id] = containerId
                it[this.slotId] = slotId
                it[role] = MessageRole.MODEL
                it[rarity] = Rarity.COMMON
                it[createdAt] = now
                it[updatedAt] = now
            }

            MessageContents.insert {
                it[id] = Uuid.random()
                it[this.containerId] = containerId
                it[type] = AvalonMessageContentType.TEXT
                it[content] = greetingText
                it[orderIndex] = 0
                it[createdAt] = now
                it[updatedAt] = now
            }
        }

        if (primaryContainerId != null) {
            MessageSlots.update({ MessageSlots.id eq slotId }) {
                it[activeMessageId] = primaryContainerId
            }
        }
    }

    suspend fun ensureGreetingSlotExists(variantId: Uuid, characterId: Uuid) {
        dbQuery {
            ensureGreetingSlotExistsInternal(variantId, characterId)
        }
    }

    suspend fun addMessageContainer(
        slotId: Uuid,
        text: String,
        role: MessageRole,
        rarity: Rarity,
        contentType: AvalonMessageContentType,
        visualDescription: String?
    ): MessageContainerResponse {
        return dbQuery {
            val newContainerId = Uuid.random()
            val newContentId = Uuid.random()
            val visualDescriptionId = if (visualDescription != null) Uuid.random() else null
            val now = getUTCTimeNow()

            MessageContainers.insert {
                it[id] = newContainerId
                it[this.slotId] = slotId
                it[this.role] = role
                it[this.rarity] = rarity
                it[createdAt] = now
                it[updatedAt] = now
            }

            MessageContents.insert {
                it[id] = newContentId
                it[containerId] = newContainerId
                it[type] = contentType
                it[content] = text
                it[orderIndex] = 0
            }

            if (visualDescription != null && visualDescriptionId != null) {
                MessageContents.insert {
                    it[id] = visualDescriptionId
                    it[this.containerId] = newContainerId
                    it[type] = AvalonMessageContentType.GENERATED_IMAGE_DESCRIPTION
                    it[content] = visualDescription
                    it[orderIndex] = 1
                }
            }

            MessageSlots.update({ MessageSlots.id eq slotId }) {
                it[activeMessageId] = newContainerId
                it[updatedAt] = now
            }

            val finalContent = if (contentType == AvalonMessageContentType.IMAGE) {
                MessageContentCodec.processImageContentUrl(text)
            } else text

            val contents = mutableListOf(MessageContentResponse(id = newContentId, type = contentType, content = finalContent, orderIndex = 0))

            MessageContainerResponse(
                containerId = newContainerId,
                slotId = slotId,
                role = role,
                rarity = rarity,
                status = MessageStatus.SENT,
                userReaction = null,
                characterReaction = null,
                isRead = (role == MessageRole.USER || role == MessageRole.SYSTEM),
                createdAt = now,
                updatedAt = now,
                contents = contents
            )
        }
    }

    suspend fun updateCharacterReaction(containerId: Uuid, reaction: String?) {
        dbQuery {
            MessageContainers.update({ MessageContainers.id eq containerId }) {
                it[characterReaction] = reaction
            }
        }
    }

    suspend fun getPreviousMessageGiftInfo(chatVariantId: Uuid, slotId: Uuid): PreviousGiftInfo? {
        return dbQuery {
            val targetCreatedAt = MessageSlots.select(MessageSlots.createdAt).where { MessageSlots.id eq slotId }.singleOrNull()?.get(MessageSlots.createdAt) ?: return@dbQuery null

            val prevActiveContainerId = MessageSlots
                .select(MessageSlots.activeMessageId)
                .where {
                    (MessageSlots.chatVariantId eq chatVariantId) and (MessageSlots.createdAt less targetCreatedAt) and (MessageSlots.deletedAt.isNull())
                }
                .orderBy(MessageSlots.createdAt to SortOrder.DESC)
                .limit(1)
                .singleOrNull()?.get(MessageSlots.activeMessageId) ?: return@dbQuery null

            val giftContentString = MessageContents
                .select(MessageContents.content)
                .where { (MessageContents.containerId eq prevActiveContainerId) and (MessageContents.type eq AvalonMessageContentType.GIFT) }
                .singleOrNull()?.get(MessageContents.content) ?: return@dbQuery null

            val payload = MessageContentCodec.decodeAs<StickerGiftPayload>(giftContentString) ?: return@dbQuery null
            val giftRow = Gifts.select(Gifts.id, Gifts.price).where { Gifts.id eq payload.giftId }.singleOrNull() ?: return@dbQuery null

            PreviousGiftInfo(giftId = giftRow[Gifts.id].value, price = giftRow[Gifts.price])
        }
    }

    suspend fun getLastGiftInfo(chatVariantId: Uuid): PreviousGiftInfo? {
        return dbQuery {
            val lastSlot = MessageSlots
                .select(MessageSlots.id)
                .where { (MessageSlots.chatVariantId eq chatVariantId) and (MessageSlots.deletedAt.isNull()) }
                .orderBy(MessageSlots.createdAt to SortOrder.DESC)
                .limit(1)
                .singleOrNull() ?: return@dbQuery null

            val lastMessage = (MessageContainers innerJoin MessageContents)
                .select(MessageContents.content)
                .where { (MessageContainers.slotId eq lastSlot[MessageSlots.id]) and (MessageContents.type eq AvalonMessageContentType.GIFT) }
                .singleOrNull() ?: return@dbQuery null

            val contentString = lastMessage[MessageContents.content]
            val payload = MessageContentCodec.decodeAs<StickerGiftPayload>(contentString) ?: return@dbQuery null
            val giftData = Gifts.select(Gifts.id, Gifts.price).where { Gifts.id eq payload.giftId }.singleOrNull() ?: return@dbQuery null

            PreviousGiftInfo(giftId = giftData[Gifts.id].value, price = giftData[Gifts.price])
        }
    }

    suspend fun saveAiResponseWithReaction(
        userId: Uuid,
        chatSessionId: Uuid,
        chatVariantId: Uuid,
        targetSlotId: Uuid?,
        text: String,
        reaction: String?,
        rarity: Rarity,
        contentType: AvalonMessageContentType,
        visualDescription: String?,
        loreTimedStates: Map<Uuid, EntryTimedState> = emptyMap()
    ): SaveAiResponseResult = dbQuery {
        var reactionEvent: ServerMessage.SendCharacterReaction? = null

        if (reaction != null) {
            val lastUserContainerId = (MessageContainers innerJoin MessageSlots)
                .select(MessageContainers.id)
                .where { (MessageSlots.chatVariantId eq chatVariantId) and (MessageContainers.role eq MessageRole.USER) }
                .orderBy(MessageContainers.createdAt to SortOrder.DESC)
                .limit(1)
                .singleOrNull()
                ?.get(MessageContainers.id)

            if (lastUserContainerId != null) {
                MessageContainers.update({ MessageContainers.id eq lastUserContainerId }) {
                    it[characterReaction] = reaction
                }
                reactionEvent = ServerMessage.SendCharacterReaction(reaction = reaction, messageContainerId = lastUserContainerId.value)
            }
        }

        val turnContainerId = Uuid.random()
        val responseData: Any = if (targetSlotId != null) {
            addMessageContainer(targetSlotId, text, MessageRole.MODEL, rarity, contentType, visualDescription)
        } else {
            createNewMessageTurn(MessageRole.MODEL, rarity, Uuid.random(), Uuid.random(), turnContainerId, chatVariantId, chatSessionId, contentType, text, null, visualDescription)
        }

        val createdContainerId = when (responseData) {
            is MessageContainerResponse -> responseData.containerId
            is MessageSlotResponse -> responseData.activeContainerId ?: turnContainerId
            else -> turnContainerId
        }

        if (loreTimedStates.isNotEmpty()) {
            saveLoreTimedStatesInternal(createdContainerId, loreTimedStates)
        }

        SaveAiResponseResult(reactionEvent = reactionEvent, responseMessageData = responseData)
    }

    suspend fun getLastActiveBotContainerId(variantId: Uuid): Uuid? = dbQuery {
        val join = MessageSlots.innerJoin(MessageContainers, { MessageSlots.activeMessageId }, { MessageContainers.id })
        join.select(MessageContainers.id)
            .where {
                (MessageSlots.chatVariantId eq variantId) and
                (MessageSlots.deletedAt.isNull()) and
                (MessageContainers.role eq MessageRole.MODEL)
            }
            .orderBy(MessageSlots.createdAt, SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?.get(MessageContainers.id)
            ?.value
    }

    suspend fun getLoreTimedStates(containerId: Uuid): Map<Uuid, EntryTimedState> = dbQuery {
        getLoreTimedStatesInternal(containerId)
    }

    private fun getLoreTimedStatesInternal(containerId: Uuid): Map<Uuid, EntryTimedState> {
        return MessageContainerLoreStates.select(
            MessageContainerLoreStates.entryId,
            MessageContainerLoreStates.stickyRemaining,
            MessageContainerLoreStates.cooldownRemaining
        )
            .where { MessageContainerLoreStates.containerId eq containerId }
            .associate { row ->
                row[MessageContainerLoreStates.entryId].value to EntryTimedState(
                    stickyRemaining = row[MessageContainerLoreStates.stickyRemaining],
                    cooldownRemaining = row[MessageContainerLoreStates.cooldownRemaining]
                )
            }
    }

    suspend fun saveLoreTimedStates(containerId: Uuid, states: Map<Uuid, EntryTimedState>): Unit = dbQuery {
        saveLoreTimedStatesInternal(containerId, states)
    }

    private fun saveLoreTimedStatesInternal(containerId: Uuid, states: Map<Uuid, EntryTimedState>) {
        val activeStates = states.filter { it.value.stickyRemaining > 0 || it.value.cooldownRemaining > 0 }
        if (activeStates.isEmpty()) return

        MessageContainerLoreStates.batchInsert(activeStates.entries) { entry ->
            this[MessageContainerLoreStates.id] = Uuid.random()
            this[MessageContainerLoreStates.containerId] = containerId
            this[MessageContainerLoreStates.entryId] = entry.key
            this[MessageContainerLoreStates.stickyRemaining] = entry.value.stickyRemaining
            this[MessageContainerLoreStates.cooldownRemaining] = entry.value.cooldownRemaining
        }
    }

    suspend fun incrementCreatorAffinity(chatSessionId: Uuid) {
            try {
                dbQuery {
                    val sessionRow = ChatSessions
                        .select(ChatSessions.userId, ChatSessions.characterId)
                        .where { ChatSessions.id eq chatSessionId }
                        .singleOrNull()

                    if (sessionRow != null) {
                        val userId = sessionRow[ChatSessions.userId].value
                        val characterId = sessionRow[ChatSessions.characterId].value

                        val creatorId = AICharacters
                            .select(AICharacters.owner)
                            .where { AICharacters.id eq characterId }
                            .singleOrNull()
                            ?.get(AICharacters.owner)
                            ?.value

                        if (creatorId != null && userId != creatorId) {
                            val affinityRow = CreatorAffinityTable
                                .select(CreatorAffinityTable.id, CreatorAffinityTable.messageCount)
                                .where { (CreatorAffinityTable.userId eq userId) and (CreatorAffinityTable.creatorId eq creatorId) }
                                .singleOrNull()

                            val now = getUTCTimeNow()
                            if (affinityRow != null) {
                                val currentCount = affinityRow[CreatorAffinityTable.messageCount]
                                CreatorAffinityTable.update({ CreatorAffinityTable.id eq affinityRow[CreatorAffinityTable.id] }) {
                                    it[messageCount] = currentCount + 1
                                    it[updatedAt] = now
                                    it[lastInteractedAt] = now
                                }
                            } else {
                                CreatorAffinityTable.insert {
                                    it[id] = Uuid.random()
                                    it[CreatorAffinityTable.userId] = userId
                                    it[CreatorAffinityTable.creatorId] = creatorId
                                    it[messageCount] = 1
                                    it[createdAt] = now
                                    it[updatedAt] = now
                                    it[lastInteractedAt] = now
                                }
                            }

                            // Автоматически завершаем PENDING аутрич со статусом INTERESTED при первом сообщении пользователя
                            val outreachRow = OutreachHistoryTable
                                .select(OutreachHistoryTable.id, OutreachHistoryTable.feedbackStatus)
                                .where { (OutreachHistoryTable.characterId eq characterId) and (OutreachHistoryTable.userId eq userId) and (OutreachHistoryTable.feedbackStatus eq FeedbackStatus.PENDING) }
                                .singleOrNull()

                            if (outreachRow != null) {
                                val historyId = outreachRow[OutreachHistoryTable.id].value
                                OutreachHistoryTable.update({ OutreachHistoryTable.id eq historyId }) {
                                    it[feedbackStatus] = FeedbackStatus.INTERESTED
                                    it[updatedAt] = now
                                }

                                val botRow = AICharacters
                                    .select(AICharacters.outreachRepliedCount)
                                    .where { AICharacters.id eq characterId }
                                    .singleOrNull()

                                val currentRepliedCount = botRow?.get(AICharacters.outreachRepliedCount) ?: 0
                                AICharacters.update({ AICharacters.id eq characterId }) {
                                    it[outreachRepliedCount] = currentRepliedCount + 1
                                    it[consecutiveFailuresCount] = 0
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error in incrementCreatorAffinity: ${e.message}")
            }
    }

    suspend fun updateAiMessageText(contentId: Uuid, newText: String) = dbQuery {
        MessageContents.update({ MessageContents.id eq contentId }) {
            it[content] = newText
            it[updatedAt] = getUTCTimeNow()
        }
    }

    suspend fun finalizeAiMessageWithReaction(
        userId: Uuid,
        chatSessionId: Uuid,
        chatVariantId: Uuid,
        slotId: Uuid,
        contentId: Uuid,
        finalText: String,
        reaction: String?
    ): ServerMessage.SendCharacterReaction? = dbQuery {
        // Финализируем текст
        MessageContents.update({ MessageContents.id eq contentId }) {
            it[content] = finalText
            it[updatedAt] = getUTCTimeNow()
        }

        // Если есть реакция, ищем последний контейнер юзера (как в saveAiResponseWithReaction)
        if (reaction != null) {
            val lastUserContainerId = (MessageContainers innerJoin MessageSlots)
                .select(MessageContainers.id)
                .where { (MessageSlots.chatVariantId eq chatVariantId) and (MessageContainers.role eq MessageRole.USER) }
                .orderBy(MessageContainers.createdAt to SortOrder.DESC)
                .limit(1)
                .singleOrNull()
                ?.get(MessageContainers.id)

            if (lastUserContainerId != null) {
                MessageContainers.update({ MessageContainers.id eq lastUserContainerId }) {
                    it[characterReaction] = reaction
                }
                return@dbQuery ServerMessage.SendCharacterReaction(reaction = reaction, messageContainerId = lastUserContainerId.value)
            }
        }
        return@dbQuery null
    }
}
