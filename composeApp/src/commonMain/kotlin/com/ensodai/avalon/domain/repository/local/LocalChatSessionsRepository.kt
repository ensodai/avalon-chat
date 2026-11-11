package com.ensodai.avalon.domain.repository.local

import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.data.db.dto.ChatSessionPreviewDto
import com.ensodai.avalon.data.db.dto.ChatVariantDto
import com.ensodai.avalon.data.db.models.ChatSessionPreview
import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.avalon.data.db.models.chat.ChatSessionEntity
import com.ensodai.avalon.data.db.models.chat.ChatVariantEntity
import com.ensodai.avalon.data.db.models.chat.SessionCharTuple
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

interface LocalChatSessionsRepository {

    suspend fun syncChatSessions(sessions: List<ChatSessionPreviewDto>)

    suspend fun hardDeleteSessions(sessions: List<Uuid>)

    suspend fun createLocalChatVariantAndSetAsActive(
        sessionId: Uuid,
        createdAt: Instant,
        newVariantId: Uuid
    )

    fun getChatSessionsPagingData(isArchived: Boolean): Flow<List<ChatSessionPreview>>

    fun getArchivedChatSessionsNames(): Flow<List<String>>

    //==============================================================================================

    suspend fun getChatSessionsBySyncAction(syncAction: SyncAction): List<ChatSessionEntity>

    suspend fun getChatVariantsBySyncAction(syncAction: SyncAction): List<ChatVariantEntity>

    suspend fun markChatSessionsAsSynced(updateResult: Map<Uuid, Instant>): Int

    suspend fun hardDeleteChatSessions(ids: List<Uuid>): Int

    suspend fun softDeleteChatSessions(ids: List<Uuid>): Int

    suspend fun updateSyncActionChatSessionByCharacterId(
        ids: List<Uuid>,
        targetAction: SyncAction
    ): Int

    suspend fun archiveChatSessions(chatSessionsIds: List<Uuid>): Int

    suspend fun unArchiveChatSessions(chatSessionsIds: List<Uuid>): Int

    suspend fun markChatSessionsAsSynced(ids: List<Uuid>): Int

    suspend fun upsertChatSessions(sessions: List<ChatSessionPreviewDto>)

    suspend fun upsertChatVariants(variants: List<ChatVariantDto>)

    suspend fun updateActiveSessionVariant(sessionId: Uuid, variantId: Uuid)

    suspend fun hardDeleteChatVariants(ids: List<Uuid>)

    suspend fun getActiveVariantIdBySessionId(sessionId: Uuid): Uuid?

    fun getActiveVariantIdBySessionIdFlow(sessionId: Uuid): Flow<Uuid?>

    suspend fun getSessionByCharacterId(characterId: Uuid): ChatSessionEntity?

    suspend fun getVariantsByCharacterId(characterId: Uuid): List<ChatVariantEntity>

    fun getSessionByCharacterIdFlow(characterId: Uuid): Flow<Uuid?>

    fun getActiveChatVariantFlow(characterId: Uuid): Flow<Uuid?>

    suspend fun createChatSessionAndChatVariant(
        chatSessionId: Uuid,
        chatVariantId: Uuid,
        characterId: Uuid,
        createdAt: Instant
    )

    suspend fun setChatVariantAsActive(
        characterId: Uuid,
        chatSessionId: Uuid,
        chatVariantId: Uuid
    )

    suspend fun getAllSessionIds(): Set<Uuid>

    suspend fun findConflictingSessions(charIds: Collection<Uuid>): List<SessionCharTuple>

    suspend fun getSelectedLlmConfig(characterId: Uuid): AvailableLlmConfigs?

    suspend fun selectActiveLlmConfig(characterId: Uuid, config: AvailableLlmConfigs)

    suspend fun updateSyncAction(ids: List<Uuid>, syncAction: SyncAction)

    suspend fun getChatSessionsByIds(ids: List<Uuid>): List<ChatSessionEntity>

    suspend fun updateDraftMessage(sessionId: Uuid, draftMessage: String?)
}
