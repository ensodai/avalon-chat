package app.data.repository

import app.domain.models.EditMessageResult
import com.ensodai.shared.avalon.chat.models.*

import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.chat.*
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import app.utils.dbQuery
import app.utils.getUTCTimeNow
import app.utils.logger.WssLogger
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class EditMessageRepository {

    suspend fun updateMessageText(
        userId: Uuid,
        slotId: Uuid,
        containerId: Uuid,
        chatSessionId: Uuid,
        newText: String,
        newMessageSystemInstruction: String?
    ): EditMessageResult = dbQuery {

        // 1. ПРОВЕРКА СЕССИИ
        val sessionExists = ChatSessions.select(ChatSessions.id)
            .where {
                (ChatSessions.id eq chatSessionId) and
                        (ChatSessions.userId eq userId) and
                        (ChatSessions.deletedAt.isNull())
            }
            .count() > 0

        if (!sessionExists) {
            WssLogger.fail("User $userId tried to edit in non-existent session $chatSessionId")
            return@dbQuery EditMessageResult.Error
        }

        // 2. ПРОВЕРКА СЛОТА
        val slotIsAlive = MessageSlots.select(MessageSlots.id)
            .where {
                (MessageSlots.id eq slotId) and
                        (MessageSlots.deletedAt.isNull())
            }
            .count() > 0

        if (!slotIsAlive) {
            WssLogger.info("Slot $slotId is deleted. Marking message as Not Found.")
            return@dbQuery EditMessageResult.NotFound
        }

        // 3. ПРОВЕРКА КОНТЕЙНЕРА
        // Проверяем, что контейнер существует и не удален сам по себе
        val messageExists = MessageContainers
            .select(MessageContainers.id)
            .where {
                (MessageContainers.id eq containerId) and
                        (MessageContainers.deletedAt.isNull())
            }
            .count() > 0

        if (!messageExists) {
            WssLogger.info("Message container $containerId not found or deleted")
            return@dbQuery EditMessageResult.NotFound
        }

        // 4. ОБНОВЛЕНИЕ КОНТЕНТА
        val now = getUTCTimeNow()
        val updatedRows = MessageContents.update({
            (MessageContents.containerId eq containerId) and
                    (MessageContents.type eq AvalonMessageContentType.TEXT)
        }) {
            it[MessageContents.content] = newText
        }

        if (updatedRows == 0) {
            return@dbQuery EditMessageResult.Error
        }

        if (newMessageSystemInstruction == null) {
            MessageContents.update({
                (MessageContents.containerId eq containerId) and
                        (MessageContents.type eq AvalonMessageContentType.MESSAGE_SYSTEM_INSTRUCTION) and
                        (MessageContents.deletedAt.isNull())
            }) {
                it[deletedAt] = now
                it[content] = ""
                it[updatedAt] = now
            }
        } else {

            val updatedInstructionRows = MessageContents.update({
                (MessageContents.containerId eq containerId) and
                        (MessageContents.type eq AvalonMessageContentType.MESSAGE_SYSTEM_INSTRUCTION)
            }) {
                it[content] = newMessageSystemInstruction
                it[deletedAt] = null
                it[updatedAt] = now
            }

            if (updatedInstructionRows == 0) {
                MessageContents.insert {
                    it[id] = Uuid.random()
                    it[this.containerId] = containerId
                    it[type] = AvalonMessageContentType.MESSAGE_SYSTEM_INSTRUCTION
                    it[content] = newMessageSystemInstruction
                    it[orderIndex] = 1
                    it[createdAt] = now
                    it[updatedAt] = now
                    it[deletedAt] = null
                }
            }
        }

        // 5. ОБНОВЛЕНИЕ МЕТАДАННЫХ
        MessageContainers.update({ MessageContainers.id eq containerId }) {
            it[updatedAt] = now
        }

        // Обновляем updatedAt у слота, чтобы сработала дельта-синхронизация на клиенте
        MessageSlots.update({ MessageSlots.id eq slotId }) {
            it[updatedAt] = now
        }

        val updatedSessionRows = ChatSessions.update({
            (ChatSessions.id eq chatSessionId) and
                    (ChatSessions.lastMessageId eq containerId)
        }) {
            val previewText = if (newText.length > 200) newText.take(200) + "..." else newText
            it[lastMessagePreview] = previewText
        }

        val isLastMessage = updatedSessionRows > 0

        EditMessageResult.Success(
            ServerMessage.MessageUpdated(
                containerId = containerId,
                newText = newText,
                newMessageSystemInstruction = newMessageSystemInstruction,
                updatedAt = now,
                sessionId = chatSessionId,
                isLastMessage = isLastMessage
            )
        )
    }

    // Вспомогательный метод оставляем как есть
    suspend fun getSessionPreviewInfo(sessionId: Uuid): SessionPreviewInfo? = dbQuery {
        ChatSessions.select(
            ChatSessions.lastMessagePreview,
            ChatSessions.lastMessageType,
            ChatSessions.lastMessageRole,
            ChatSessions.lastMessageAt
        )
            .where { ChatSessions.id eq sessionId }
            .map {
                SessionPreviewInfo(
                    previewContent = it[ChatSessions.lastMessagePreview],
                    previewType = it[ChatSessions.lastMessageType],
                    previewRole = it[ChatSessions.lastMessageRole],
                    previewTime = it[ChatSessions.lastMessageAt]
                )
            }
            .singleOrNull()
    }
}

data class SessionPreviewInfo(
    val previewContent: String?,
    val previewType: AvalonMessageContentType?,
    val previewRole: MessageRole?,
    val previewTime: Instant?
)