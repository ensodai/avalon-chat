package com.ensodai.avalon.data.repository.local

import com.ensodai.avalon.data.repository.local.base.BaseRepository
import com.ensodai.avalon.data.repository.local.base.DatabaseExecutor

import com.ensodai.avalon.data.db.AppDataBase
import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.data.db.dao.CacheDao
import com.ensodai.avalon.data.db.dao.CharacterDao
import com.ensodai.avalon.data.db.dao.ChatSessionDao
import com.ensodai.avalon.data.db.dao.ChatVariantDao
import com.ensodai.avalon.data.db.dto.ChatSessionPreviewDto
import com.ensodai.avalon.data.db.dto.ChatVariantDto
import com.ensodai.avalon.data.db.models.ChatSessionPreview
import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.avalon.data.db.models.chat.ChatSessionEntity
import com.ensodai.avalon.data.db.models.chat.ChatVariantEntity
import com.ensodai.avalon.data.db.models.chat.ChatVariantImageEntity
import com.ensodai.avalon.data.db.models.chat.SessionCharTuple
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.getUTCTimeNow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.time.Instant
import kotlin.uuid.Uuid

class LocalChatSessionsRepositoryImpl(
    dbExecutor: DatabaseExecutor,
    private val chatSessionsDao: ChatSessionDao,
    private val appDatabase: AppDataBase,
    private val chatVariantDao: ChatVariantDao,
    private val characterDao: CharacterDao,
    private val cacheDao: CacheDao,
    private val logger: AppLogger
) : BaseRepository(dbExecutor), LocalChatSessionsRepository {

    override suspend fun syncChatSessions(sessions: List<ChatSessionPreviewDto>) {
        val ids = sessions.map { it.id }
        val existingSessions = dbRead { chatSessionsDao.getChatSessionsByIds(ids) }.associateBy { it.id }

        val sessionEntities = sessions.map { dto ->
            val existingSession = existingSessions[dto.id]
            val existingConfig = existingSession?.selectedLlmConfigCodeName
            val existingDraft = existingSession?.draftMessage

            ChatSessionEntity(
                id = dto.id,
                characterId = dto.characterId,
                updatedAt = dto.updatedAt,
                createdAt = dto.createdAt,
                isArchived = dto.isArchived,
                activeChatVariantId = dto.activeChatVariantId,
                lastMessageContent = dto.lastMessage?.content,
                lastMessageType = dto.lastMessage?.type,
                lastMessageRole = dto.lastMessage?.role,
                lastMessageTime = dto.lastMessage?.createdAt,
                syncAction = SyncAction.NONE,
                selectedLlmConfigCodeName = existingConfig,
                draftMessage = existingDraft
            )
        }

        dbWrite {
            chatSessionsDao.upsertChatSessions(sessionEntities)
        }
    }

    override suspend fun hardDeleteSessions(sessions: List<Uuid>) {
        if (sessions.isNotEmpty()) {
            dbWrite { chatSessionsDao.hardDeleteChatSessions(sessions) }
        }
    }

    override suspend fun createLocalChatVariantAndSetAsActive(
        sessionId: Uuid,
        createdAt: Instant,
        newVariantId: Uuid,
    ) {
        val newVariant = ChatVariantEntity(
            id = newVariantId,
            syncAction = SyncAction.NONE,
            chatSessionId = sessionId,
            name = "New Chat",
            createdAt = createdAt,
            updatedAt = createdAt
        )

        dbWrite { chatVariantDao.createAndActivateVariant(newVariant) }
    }

    override suspend fun upsertChatVariants(variants: List<ChatVariantDto>) {

        variants.forEach { chatVariantDto ->
            val imageEntity = chatVariantDto.image?.let {
                ChatVariantImageEntity(
                    id = it.id,
                    chatVariantId = chatVariantDto.id,
                    imageUrl = it.imageUrl,
                    thumbHash = it.thumbHash
                )
            }
            val chatVariantEntity = ChatVariantEntity(
                id = chatVariantDto.id,
                syncAction = SyncAction.NONE,
                chatSessionId = chatVariantDto.sessionId,
                name = chatVariantDto.name,
                unreadCount = chatVariantDto.unreadCount,
                createdAt = chatVariantDto.createdAt,
                updatedAt = chatVariantDto.updatedAt
            )
            dbWrite { chatVariantDao.upsertChatVariantState(chatVariantEntity, imageEntity) }
        }
    }

    override fun getChatSessionsPagingData(isArchived: Boolean): Flow<List<ChatSessionPreview>> {
        return dbObserve { chatSessionsDao.getChatsPagingSource(isArchived) } as Flow<List<ChatSessionPreview>>
    }

    override fun getArchivedChatSessionsNames(): Flow<List<String>> {
        return dbObserve { chatSessionsDao.getArchivedCharacterNamesFlow() } as Flow<List<String>>
    }

    //==============================================================================================
    override suspend fun getChatSessionsBySyncAction(syncAction: SyncAction): List<ChatSessionEntity> {
        return dbRead { chatSessionsDao.getChatSessionsBySyncAction(syncAction) }
    }

    override suspend fun getChatVariantsBySyncAction(syncAction: SyncAction): List<ChatVariantEntity> {
        return dbRead { chatSessionsDao.getChatVariantsBySyncAction(syncAction) }
    }

    override suspend fun markChatSessionsAsSynced(updateResult: Map<Uuid, Instant>): Int {
        if (updateResult.isEmpty()) return 0
        var totalUpdated = 0
        dbWrite {
            updateResult.forEach { (id, updatedAt) ->
                totalUpdated += chatSessionsDao.updateChatSessionSync(
                    id = id,
                    updatedAt = updatedAt,
                    syncedAction = SyncAction.NONE
                )
            }
        }
        return totalUpdated
    }

    override suspend fun hardDeleteChatSessions(ids: List<Uuid>): Int {
        if (ids.isEmpty()) return 0

        return try {
            dbWrite {
                val sessions = chatSessionsDao.getChatSessionsByIds(ids)
                val foreignCharacterIds = mutableListOf<Uuid>()
                sessions.forEach { session ->
                    val character = characterDao.getCharacterById(session.characterId)
                    if (character != null && !character.canEdit) {
                        foreignCharacterIds.add(character.id)
                    }
                }

                var totalDeleted = 0
                if (foreignCharacterIds.isNotEmpty()) {
                    totalDeleted += characterDao.hardDeleteCharacters(foreignCharacterIds)
                }

                totalDeleted += chatSessionsDao.hardDeleteChatSessions(ids)
                totalDeleted
            }
        } catch (e: Exception) {
            logger.e(
                "LocalChatSessionsRepositoryImpl",
                "Ошибка жёсткого удаления сессий: ${e.stackTraceToString()}"
            )
            0
        }
    }

    override suspend fun softDeleteChatSessions(ids: List<Uuid>): Int {
        if (ids.isEmpty()) return 0
        return dbWrite { chatSessionsDao.softDeleteChatSessions(ids = ids, deleteAction = SyncAction.DELETE) }
    }

    override suspend fun updateSyncActionChatSessionByCharacterId(
        ids: List<Uuid>,
        targetAction: SyncAction
    ): Int {
        return dbWrite {
            chatSessionsDao.updateSyncActionChatSessionByCharacterIds(
                characterIds = ids,
                targetAction = targetAction
            )
        }
    }

    override suspend fun archiveChatSessions(chatSessionsIds: List<Uuid>): Int {
        if (chatSessionsIds.isEmpty()) return 0
        return dbWrite {
            chatSessionsDao.archiveChatSessions(
                ids = chatSessionsIds,
                archived = true,
                updateAction = SyncAction.UPDATE
            )
        }
    }

    override suspend fun unArchiveChatSessions(chatSessionsIds: List<Uuid>): Int {
        if (chatSessionsIds.isEmpty()) return 0
        return dbWrite {
            chatSessionsDao.archiveChatSessions(
                ids = chatSessionsIds,
                archived = false,
                updateAction = SyncAction.UPDATE
            )
        }
    }

    override suspend fun markChatSessionsAsSynced(ids: List<Uuid>): Int {
        if (ids.isEmpty()) return 0
        return dbWrite {
            chatSessionsDao.markChatSessionsAsSynced(
                ids = ids,
                noneAction = SyncAction.NONE
            )
        }
    }

    override suspend fun upsertChatSessions(sessions: List<ChatSessionPreviewDto>) {
        val ids = sessions.map { it.id }
        val existingSessions = dbRead { chatSessionsDao.getChatSessionsByIds(ids) }.associateBy { it.id }

        val entities = sessions.map { dto ->
            val existingSession = existingSessions[dto.id]
            val existingConfig = existingSession?.selectedLlmConfigCodeName
            val existingDraft = existingSession?.draftMessage

            ChatSessionEntity(
                id = dto.id,
                characterId = dto.characterId,
                updatedAt = dto.updatedAt,
                createdAt = dto.createdAt,
                isArchived = dto.isArchived,
                activeChatVariantId = dto.activeChatVariantId,
                lastMessageContent = dto.lastMessage?.content,
                lastMessageType = dto.lastMessage?.type,
                lastMessageRole = dto.lastMessage?.role,
                lastMessageTime = dto.lastMessage?.createdAt,
                syncAction = SyncAction.NONE,
                selectedLlmConfigCodeName = existingConfig,
                draftMessage = existingDraft
            )
        }
        dbWrite { chatSessionsDao.upsertChatSessions(entities) }
    }

    override suspend fun updateActiveSessionVariant(sessionId: Uuid, variantId: Uuid) {
        dbWrite { chatSessionsDao.updateActiveVariant(sessionId, variantId) }
    }

    override suspend fun hardDeleteChatVariants(ids: List<Uuid>) {
        dbWrite { chatVariantDao.deleteVariants(ids) }
    }

    override suspend fun getActiveVariantIdBySessionId(sessionId: Uuid): Uuid? {
        return dbRead { chatSessionsDao.getActiveVariantId(sessionId) }
    }

    override fun getActiveVariantIdBySessionIdFlow(sessionId: Uuid): Flow<Uuid?> {
        return cachedObserve("getActiveVariantIdBySessionIdFlow_${sessionId}") { chatSessionsDao.getActiveVariantIdFlow(sessionId) } as Flow<Uuid?>
    }

    override suspend fun getSessionByCharacterId(characterId: Uuid): ChatSessionEntity? {
        return dbRead { chatSessionsDao.getSessionByCharacterId(characterId) }
    }

    override suspend fun getVariantsByCharacterId(characterId: Uuid): List<ChatVariantEntity> {
        return dbRead { chatSessionsDao.getVariantsByCharacterId(characterId) }
    }

    override fun getActiveChatVariantFlow(characterId: Uuid): Flow<Uuid?> {
        return cachedObserve("getActiveChatVariantFlow_${characterId}") { chatSessionsDao.getActiveChatVariantFlow(characterId) }
            .distinctUntilChanged() as Flow<Uuid?>
    }

    override fun getSessionByCharacterIdFlow(characterId: Uuid): Flow<Uuid?> {
        return cachedObserve("getSessionByCharacterIdFlow_${characterId}") { chatSessionsDao.getSessionByCharacterIdFlow(characterId) } as Flow<Uuid?>
    }

    override suspend fun createChatSessionAndChatVariant(
        chatSessionId: Uuid,
        chatVariantId: Uuid,
        characterId: Uuid,
        createdAt: Instant
    ) {
        val session = ChatSessionEntity(
            id = chatSessionId,
            characterId = characterId,
            createdAt = createdAt,
            updatedAt = createdAt,
            isArchived = false,
            syncAction = SyncAction.NONE,
            activeChatVariantId = chatVariantId,
            lastMessageContent = null,
            lastMessageType = null,
            lastMessageRole = null,
            lastMessageTime = null
        )

        val variant = ChatVariantEntity(
            id = chatVariantId,
            syncAction = SyncAction.NONE,
            chatSessionId = chatSessionId,
            name = "New Chat",
            createdAt = createdAt,
            updatedAt = createdAt
        )

        dbWrite { chatSessionsDao.insertSessionAndVariant(session, variant) }
    }

    override suspend fun setChatVariantAsActive(
        characterId: Uuid,
        chatSessionId: Uuid,
        chatVariantId: Uuid
    ) {
        val now = getUTCTimeNow()
        dbWrite {
            chatSessionsDao.updateActiveVariant(
                characterId = characterId,
                chatSessionId = chatSessionId,
                chatVariantId = chatVariantId,
                updatedAt = now
            )
        }
    }

    override suspend fun getAllSessionIds(): Set<Uuid> {
        return dbRead { chatSessionsDao.getAllSessionIds() }.toSet()
    }

    override suspend fun findConflictingSessions(charIds: Collection<Uuid>): List<SessionCharTuple> {
        return dbRead { chatSessionsDao.findConflictingSessions(charIds) }
    }

    override suspend fun getSelectedLlmConfig(characterId: Uuid): AvailableLlmConfigs? {
        return dbRead { chatSessionsDao.getSelectedLlmConfig(characterId) }
    }

    override suspend fun selectActiveLlmConfig(characterId: Uuid, config: AvailableLlmConfigs) {
        dbWrite {
            chatSessionsDao.updateSelectedConfig(
                characterId = characterId,
                codeName = config.codeName
            )
        }
    }

    override suspend fun updateSyncAction(ids: List<Uuid>, syncAction: SyncAction) {
        dbWrite { chatSessionsDao.updateSyncAction(ids, syncAction) }
    }

    override suspend fun getChatSessionsByIds(ids: List<Uuid>): List<ChatSessionEntity> {
        return dbRead { chatSessionsDao.getChatSessionsByIds(ids) }
    }

    override suspend fun updateDraftMessage(sessionId: Uuid, draftMessage: String?) {
        dbWrite { chatSessionsDao.updateDraftMessage(sessionId, draftMessage) }
    }
}
