package com.ensodai.avalon.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import androidx.room3.Update
import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.data.db.models.ChatSessionPreview
import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.avalon.data.db.models.chat.ChatSessionEntity
import com.ensodai.avalon.data.db.models.chat.ChatVariantEntity
import com.ensodai.avalon.data.db.models.chat.SessionCharTuple
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
abstract class ChatSessionDao {

    @Query("SELECT * FROM chat_session_entity WHERE character_id = :characterId")
    abstract fun getSessionFlow(characterId: Int): Flow<ChatSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_upsertChatSessions(sessions: List<ChatSessionEntity>)

    @Update
    protected abstract suspend fun _update_upsertChatSessions(sessions: List<ChatSessionEntity>)

    @Transaction
    open suspend fun upsertChatSessions(sessions: List<ChatSessionEntity>) {
        _insert_upsertChatSessions(sessions)
        _update_upsertChatSessions(sessions)
    }

    @Query("UPDATE chat_session_entity SET active_chat_variant_id = :variantId WHERE id = :sessionId")
    abstract suspend fun updateActiveVariant(sessionId: Uuid, variantId: Uuid)

    @Query("UPDATE chat_session_entity SET draft_message = :draftMessage WHERE id = :sessionId")
    abstract suspend fun updateDraftMessage(sessionId: Uuid, draftMessage: String?)

    @Query(
        """
        SELECT 
            session.id AS sessionId,
            session.character_id AS characterId,
            char.name AS characterName,
            av.image_url AS characterAvatarUrl,
            session.last_message_content AS lastMessageContent,
            session.last_message_type AS lastMessageType,
            session.last_message_time AS lastMessageTime,
            session.updated_at AS updatedAt,
            session.is_archived AS isArchived,
            session.draft_message AS draftMessage,
            COALESCE(variant.unread_count, 0) AS unreadCount
        FROM chat_session_entity AS session
        LEFT JOIN character_entity AS char ON session.character_id = char.id
        LEFT JOIN character_avatar_entity AS av ON char.id = av.character_id
        LEFT JOIN chat_variant_entity AS variant ON session.active_chat_variant_id = variant.id
        WHERE session.is_archived = :isArchived 
          AND session.sync_state != :excludeStatus
        ORDER BY COALESCE(session.updated_at, session.created_at) DESC
    """
    )
    abstract fun getChatsPagingSource(
        isArchived: Boolean,
        excludeStatus: SyncAction = SyncAction.DELETE
    ): Flow<List<ChatSessionPreview>>

    @Query(
        """
        SELECT char.name
        FROM chat_session_entity AS session
        INNER JOIN character_entity AS char ON session.character_id = char.id
        WHERE session.is_archived = 1 
          AND session.sync_state != :syncActionDelete
        ORDER BY COALESCE(session.last_message_time, session.updated_at) DESC
        LIMIT 20
    """
    )
    abstract fun getArchivedCharacterNamesFlow(
        syncActionDelete: SyncAction = SyncAction.DELETE
    ): Flow<List<String>>

    //==============================================================================================
    @Query("SELECT * FROM chat_session_entity WHERE sync_state = :syncAction ORDER BY updated_at DESC")
    abstract suspend fun getChatSessionsBySyncAction(syncAction: SyncAction): List<ChatSessionEntity>

    @Query("SELECT * FROM chat_variant_entity WHERE sync_state = :syncAction ORDER BY updated_at DESC")
    abstract suspend fun getChatVariantsBySyncAction(syncAction: SyncAction): List<ChatVariantEntity>


    @Query(
        """
        UPDATE chat_session_entity 
        SET sync_state = :syncedAction, updated_at = :updatedAt 
        WHERE id = :id
    """
    )
    abstract suspend fun updateChatSessionSync(
        id: Uuid,
        updatedAt: Instant,
        syncedAction: SyncAction
    ): Int

