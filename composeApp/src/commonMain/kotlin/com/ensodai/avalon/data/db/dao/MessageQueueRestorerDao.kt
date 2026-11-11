package com.ensodai.avalon.data.db.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.avalon.data.db.models.messageQueueRestorerDao.CreateMessageTuple
import com.ensodai.avalon.data.db.models.messageQueueRestorerDao.DeleteMessageTuple
import com.ensodai.avalon.data.db.models.messageQueueRestorerDao.SwipeMessageTuple
import com.ensodai.avalon.data.db.models.messageQueueRestorerDao.UpdateMessageTuple
import com.ensodai.shared.avalon.chat.models.MessageStatus

@Dao
interface MessageQueueRestorerDao {

    // --- 1. CREATES ---
    // Берем контейнеры CREATE/PENDING.
    // ИСКЛЮЧАЕМ, если родительский Слот стоит на удалении (sync_action = 'DELETE')
    // ИСКЛЮЧАЕМ, если родительская Сессия стоит на удалении (sync_state = 'DELETE')
    @Transaction
    @Query(
        """
        SELECT 
            cnt.id as container_id,
            cnt.slot_id,
            con.id as content_id,
            con.content as text_content,
            s.chat_variant_id,
            v.chat_session_id,
            ses.character_id
        FROM message_containers cnt
        INNER JOIN message_slots s ON cnt.slot_id = s.id
        INNER JOIN chat_variant_entity v ON s.chat_variant_id = v.id
        INNER JOIN chat_session_entity ses ON v.chat_session_id = ses.id
        INNER JOIN message_contents con ON cnt.id = con.container_id
        WHERE 
            cnt.status = :statusPending
            AND con.avalon_message_content_type = :contentTypeText
            
            -- Защита от отправки создания для удаленных контекстов
            AND s.sync_action != :syncActionDelete
            AND ses.sync_state != :syncActionDelete
            
        ORDER BY cnt.created_at ASC
    """
    )
    abstract suspend fun getPendingCreates(
        statusPending: MessageStatus = MessageStatus.PENDING,
        syncActionDelete: SyncAction = SyncAction.DELETE,
        contentTypeText: AvalonMessageContentType = AvalonMessageContentType.TEXT
    ): List<CreateMessageTuple>


    // --- 2. UPDATES ---
    // Стратегия: "Белый список".
    // Мы отправляем Edit ТОЛЬКО для сообщений, которые сервер уже знает (SENT).
    //
    // Если статус PENDING -> его заберет getPendingCreates (как новое).
    // Если статус FAILED -> мы его игнорируем (Restorer не должен долбить ошибку).
    // Юзер увидит красный знак, исправит, нажмет сохранить -> статус станет PENDING/SENT -> попадет в очередь.
    @Transaction
    @Query(
        """
        SELECT 
            cnt.id as container_id,
            cnt.slot_id,
            con.content as text_content,
            s.chat_variant_id,
            v.chat_session_id
        FROM message_containers cnt
        INNER JOIN message_slots s ON cnt.slot_id = s.id
        INNER JOIN chat_variant_entity v ON s.chat_variant_id = v.id
        INNER JOIN message_contents con ON cnt.id = con.container_id
        WHERE 
            cnt.sync_action = :syncActionUpdate
            AND cnt.status = :statusSent
            AND con.avalon_message_content_type = :contentTypeText
            
            AND s.sync_action != :syncActionDelete
            
        ORDER BY cnt.updated_at ASC
    """
    )
    abstract suspend fun getPendingUpdates(
        syncActionUpdate: SyncAction = SyncAction.UPDATE,
        statusSent: MessageStatus = MessageStatus.SENT, // <-- Ключевой фильтр
        syncActionDelete: SyncAction = SyncAction.DELETE,
        contentTypeText: AvalonMessageContentType = AvalonMessageContentType.TEXT
    ): List<UpdateMessageTuple>


    // --- 3. SWIPES ---
    // Тут сложнее фильтровать по статусу контейнера, так как свайп - это свойство слота.
    // Но обычно свайпы не имеют статуса FAILED (это редкий кейс).
    // Оставляем проверку на UPDATE.
    @Query(
        """
        SELECT 
            s.id as slot_id,
            s.active_container_id as new_container_id,
            s.chat_variant_id,
            v.chat_session_id
        FROM message_slots s
        INNER JOIN chat_variant_entity v ON s.chat_variant_id = v.id
        WHERE 
            s.sync_action = :syncActionUpdate
            AND s.active_container_id IS NOT NULL
            AND s.sync_action != :syncActionDelete
    """
    )
    abstract suspend fun getPendingSwipes(
        syncActionUpdate: SyncAction = SyncAction.UPDATE,
        syncActionDelete: SyncAction = SyncAction.DELETE
    ): List<SwipeMessageTuple>


    // --- 4. DELETES ---
    // Отправляем DELETE, только если сервер знает об этих сообщениях (есть SENT контейнеры).
    // FAILED здесь не важен, так как при ошибке удаления мы делаем Hard Delete локально.
    @Query(
        """
        SELECT 
            s.id as slot_id,
            s.chat_variant_id,
            v.chat_session_id
        FROM message_slots s
        INNER JOIN chat_variant_entity v ON s.chat_variant_id = v.id
        WHERE 
            s.sync_action = :syncActionDelete
            AND EXISTS (
                SELECT 1 
                FROM message_containers c 
                WHERE c.slot_id = s.id 
                AND c.status = :statusSent
            )
    """
    )
    abstract suspend fun getPendingDeletes(
        syncActionDelete: SyncAction = SyncAction.DELETE,
        statusSent: MessageStatus = MessageStatus.SENT
    ): List<DeleteMessageTuple>

    // --- CLEANUP ---
    // Чистим "Локальные" удаления.
    // Это слоты, помеченные DELETE, но у которых нет SENT контейнеров.
    // Значит, сервер о них не знает -> удаляем молча.
    @Query(
        """
        DELETE FROM message_slots
        WHERE sync_action = :syncActionDelete
        AND NOT EXISTS (
            SELECT 1 
            FROM message_containers c 
            WHERE c.slot_id = message_slots.id 
            AND c.status = :statusSent
        )
    """
    )
    abstract suspend fun clearLocalOnlyDeletes(
        syncActionDelete: SyncAction = SyncAction.DELETE,
        statusSent: MessageStatus = MessageStatus.SENT
    ): Int
}
