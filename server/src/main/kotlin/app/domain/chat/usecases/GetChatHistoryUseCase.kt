package app.domain.chat.usecases

import org.koin.core.annotation.Factory
import app.data.db.tables.chatSessions.ChatSessions
import app.domain.utils.ErrorType
import com.ensodai.shared.avalon.chat.services.MessageContentCodec
import com.ensodai.shared.avalon.chat.services.processImageContentUrl
import app.data.db.tables.chat.MessageContainers
import app.data.db.tables.chat.MessageContents
import app.data.db.tables.chat.MessageSlots
import app.utils.dbQuery
import app.data.db.tables.chat.MessageReadReceiptsTable
import app.data.db.tables.aiCharacter.OutreachHistoryTable
import app.data.db.tables.aiCharacter.FeedbackStatus
import com.ensodai.shared.avalon.chat.models.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import app.data.repository.ChatRepository
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Factory
class GetChatHistoryUseCase(
    private val chatRepository: ChatRepository
) {
    suspend fun execute(
        request: GetChatHistoryRequest,
        userId: Uuid
    ): PaginatedChatResponse = dbQuery {
        val readContainerIds = MessageReadReceiptsTable
            .select(MessageReadReceiptsTable.messageContainerId)
            .where { MessageReadReceiptsTable.userId eq userId }
            .map { it[MessageReadReceiptsTable.messageContainerId].value }
            .toSet()

        val chatSessionRow = ChatSessions
            .selectAll()
            .where { (ChatSessions.id eq request.chatSessionId) and (ChatSessions.userId eq userId) }
            .singleOrNull()
            ?: throw Exception("Chat session not found or access denied")

        val characterId = chatSessionRow[ChatSessions.characterId].value
        val pendingOutreachSentAt = OutreachHistoryTable
            .select(OutreachHistoryTable.sentAt)
            .where { (OutreachHistoryTable.characterId eq characterId) and (OutreachHistoryTable.userId eq userId) and (OutreachHistoryTable.feedbackStatus eq FeedbackStatus.PENDING) }
            .singleOrNull()
            ?.get(OutreachHistoryTable.sentAt)

        val dbVariantId = chatSessionRow[ChatSessions.activeVariantId]?.value
        val targetVariantId: Uuid = request.variantId ?: dbVariantId
        ?: return@dbQuery PaginatedChatResponse(emptyList(), null)

        if (request.cursor == null) {
            chatRepository.ensureGreetingSlotExistsInternal(targetVariantId, characterId)
        }

        val slotsQuery = MessageSlots
            .selectAll()
            .where {
                (MessageSlots.chatVariantId eq targetVariantId) and
                        (MessageSlots.deletedAt.isNull())
            }

        val requestCursor = request.cursor
        if (requestCursor != null) {
            slotsQuery.andWhere { MessageSlots.createdAt less requestCursor }
        }

        val slotsRows = slotsQuery
            .orderBy(MessageSlots.createdAt to SortOrder.DESC)
            .limit(request.limit)
            .toList()
        if (slotsRows.isEmpty()) {
            return@dbQuery PaginatedChatResponse(emptyList(), null)
        }

        val slotIds = slotsRows.map { it[MessageSlots.id].value }

        val dataRows = MessageContainers
            .leftJoin(MessageContents)
            .selectAll()
            .where {
                (MessageContainers.slotId inList slotIds) and
                        (MessageContainers.deletedAt.isNull())
            }
            .orderBy(MessageContainers.createdAt to SortOrder.ASC, MessageContents.orderIndex to SortOrder.ASC)
            .toList()

        val slotsMap = dataRows
            .groupBy { it[MessageContainers.slotId].value }

        val resultSlots: List<MessageSlotResponse> = slotsRows.map { slotRow ->
            val sId = slotRow[MessageSlots.id].value
            val slotDataRows = slotsMap[sId] ?: emptyList()

            val containers = slotDataRows
                .groupBy { it[MessageContainers.id].value }
                .map { (cId, cRows) ->
                    val firstRow = cRows.first()

                    val allContents = cRows.mapNotNull { row ->
                        if (row.getOrNull(MessageContents.id) == null) return@mapNotNull null

                        val type = row[MessageContents.type]
                        val rawContent = row[MessageContents.content]
                        val finalContent = if (type == AvalonMessageContentType.IMAGE) {
                            MessageContentCodec.processImageContentUrl(rawContent)
                        } else {
                            rawContent
                        }

                        MessageContentResponse(
                            id = row[MessageContents.id].value,
                            type = type,
                            content = finalContent,
                            orderIndex = row[MessageContents.orderIndex]
                        )
                    }

                    val hasImage = allContents.any { it.type == AvalonMessageContentType.IMAGE }
                    val hasDescription = allContents.any { it.type == AvalonMessageContentType.GENERATED_IMAGE_DESCRIPTION }

                    val contents = if (hasImage && hasDescription) {
                        allContents.filter { it.type != AvalonMessageContentType.GENERATED_IMAGE_DESCRIPTION }
                    } else {
                        allContents
                    }

                    val role = firstRow[MessageContainers.role]
                    val isRead = if (role == MessageRole.USER) true else (cId in readContainerIds)
                    val showOutreachFeedback = if (role == MessageRole.MODEL && pendingOutreachSentAt != null) {
                        val diff = kotlin.math.abs(firstRow[MessageContainers.createdAt].toEpochMilliseconds() - pendingOutreachSentAt.toEpochMilliseconds())
                        diff < 5000L
                    } else false

                    MessageContainerResponse(
                        containerId = cId,
                        slotId = sId,
                        role = role,
                        rarity = firstRow[MessageContainers.rarity],
                        status = MessageStatus.SENT,
                        userReaction = firstRow[MessageContainers.userReaction],
                        characterReaction = firstRow[MessageContainers.characterReaction],
                        isRead = isRead,
                        showOutreachFeedback = showOutreachFeedback,
                        createdAt = firstRow[MessageContainers.createdAt],
                        updatedAt = firstRow[MessageContainers.updatedAt],
                        contents = contents
                    )
                }

            MessageSlotResponse(
                id = sId,
                chatVariantId = slotRow[MessageSlots.chatVariantId].value,
                chatSessionId = request.chatSessionId,
                activeContainerId = slotRow[MessageSlots.activeMessageId]?.value,
                createdAt = slotRow[MessageSlots.createdAt],
                updatedAt = slotRow[MessageSlots.updatedAt],
                containers = containers
            )
        }

        val nextCursor: Instant? = resultSlots.lastOrNull()?.createdAt

        PaginatedChatResponse(resultSlots, nextCursor)
    }
}