    /**
     * Получает сессии по ID для pull characterId перед удалением.
     * @param ids Список ID сессий.
     * @return List<ChatSessionEntity> (с characterId для проверки).
     */
    @Query("SELECT * FROM chat_session_entity WHERE id IN (:ids)")
    abstract suspend fun getChatSessionsByIds(ids: List<Uuid>): List<ChatSessionEntity>

    /**
     * Жёстко удаляет чат-сессии по ID: CASCADE потянет Variants, Slots, Containers, Contents, Images — чисто!
     * @param ids Список ID сессий (батч для скорости).
     * @return Int — кол-во удалённых сессий (проверь == ids.size для full clean).
     */
    @Query("DELETE FROM chat_session_entity WHERE id IN (:ids)")
    abstract suspend fun hardDeleteChatSessions(ids: List<Uuid>): Int

    /**
     * Soft-delete: ставит syncAction = DELETE для сессий по ID (батч для скорости).
     * @param ids Список ID сессий на "удаление".
     * @return Int — кол-во обновлённых (== ids.size? Успех!).
     */
    @Query("UPDATE chat_session_entity SET sync_state = :deleteAction WHERE id IN (:ids)")
    abstract suspend fun softDeleteChatSessions(ids: List<Uuid>, deleteAction: SyncAction): Int

    /**
     * Обновляет sync_state для всех сессий по characterIds.
     */
    @Query("UPDATE chat_session_entity SET sync_state = :targetAction WHERE character_id IN (:characterIds)")
    abstract suspend fun updateSyncActionChatSessionByCharacterIds(
        characterIds: List<Uuid>,
        targetAction: SyncAction
    ): Int

    @Query(
        """
        UPDATE chat_session_entity 
        SET is_archived = :archived, sync_state = :updateAction 
        WHERE id IN (:ids)
    """
    )
    abstract suspend fun archiveChatSessions(
        ids: List<Uuid>,
        archived: Boolean,
        updateAction: SyncAction
    ): Int

    /**
     * Unarchive/синк: syncAction = NONE для сессий по ID (батч, после API).
     * @param ids Список ID сессий на "чистку флага".
     * @return Int — кол-во обновлённых.
     */
    @Query("UPDATE chat_session_entity SET sync_state = :noneAction WHERE id IN (:ids)")
    abstract suspend fun markChatSessionsAsSynced(ids: List<Uuid>, noneAction: SyncAction): Int

    @Query("SELECT active_chat_variant_id FROM chat_session_entity WHERE id = :sessionId")
    abstract suspend fun getActiveVariantId(sessionId: Uuid): Uuid?

    @Query("SELECT active_chat_variant_id FROM chat_session_entity WHERE id = :sessionId")
    abstract fun getActiveVariantIdFlow(sessionId: Uuid): Flow<Uuid?>

    @Query(
        """
        UPDATE chat_session_entity 
        SET last_message_content = :content, 
            last_message_time = :time,
            last_message_type = :type,
            last_message_role = :role,
            updated_at = :time
        WHERE id = :sessionId 
          AND (last_message_time IS NULL OR last_message_time <= :time)
    """
    )
    abstract suspend fun updateSessionPreview(
        sessionId: Uuid,
        content: String?,
        time: Instant,
        type: AvalonMessageContentType,
        role: MessageRole
    )

    @Query("SELECT * FROM chat_session_entity WHERE character_id = :characterId LIMIT 1")
    abstract suspend fun getSessionByCharacterId(characterId: Uuid): ChatSessionEntity?

    @Query(
        """
        SELECT variant.* 
        FROM chat_variant_entity AS variant
        INNER JOIN chat_session_entity AS session ON variant.chat_session_id = session.id
        WHERE session.character_id = :characterId
    """
    )
    abstract suspend fun getVariantsByCharacterId(characterId: Uuid): List<ChatVariantEntity>

