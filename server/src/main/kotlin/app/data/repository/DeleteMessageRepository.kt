package app.data.repository

import com.ensodai.shared.avalon.chat.models.*

import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.chatSessions.ChatVariants
import app.data.db.tables.chat.*
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import app.utils.dbQuery
import app.utils.getUTCTimeNow
import app.utils.logger.WssLogger
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class DeleteMessageRepository {

    suspend fun deleteMessages(
        userId: Uuid,
        chatSessionId: Uuid,
        chatVariantId: Uuid,
        slotIds: List<Uuid>
    ): ServerMessage.MessagesDeleted? = dbQuery {
        val now = getUTCTimeNow()

        // 1. ВАЛИДАЦИЯ И ФИЛЬТРАЦИЯ
        val validSlotIds = MessageSlots
            .join(ChatVariants, JoinType.INNER, MessageSlots.chatVariantId, ChatVariants.id)
            .join(ChatSessions, JoinType.INNER, ChatVariants.chatSessionId, ChatSessions.id)
            .select(MessageSlots.id)
            .where {
                (MessageSlots.id inList slotIds) and
                        (ChatSessions.userId eq userId) and
                        (ChatSessions.id eq chatSessionId)
            }
            .map { it[MessageSlots.id].value }

        if (validSlotIds.isEmpty()) {
            WssLogger.fail("Delete failed: No valid slots found for user $userId (slotIds=$slotIds, session=$chatSessionId)")
            return@dbQuery null
        }

        // 2. SOFT DELETE
        val updatedRows = MessageSlots.update({ MessageSlots.id inList validSlotIds }) {
            it[deletedAt] = now
            it[updatedAt] = now
        }

        if (updatedRows == 0) return@dbQuery null

        // 3. ПОИСК "НАСЛЕДНИКА" (Нового последнего сообщения)
        val newLastMessageRow = MessageSlots
            .join(MessageContainers, JoinType.INNER, MessageSlots.activeMessageId, MessageContainers.id)
            .join(MessageContents, JoinType.INNER, MessageContainers.id, MessageContents.containerId)
            .select(
                MessageContainers.id,
                MessageContainers.role,
                MessageContents.type,
                MessageContents.content,
                MessageSlots.createdAt
            )
            .where {
                (MessageSlots.chatVariantId eq chatVariantId) and
                        (MessageSlots.deletedAt.isNull()) and // Только живые!
                        (MessageContents.type eq AvalonMessageContentType.TEXT) // Упрощение для превью
            }
            .orderBy(MessageSlots.createdAt to SortOrder.DESC)
            .limit(1)
            .singleOrNull()

        // 4. ПОДГОТОВКА ДАННЫХ ДЛЯ ПРЕВЬЮ
        var finalPreviewContent: String? = null
        var finalPreviewType: AvalonMessageContentType? = null
        var finalPreviewRole: MessageRole? = null
        var finalPreviewTime: Instant? = null
        var finalLastMessageId: Uuid? = null

        if (newLastMessageRow != null) {
            val content = newLastMessageRow[MessageContents.content]

            // Обрезаем для БД и DTO
            finalPreviewContent = if (content.length > 200) content.take(200) + "..." else content
            finalPreviewType = newLastMessageRow[MessageContents.type]
            finalPreviewRole = newLastMessageRow[MessageContainers.role]
            finalPreviewTime = newLastMessageRow[MessageSlots.createdAt]
            finalLastMessageId = newLastMessageRow[MessageContainers.id].value
        }

        // 5. ОБНОВЛЕНИЕ СЕССИИ
        ChatSessions.update({ ChatSessions.id eq chatSessionId }) {
            it[lastMessageId] = finalLastMessageId // UUID или Null
            it[lastMessagePreview] = finalPreviewContent
            it[lastMessageType] = finalPreviewType
            it[lastMessageRole] = finalPreviewRole
            it[lastMessageAt] = finalPreviewTime
        }

        // 6. ВОЗВРАТ ОБОГАЩЕННОГО СОБЫТИЯ
        ServerMessage.MessagesDeleted(
            slotIds = validSlotIds,
            deletedAt = now,
            sessionId = chatSessionId,
            previewContent = finalPreviewContent,
            previewType = finalPreviewType,
            previewRole = finalPreviewRole,
            previewTime = finalPreviewTime
        )
    }
}