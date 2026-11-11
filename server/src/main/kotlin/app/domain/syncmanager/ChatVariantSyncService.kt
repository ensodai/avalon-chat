package app.domain.syncmanager

import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.chatSessions.ChatVariants
import app.data.db.tables.images.ChatVariantImages
import com.ensodai.shared.avalon.chat.models.ChatVariantResponse
import com.ensodai.shared.avalon.chat.models.ChatVariantImageResponse
import app.utils.toServerUrl
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.select
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class ChatVariantSyncService {

    fun fetchChanges(userId: Uuid, since: Instant): SyncEntityChanges<ChatVariantResponse> {

        // 1. Загружаем ОБНОВЛЕННЫЕ варианты
        val variantRows = ChatVariants
            .join(
                otherTable = ChatSessions,
                joinType = JoinType.INNER,
                onColumn = ChatVariants.chatSessionId,
                otherColumn = ChatSessions.id
            )
            .leftJoin(ChatVariantImages)
            .select(
                ChatVariants.id,
                ChatVariants.name,
                ChatVariants.chatSessionId,
                ChatVariants.createdAt,
                ChatVariants.updatedAt,
                ChatVariants.unreadCount,
                ChatVariantImages.id,
                ChatVariantImages.imagePath,
                ChatVariantImages.thumbHash
            )
            .where {
                (ChatSessions.userId eq userId) and
                        (ChatVariants.updatedAt greater since) and
                        (ChatVariants.deletedAt.isNull()) and
                        (ChatSessions.deletedAt.isNull())
            }
            .toList()

        val updatedItems = variantRows.map { row ->
            val imageResp = if (row.getOrNull(ChatVariantImages.id) != null) {
                ChatVariantImageResponse(
                    id = row[ChatVariantImages.id].value,
                    imageUrl = row[ChatVariantImages.imagePath].toServerUrl(),
                    thumbHash = row[ChatVariantImages.thumbHash]
                )
            } else {
                null
            }

            ChatVariantResponse(
                id = row[ChatVariants.id].value,
                name = row[ChatVariants.name],
                image = imageResp,
                sessionId = row[ChatVariants.chatSessionId].value,
                createdAt = row[ChatVariants.createdAt],
                updatedAt = row[ChatVariants.updatedAt],
                unreadCount = row[ChatVariants.unreadCount]
            )
        }

        // 2. Загружаем УДАЛЕННЫЕ варианты
        val isFirstSync = since.toLocalDateTime(TimeZone.UTC).year < 2000
        val deletedIds = if (isFirstSync) {
            emptyList()
        } else {
            ChatVariants
                .join(
                    otherTable = ChatSessions,
                    joinType = JoinType.INNER,
                    onColumn = ChatVariants.chatSessionId,
                    otherColumn = ChatSessions.id
                )
                .select(ChatVariants.id)
                .where {
                    (ChatSessions.userId eq userId) and
                            (ChatVariants.updatedAt greater since) and
                            (ChatVariants.deletedAt.isNotNull()) and // Вариант удален
                            (ChatSessions.deletedAt.isNull())        // А сессия жива
                }
                .map { it[ChatVariants.id].value }
        }

        return SyncEntityChanges(updatedItems, deletedIds)
    }
}