    @Query("SELECT id FROM chat_session_entity WHERE character_id = :characterId")
    abstract fun getSessionByCharacterIdFlow(characterId: Uuid): Flow<Uuid?>

    @Query("SELECT active_chat_variant_id FROM chat_session_entity WHERE character_id = :characterId")
    abstract fun getActiveChatVariantFlow(characterId: Uuid): Flow<Uuid?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertSession(chatSession: ChatSessionEntity)

    @Update
    protected abstract suspend fun _update_insertSession(chatSession: ChatSessionEntity)

    @Transaction
    open suspend fun insertSession(chatSession: ChatSessionEntity) {
        _insert_insertSession(chatSession)
        _update_insertSession(chatSession)
    }// 2. Базовая вставка варианта

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertVariant(chatVariant: ChatVariantEntity)

    @Update
    protected abstract suspend fun _update_insertVariant(chatVariant: ChatVariantEntity)

    @Transaction
    open suspend fun insertVariant(chatVariant: ChatVariantEntity) {
        _insert_insertVariant(chatVariant)
        _update_insertVariant(chatVariant)
    }// 3. Обновление активного варианта в сессии
//    @Query("UPDATE chat_session_entity SET active_chat_variant_id = :variantId WHERE id = :sessionId")
//    abstract suspend fun updateActiveVariant(sessionId: UUID, variantId: UUID)

    // ГЛАВНЫЙ МЕТОД: Транзакция
    @Transaction
    open suspend fun insertSessionAndVariant(
        session: ChatSessionEntity,
        variant: ChatVariantEntity
    ) {
        // Шаг A: Вставляем сессию, но принудительно зануляем active_chat_variant_id,
        // чтобы не нарушить FK constraints, так как вариант еще не в базе.
        insertSession(session.copy(activeChatVariantId = null))

        // Шаг B: Вставляем вариант (он уже знает про sessionId)
        insertVariant(variant)

        // Шаг C: Теперь, когда вариант в базе, обновляем сессию, связывая их
        updateActiveVariant(session.id, variant.id)
    }

    @Query("SELECT EXISTS(SELECT 1 FROM chat_session_entity WHERE id = :id)")
    abstract suspend fun exists(id: Uuid): Boolean

    @Query(
        """
        UPDATE chat_session_entity
        SET 
            active_chat_variant_id = :chatVariantId,
            updated_at = :updatedAt
        WHERE id = :chatSessionId 
          AND character_id = :characterId
          AND EXISTS (
              SELECT 1 FROM chat_variant_entity 
              WHERE id = :chatVariantId 
              AND chat_session_id = :chatSessionId
          )
    """
    )
    abstract suspend fun updateActiveVariant(
        characterId: Uuid,
        chatSessionId: Uuid,
        chatVariantId: Uuid,
        updatedAt: Instant
    ): Int

    @Query("SELECT id FROM chat_session_entity")
    abstract suspend fun getAllSessionIds(): List<Uuid>

    @Query("SELECT id, character_id FROM chat_session_entity WHERE character_id IN (:charIds)")
    abstract suspend fun findConflictingSessions(charIds: Collection<Uuid>): List<SessionCharTuple>

    @Query(
        """
        SELECT config.* 
        FROM llm_configs AS config
        INNER JOIN chat_session_entity AS session 
            ON config.code_name = session.selected_llm_config_code_name
        WHERE session.character_id = :characterId
    """
    )
    abstract suspend fun getSelectedLlmConfig(characterId: Uuid): AvailableLlmConfigs?

    @Query("UPDATE chat_session_entity SET selected_llm_config_code_name = :codeName WHERE character_id = :characterId")
    abstract suspend fun updateSelectedConfig(characterId: Uuid, codeName: String)

    @Query("UPDATE chat_session_entity SET sync_state = :syncAction WHERE id IN (:ids)")
    abstract suspend fun updateSyncAction(ids: List<Uuid>, syncAction: SyncAction)
}
