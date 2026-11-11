package app.data.repository

import app.data.db.tables.chatSessions.ChatSessions
import app.data.db.tables.chatSessions.ChatVariants
import app.data.db.tables.chat.MessageContainers
import app.data.db.tables.chat.MessageContents
import app.data.db.tables.chat.MessageSlots
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import app.utils.dbQuery
import app.utils.getUTCTimeNow
import app.utils.logger.WssLogger
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single


@Single
class ChangeContainerRepository() {

    suspend fun setActiveContainer(
        userId: Uuid,
        slotId: Uuid,
        containerId: Uuid
    ): ServerMessage.ActiveContainerChanged? = dbQuery {
        val now = getUTCTimeNow()

        val isValidRequest = MessageContainers
            .join(MessageSlots, JoinType.INNER, MessageContainers.slotId, MessageSlots.id)
            .join(ChatVariants, JoinType.INNER, MessageSlots.chatVariantId, ChatVariants.id)
            .join(ChatSessions, JoinType.INNER, ChatVariants.chatSessionId, ChatSessions.id)
            .select(MessageContainers.id)
            .where {
                (MessageContainers.id eq containerId) and
                        (MessageSlots.id eq slotId) and
                        (ChatSessions.userId eq userId)
            }
            .count() > 0

        if (!isValidRequest) {
            WssLogger.fail("User $userId tried to set active container $containerId for slot $slotId (Access Denied)")
            return@dbQuery null
        }

        MessageSlots.update({ MessageSlots.id eq slotId }) {
            it[activeMessageId] = containerId
            it[updatedAt] = now
        }

        val contentRow = MessageContents
            .join(MessageContainers, JoinType.INNER, MessageContents.containerId, MessageContainers.id)
            .select(
                MessageContents.content,
                MessageContents.type,
                MessageContainers.role
            )
            .where { MessageContents.containerId eq containerId }
            .orderBy(MessageContents.orderIndex to SortOrder.ASC)
            .limit(1)
            .singleOrNull()

        if (contentRow == null) {
            WssLogger.fail("Content not found for container $containerId")
            return@dbQuery null
        }

        val finalContent = contentRow[MessageContents.content]
        val finalContentType = contentRow[MessageContents.type]
        val finalRole = contentRow[MessageContainers.role]

        val sessionId = MessageSlots
            .join(ChatVariants, JoinType.INNER, MessageSlots.chatVariantId, ChatVariants.id)
            .select(ChatVariants.chatSessionId)
            .where { MessageSlots.id eq slotId }
            .singleOrNull()
            ?.get(ChatVariants.chatSessionId)
            ?.value

        if (sessionId == null) {
            WssLogger.fail("Session ID not found for slot $slotId")
            return@dbQuery null
        }

        val sessionNeedsUpdate = ChatSessions
            .join(MessageContainers, JoinType.INNER, ChatSessions.lastMessageId, MessageContainers.id)
            .select(ChatSessions.id)
            .where {
                (ChatSessions.id eq sessionId) and
                        (MessageContainers.slotId eq slotId)
            }
            .count() > 0

        if (sessionNeedsUpdate) {
            val previewText = if (finalContent.length > 200) finalContent.take(200) + "..." else finalContent

            ChatSessions.update({ ChatSessions.id eq sessionId }) {
                it[lastMessageId] = containerId
                it[lastMessagePreview] = previewText
                it[lastMessageType] = finalContentType
                it[lastMessageRole] = finalRole
            }
        }

        ServerMessage.ActiveContainerChanged(
            slotId = slotId,
            newContainerId = containerId,
            updatedAt = now,
            content = finalContent,
            contentType = finalContentType,
            role = finalRole,
            sessionId = sessionId
        )
    }
}