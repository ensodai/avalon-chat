package app.domain.syncmanager

import com.ensodai.shared.avalon.chat.models.*
import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.chat.MessageReadReceiptsTable
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class ChatSessionSyncService {

    fun fetchChanges(userId: Uuid, since: Instant): SyncEntityChanges<ChatSessionPreviewResponse> {
        val isFirstSync = since.toLocalDateTime(TimeZone.UTC).year < 2000
        // 1. ПЛОСКИЙ ЗАПРОС (NO JOINS!)
        val rows = ChatSessions
            .select(
                ChatSessions.id,
                ChatSessions.characterId,
                ChatSessions.isArchived,
                ChatSessions.activeVariantId,
                ChatSessions.updatedAt,
                ChatSessions.createdAt,
                // Все данные сообщения уже тут:
                ChatSessions.lastMessageId,
                ChatSessions.lastMessagePreview,
                ChatSessions.lastMessageType,
                ChatSessions.lastMessageAt,
                ChatSessions.lastMessageRole
            )
            .where {
                (ChatSessions.userId eq userId) and
                        (ChatSessions.updatedAt greater since) and
                        (ChatSessions.deletedAt.isNull())
            }
            .orderBy(ChatSessions.updatedAt to SortOrder.DESC)
            .toList()

        // 2. Получение прочитанных сообщений для превью последнего сообщения
        val lastMsgIds = rows.mapNotNull { row ->
            val role = row.getOrNull(ChatSessions.lastMessageRole)
            val msgId = row.getOrNull(ChatSessions.lastMessageId)?.value
            if (role != MessageRole.USER && msgId != null) msgId else null
        }.distinct()

        val readContainerIds = if (lastMsgIds.isNotEmpty()) {
            MessageReadReceiptsTable
                .selectAll()
                .where { (MessageReadReceiptsTable.userId eq userId) and (MessageReadReceiptsTable.messageContainerId inList lastMsgIds) }
                .map { it[MessageReadReceiptsTable.messageContainerId].value }
                .toSet()
        } else {
            emptySet()
        }

        // 3. Моментальный маппинг
        val updated = rows.map { row ->

            // Валидация целостности превью
            val lastMsgId = row.getOrNull(ChatSessions.lastMessageId)?.value
            val previewText = row.getOrNull(ChatSessions.lastMessagePreview)
            val msgType = row.getOrNull(ChatSessions.lastMessageType)
            val msgAt = row.getOrNull(ChatSessions.lastMessageAt)
            val msgRole = row.getOrNull(ChatSessions.lastMessageRole)

            val lastMessageDto = if (lastMsgId != null && previewText != null && msgType != null && msgAt != null && msgRole != null) {
                val isRead = if (msgRole == MessageRole.USER) true else (lastMsgId in readContainerIds)
                ChatPreviewMessageResponse(
                    id = lastMsgId,
                    type = msgType,
                    content = previewText,
                    role = msgRole,
                    isRead = isRead,
                    createdAt = msgAt
                )
            } else {
                null
            }

            ChatSessionPreviewResponse(
                id = row[ChatSessions.id].value,
                characterId = row[ChatSessions.characterId].value,
                isArchived = row[ChatSessions.isArchived],
                updatedAt = row[ChatSessions.updatedAt],
                createdAt = row[ChatSessions.createdAt],
                lastMessage = lastMessageDto,
                activeVariantId = row[ChatSessions.activeVariantId]?.value
            )
        }

        val deleted = if (isFirstSync) {
            emptyList()
        } else {
            ChatSessions
                .select(ChatSessions.id)
                .where {
                    (ChatSessions.userId eq userId) and
                            (ChatSessions.updatedAt greater since) and
                            (ChatSessions.deletedAt.isNotNull())
                }
                .map { it[ChatSessions.id].value }
        }

        return SyncEntityChanges(updated, deleted)
    }
}
