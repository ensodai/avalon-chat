package com.ensodai.avalon.data.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import androidx.room3.Update
import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.avalon.data.db.models.chat.ContentPreviewDto
import com.ensodai.avalon.data.db.models.chat.SlotWithContainers
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.avalon.data.db.models.message.MessageContainerEntity
import com.ensodai.avalon.data.db.models.message.MessageContentEntity
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.avalon.data.db.models.message.MessageSlotEntity
import com.ensodai.avalon.data.db.models.message.SlotInfo
import com.ensodai.shared.avalon.chat.models.MessageStatus
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Dao
abstract class ChatHistoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertSlot(slot: MessageSlotEntity)

    @Update
    protected abstract suspend fun _update_insertSlot(slot: MessageSlotEntity)

    @Transaction
    open suspend fun insertSlot(slot: MessageSlotEntity) {
        _insert_insertSlot(slot)
        _update_insertSlot(slot)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertContainers(containers: List<MessageContainerEntity>)

    @Update
    protected abstract suspend fun _update_insertContainers(containers: List<MessageContainerEntity>)

    @Transaction
    open suspend fun insertContainers(containers: List<MessageContainerEntity>) {
        _insert_insertContainers(containers)
        _update_insertContainers(containers)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertContents(contents: List<MessageContentEntity>)

    @Update
    protected abstract suspend fun _update_insertContents(contents: List<MessageContentEntity>)

    @Transaction
    open suspend fun insertContents(contents: List<MessageContentEntity>) {
        _insert_insertContents(contents)
        _update_insertContents(contents)
    }

    @Query("SELECT MAX(created_at) FROM message_slots WHERE chat_variant_id = :variantId")
    abstract suspend fun getLastSlotCreatedAt(variantId: Uuid): Instant?

    // 1. Приватный или вспомогательный метод для обновления контейнера
    @Query(
        """
        UPDATE message_containers 
        SET status = :status, 
            sync_action = :syncAction, 
            updated_at = :serverTimestamp
        WHERE id = :containerId        
    """
    )
    abstract suspend fun updateContainerSentStatus(
        containerId: Uuid,
        status: MessageStatus,
        syncAction: SyncAction,
        serverTimestamp: Instant
    )

    // 2. Вспомогательный метод для обновления слота
    @Query(
        """
        UPDATE message_slots
        SET sync_action = :syncAction,
            updated_at = :serverTimestamp
        WHERE id = :slotId
    """
    )
    abstract suspend fun updateSlotSyncStatus(
        slotId: Uuid,
        syncAction: SyncAction,
        serverTimestamp: Instant
    )

    // 3. Основной метод, который обновляет обе таблицы в одной транзакции
    @Transaction
    open suspend fun markMessageAsSent(
        containerId: Uuid,
        slotId: Uuid,
        status: MessageStatus,
        syncAction: SyncAction,
        serverTimestamp: Instant
    ) {
        // Обновляем сам контейнер сообщения
        updateContainerSentStatus(
            containerId = containerId,
            status = status,
            syncAction = syncAction,
            serverTimestamp = serverTimestamp
        )

        // Обновляем слот, к которому привязано сообщение
        updateSlotSyncStatus(
            slotId = slotId,
            syncAction = syncAction,
            serverTimestamp = serverTimestamp
        )
    }

    // 1. Вспомогательный метод: обновляем статус контейнера на FAILED и убираем синхронизацию
    @Query(
        """
        UPDATE message_containers 
        SET status = :status, 
            sync_action = :syncAction, 
            updated_at = :serverTimestamp
        WHERE id = :containerId
    """
    )
    abstract suspend fun updateContainerErrorStatus(
        containerId: Uuid,
        status: MessageStatus, // TypeConverter обработает FAILED
        syncAction: SyncAction,
        serverTimestamp: Instant
    )

    // 3. Основной Transaction метод
    @Transaction
    open suspend fun markMessageAsError(
        containerId: Uuid,
        slotId: Uuid, // Добавили slotId
        status: MessageStatus,
        syncAction: SyncAction,
        serverTimestamp: Instant
    ) {
        // Обновляем контейнер (ставим ошибку и NONE)
        updateContainerErrorStatus(
            containerId = containerId,
            status = status,
            syncAction = syncAction,
            serverTimestamp = serverTimestamp
        )

        // Обновляем слот (ставим NONE)
        updateSlotSyncStatus(
            slotId = slotId,
            syncAction = syncAction,
            serverTimestamp = serverTimestamp
        )
    }

    @Transaction
    @Query(
        """
        SELECT * FROM message_slots 
        WHERE chat_variant_id = :variantId 
        AND sync_action != :deletedAction
        ORDER BY created_at DESC 
        LIMIT :limit
    """
    )
    abstract fun getChatHistoryFlow(
        variantId: Uuid,
        limit: Int,
        deletedAction: SyncAction = SyncAction.DELETE
    ): Flow<List<SlotWithContainers>>

    @Query(
        """
        UPDATE message_slots 
        SET active_container_id = :containerId, 
            sync_action = :syncActionUpdate,
            updated_at = :now
        WHERE id = :slotId
    """
    )
    abstract suspend fun updateActiveContainer(
        slotId: Uuid,
        containerId: Uuid,
        now: Instant,
        syncActionUpdate: SyncAction = SyncAction.UPDATE
    )

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertSlots(slots: List<MessageSlotEntity>)

    @Update
    protected abstract suspend fun _update_insertSlots(slots: List<MessageSlotEntity>)

    @Transaction
    open suspend fun insertSlots(slots: List<MessageSlotEntity>) {
        _insert_insertSlots(slots)
        _update_insertSlots(slots)
    }

    @Transaction
    open suspend fun saveChatHistoryFlat(
        slots: List<MessageSlotEntity>,
        containers: List<MessageContainerEntity>,
        contents: List<MessageContentEntity>
    ) {
        insertSlots(slots)
        insertContainers(containers)
        insertContents(contents)
    }

    @Query("SELECT * FROM message_slots WHERE chat_variant_id = :variantId ORDER BY created_at DESC LIMIT 1")
    abstract suspend fun getNewestSlot(variantId: Uuid): MessageSlotEntity?

    @Query("DELETE FROM message_slots WHERE chat_variant_id = :variantId")
    abstract suspend fun clearHistory(variantId: Uuid)

    @Query("SELECT created_at FROM message_slots WHERE chat_variant_id = :variantId ORDER BY created_at DESC LIMIT 1")
    abstract suspend fun getLastMessageTime(variantId: Uuid): Instant?

    @Query("UPDATE message_contents SET content = :content WHERE container_id = :containerId")
    abstract suspend fun updateContentText(containerId: Uuid, content: String)

    @Query("UPDATE message_contents SET content = :content WHERE container_id = :containerId AND avalon_message_content_type = :type")
    abstract suspend fun updateContentByType(
        containerId: Uuid,
        content: String,
        type: AvalonMessageContentType
    )

    @Query(
        """
        UPDATE message_containers 
        SET 
            sync_action = :syncAction,
            updated_at = COALESCE(:updatedAt, updated_at)
        WHERE id = :containerId
    """
    )
    abstract suspend fun updateContainerState(
        containerId: Uuid,
        syncAction: SyncAction,
        updatedAt: Instant?
    )

    @Query(
        """
        UPDATE chat_session_entity 
        SET 
            last_message_content = :content, 
            updated_at = :updatedAt
        WHERE id = :sessionId
    """
    )
    abstract suspend fun updateSessionPreviewContentOnly(
        sessionId: Uuid,
        content: String,
        updatedAt: Instant?
    )

    // 2. ТРАНЗАКЦИЯ: Обработка события обновления
    @Transaction
    open suspend fun handleMessageUpdateEvent(
        containerId: Uuid,
        content: String,
        systemInstruction: String?,
        syncAction: SyncAction,
        updatedAt: Instant?,
        sessionId: Uuid,
        isLastMessage: Boolean
    ) {

        updateContentByType(containerId, content, AvalonMessageContentType.TEXT)
        if (systemInstruction != null) {
            updateContentByType(
                containerId,
                systemInstruction,
                AvalonMessageContentType.MESSAGE_SYSTEM_INSTRUCTION
            )
        }
        updateContainerState(containerId, syncAction, updatedAt)

        if (isLastMessage) {
            updateSessionPreviewContentOnly(
                sessionId = sessionId,
                content = content,
                updatedAt = updatedAt
            )
        }
    }

    @Transaction
    open suspend fun updateLocalMessageTextAndSyncState(
        containerId: Uuid,
        content: String,
        syncAction: SyncAction,
        updatedAt: Instant?
    ) {
        updateContentText(containerId, content)
        updateContainerState(containerId, syncAction, updatedAt)

    }

    // --- SOFT DELETE ---
    @Query("UPDATE message_slots SET sync_action = :deleteAction WHERE id IN (:ids)")
    abstract suspend fun markSlotsAsDeleted(
        ids: List<Uuid>,
        deleteAction: SyncAction = SyncAction.DELETE
    )

    @Transaction
    open suspend fun executeSmartSoftDelete(ids: List<Uuid>) {
        deleteLocalOnlySlots(ids)
        markSyncedSlotsAsDeleted(ids)
    }

    @Query("DELETE FROM message_slots WHERE id IN (:ids) AND sync_action = :syncActionCreate")
    abstract suspend fun deleteLocalOnlySlots(
        ids: List<Uuid>,
        syncActionCreate: SyncAction = SyncAction.CREATE
    )

    @Query("UPDATE message_slots SET sync_action = :syncActionDelete WHERE id IN (:ids) AND sync_action != :syncActionCreate")
    abstract suspend fun markSyncedSlotsAsDeleted(
        ids: List<Uuid>,
        syncActionCreate: SyncAction = SyncAction.CREATE,
        syncActionDelete: SyncAction = SyncAction.DELETE
    )

    @Query("SELECT * FROM message_containers WHERE id IN (:ids)")
    abstract suspend fun getContainersByIds(ids: List<Uuid>): List<MessageContainerEntity>

    @Query("SELECT * FROM message_slots WHERE id IN (:ids)")
    abstract suspend fun getSlotsByIds(ids: List<Uuid>): List<MessageSlotEntity>

    @Query("SELECT id FROM message_containers WHERE id IN (:candidateIds) AND sync_action != :cleanState")
    abstract suspend fun getDirtyContainerIds(
        candidateIds: List<Uuid>,
        cleanState: SyncAction = SyncAction.NONE
    ): List<Uuid>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun _insert_insertContainer(container: MessageContainerEntity)

    @Update
    protected abstract suspend fun _update_insertContainer(container: MessageContainerEntity)

    @Transaction
    open suspend fun insertContainer(container: MessageContainerEntity) {
        _insert_insertContainer(container)
        _update_insertContainer(container)
    }

    @Query("UPDATE message_slots SET active_container_id = :containerId, updated_at = :updatedAt WHERE id = :slotId")
    abstract suspend fun updateSlotActiveContainer(
        slotId: Uuid,
        containerId: Uuid,
        updatedAt: Instant
    )

    @Transaction
    open suspend fun insertContainerIfSlotExists(
        container: MessageContainerEntity,
        contents: List<MessageContentEntity>
    ) {
        val exists = slotExists(container.slotId)

        if (!exists) {
            // Если слота нет — просто выходим. Ничего не пишем, ошибок не кидаем.
            return
        }

        insertContainer(container)
        insertContents(contents)

        updateSlotActiveContainer(
            slotId = container.slotId,
            containerId = container.id,
            updatedAt = container.createdAt
        )
    }

    @Query("SELECT MAX(created_at) FROM message_slots WHERE chat_variant_id = :variantId")
    abstract suspend fun getLatestSlotCreatedAt(variantId: Uuid): Instant?

    @Query(
        """
        SELECT 
            slots.id as slotId,
            containers.role as role
        FROM message_slots AS slots
        INNER JOIN message_containers AS containers ON slots.active_container_id = containers.id
        WHERE slots.chat_variant_id = :variantId
          AND slots.sync_action != :deletedAction
        ORDER BY slots.created_at DESC
        LIMIT 1
    """
    )
    abstract fun getLastSlotInfoFlow(
        variantId: Uuid,
        deletedAction: SyncAction = SyncAction.DELETE
    ): Flow<SlotInfo?>

    @Query(
        """
        UPDATE message_slots 
        SET 
            active_container_id = :newContainerId,
            sync_action = :syncAction,
            updated_at = :updatedAt
        WHERE id = :slotId
    """
    )
    abstract suspend fun updateSlotActiveContainer(
        slotId: Uuid,
        newContainerId: Uuid,
        syncAction: SyncAction,
        updatedAt: Instant
    )

    @Query("SELECT updated_at FROM message_slots WHERE id = :slotId")
    abstract suspend fun getSlotUpdatedAt(slotId: Uuid): Instant?

    @Query("SELECT role FROM message_containers WHERE id = :containerId")
    abstract suspend fun getContainerRole(containerId: Uuid): MessageRole?

    @Query(
        """
        UPDATE chat_session_entity 
        SET 
            last_message_content = :content,
            last_message_type = :type,
            last_message_role = :role,
            last_message_time = :messageTime,
            updated_at = :updatedAt
        WHERE id = (
            SELECT chat_session_id 
            FROM chat_variant_entity 
            WHERE id = (SELECT chat_variant_id FROM message_slots WHERE id = :slotId)
        )
    """
    )
    abstract suspend fun updateSessionPreview(
        slotId: Uuid,
        content: String,
        type: AvalonMessageContentType,
        role: MessageRole,
        messageTime: Instant,
        updatedAt: Instant
    )

    @Transaction
    open suspend fun switchActiveMessageAndRefreshSession(
        slotId: Uuid,
        newActiveMessageId: Uuid,
        content: String,
        contentType: AvalonMessageContentType,
        safeUpdatedAt: Instant
    ) {
        updateSlotActiveContainer(
            slotId = slotId,
            newContainerId = newActiveMessageId,
            syncAction = SyncAction.UPDATE,
            updatedAt = safeUpdatedAt
        )

        val role = getContainerRole(newActiveMessageId) ?: MessageRole.MODEL

        updateSessionPreview(
            slotId = slotId,
            content = content,
            type = contentType,
            role = role,
            messageTime = safeUpdatedAt,
            updatedAt = safeUpdatedAt
        )
    }

    @Query(
        """
        SELECT content, avalon_message_content_type 
        FROM message_contents 
        WHERE container_id = :containerId 
        ORDER BY order_index ASC 
        LIMIT 1
    """
    )
    abstract suspend fun getContainerContentPreview(containerId: Uuid): ContentPreviewDto?

    @Query(
        """
        UPDATE message_slots 
        SET 
            active_container_id = :newContainerId,
            sync_action = :syncAction, 
            updated_at = :updatedAt
        WHERE id = :slotId
    """
    )
    abstract suspend fun updateSlotActiveContainerIfExists(
        slotId: Uuid,
        newContainerId: Uuid,
        updatedAt: Instant,
        syncAction: SyncAction
    )

    @Transaction
    open suspend fun processActiveContainerEvent(
        slotId: Uuid,
        sessionId: Uuid,
        newContainerId: Uuid,
        content: String,
        contentType: AvalonMessageContentType,
        role: MessageRole,
        serverUpdatedAt: Instant,
        syncAction: SyncAction
    ) {
        updateSlotActiveContainerIfExists(
            slotId = slotId,
            newContainerId = newContainerId,
            updatedAt = serverUpdatedAt,
            syncAction = syncAction
        )

        updateSessionPreviewDirectly(
            sessionId = sessionId,
            content = content,
            type = contentType,
            role = role,
            messageTime = serverUpdatedAt,
            updatedAt = serverUpdatedAt
        )
    }

    @Query("SELECT EXISTS(SELECT 1 FROM message_slots WHERE id = :slotId)")
    abstract suspend fun slotExists(slotId: Uuid): Boolean

    @Transaction
    open suspend fun handleNewContainerEvent(
        sessionId: Uuid,
        container: MessageContainerEntity,
        contents: List<MessageContentEntity>,
        previewContent: String,
        previewType: AvalonMessageContentType
    ) {
        updateSessionPreviewDirectly(
            sessionId = sessionId,
            content = previewContent,
            type = previewType,
            role = container.role,
            messageTime = container.createdAt,
            updatedAt = container.createdAt
        )

        val exists = slotExists(container.slotId)

        if (exists) {
            insertContainer(container)
            insertContents(contents)

            updateSlotActiveContainer(
                slotId = container.slotId,
                containerId = container.id,
                updatedAt = container.createdAt
            )
        }
    }

    @Query("DELETE FROM message_slots WHERE id IN (:slotIds)")
    abstract suspend fun hardDeleteSlotsPermanently(slotIds: List<Uuid>)

    @Query(
        """
        UPDATE chat_session_entity 
        SET 
            last_message_content = :content,
            last_message_type = :type,
            last_message_role = :role,
            last_message_time = :messageTime,
            updated_at = :updatedAt
        WHERE id = :sessionId
    """
    )
    abstract suspend fun updateSessionPreviewDirectly(
        sessionId: Uuid,
        content: String?,
        type: AvalonMessageContentType?,
        role: MessageRole?,
        messageTime: Instant?,
        updatedAt: Instant
    )

    @Transaction
    open suspend fun handleMessageDeletionEvent(
        slotIds: List<Uuid>,
        sessionId: Uuid,
        previewContent: String?,
        previewType: AvalonMessageContentType?,
        previewRole: MessageRole?,
        previewTime: Instant?,
        eventTime: Instant
    ) {

        if (slotIds.isNotEmpty()) {
            hardDeleteSlotsPermanently(slotIds)
        }

        updateSessionPreviewDirectly(
            sessionId = sessionId,
            content = previewContent,
            type = previewType,
            role = previewRole,
            messageTime = previewTime,
            updatedAt = eventTime
        )
    }

    @Query(
        """
        SELECT COUNT(s.id) > 0 
        FROM message_slots AS s
        INNER JOIN message_containers AS c ON s.id = c.slot_id
        WHERE s.chat_variant_id = :variantId 
          AND s.sync_action != :syncAction
          AND (
              c.status = :pendingStatus 
              OR c.status = :sendingStatus 
              OR c.status = :sentStatus
          )
    """
    )
    abstract suspend fun variantHasUnsyncedMessages(
        variantId: Uuid,
        syncAction: SyncAction = SyncAction.NONE,
        pendingStatus: MessageStatus = MessageStatus.PENDING,
        sendingStatus: MessageStatus = MessageStatus.SENDING,
        sentStatus: MessageStatus = MessageStatus.SENT
    ): Boolean

    @Query("UPDATE message_containers SET user_reaction = :reaction, updated_at = :updatedAt WHERE id = :containerId")
    abstract suspend fun updateMessageUserReaction(
        containerId: Uuid,
        reaction: String?,
        updatedAt: Instant
    )

    @Query("UPDATE message_containers SET character_reaction = :reaction, updated_at = :updatedAt WHERE id = :containerId")
    abstract suspend fun updateMessageCharacterReaction(
        containerId: Uuid,
        reaction: String?,
        updatedAt: Instant
    )

    @Query("UPDATE message_containers SET is_read = 1 WHERE id IN (:containerIds)")
    abstract suspend fun markMessagesAsRead(containerIds: List<Uuid>)

    @Query(
        """
        UPDATE chat_variant_entity 
        SET unread_count = MAX(0, unread_count - (
            SELECT COUNT(*) 
            FROM message_containers AS mc
            INNER JOIN message_slots AS ms ON mc.slot_id = ms.id
            WHERE ms.chat_variant_id = chat_variant_entity.id
              AND mc.id IN (:containerIds)
              AND mc.role = 'MODEL'
              AND mc.is_read = 0
        ))
        WHERE id IN (
            SELECT DISTINCT ms.chat_variant_id
            FROM message_containers AS mc
            INNER JOIN message_slots AS ms ON mc.slot_id = ms.id
            WHERE mc.id IN (:containerIds)
        )
    """
    )
    abstract suspend fun decrementUnreadCountByContainers(containerIds: List<Uuid>)

    @Transaction
    open suspend fun markMessagesAsReadWithUnreadCountUpdate(containerIds: List<Uuid>) {
        decrementUnreadCountByContainers(containerIds)
        markMessagesAsRead(containerIds)
    }

    @Query("UPDATE message_containers SET show_outreach_feedback = 0 WHERE id = :containerId")
    abstract suspend fun hideOutreachFeedbackForContainer(containerId: Uuid)

    @Query(
        """
    SELECT MAX(updated_at) 
    FROM message_slots 
    WHERE chat_variant_id = :variantId 
      AND sync_action = :syncedAction 
"""
    )
    abstract suspend fun getLastServerUpdateTime(
        variantId: Uuid,
        syncedAction: SyncAction = SyncAction.NONE
    ): Instant?

    @Query("SELECT * FROM message_slots WHERE chat_variant_id = :variantId ORDER BY created_at ASC LIMIT 1")
    abstract suspend fun getOldestSlot(variantId: Uuid): MessageSlotEntity?

    @Query("SELECT COUNT(id) FROM message_slots WHERE chat_variant_id = :variantId")
    abstract suspend fun getSlotsCount(variantId: Uuid): Int

    @Query(
        """
        SELECT mc.content 
        FROM message_contents mc
        INNER JOIN message_containers mcon ON mc.container_id = mcon.id
        INNER JOIN message_slots ms ON mcon.slot_id = ms.id
        INNER JOIN chat_variant_entity cv ON ms.chat_variant_id = cv.id
        INNER JOIN chat_session_entity cs ON cv.chat_session_id = cs.id
        WHERE cs.character_id = :characterId
          AND mc.avalon_message_content_type = :imageType
          AND ms.sync_action != :deletedAction
        ORDER BY mcon.created_at DESC
    """
    )
    abstract fun getSharedMediaFlow(
        characterId: Uuid,
        imageType: AvalonMessageContentType = AvalonMessageContentType.IMAGE,
        deletedAction: SyncAction = SyncAction.DELETE
    ): Flow<List<String>>

    @Query(
        """
        SELECT mc.id 
        FROM message_containers AS mc
        INNER JOIN message_slots AS ms ON mc.slot_id = ms.id
        WHERE ms.chat_variant_id = :variantId
          AND mc.role = :modelRole
          AND mc.is_read = 0
    """
    )
    abstract suspend fun getUnreadModelContainerIds(
        variantId: Uuid,
        modelRole: MessageRole = MessageRole.MODEL
    ): List<Uuid>
}
