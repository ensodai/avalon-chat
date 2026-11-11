package app.domain.chat.usecases

import org.koin.core.annotation.Factory
import app.data.db.tables.chatSessions.ChatSessions
import com.ensodai.shared.avalon.chat.services.MessageContentCodec
import com.ensodai.shared.avalon.chat.services.processImageContentUrl
import app.data.db.tables.chat.MessageContainers
import app.data.db.tables.chat.MessageContents
import app.data.db.tables.chat.MessageSlots
import app.data.db.tables.chat.MessageReadReceiptsTable
import app.data.db.tables.aiCharacter.OutreachHistoryTable
import app.data.db.tables.aiCharacter.FeedbackStatus
import app.utils.dbQuery
import com.ensodai.shared.avalon.chat.models.*
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.select
import kotlin.uuid.Uuid

@Factory
class SyncChatHistoryUseCase {

    private val MAX_GAP_THRESHOLD = 100L
    private val HARD_RESET_LIMIT = 50

    suspend fun execute(
        request: ChatSyncRequest,
        userId: Uuid
    ): ChatSyncResponse = dbQuery {

        val chatSessionRow = ChatSessions
            .selectAll()
            .where { (ChatSessions.id eq request.chatSessionId) and (ChatSessions.userId eq userId) }
            .singleOrNull()
            ?: throw Exception("Chat session not found or access denied")

        val dbVariantId = chatSessionRow[ChatSessions.activeVariantId]?.value
        val targetVariantId: Uuid = request.variantId ?: dbVariantId
        ?: throw Exception("Active chat variant not found")

        val requestSince = request.since
        if (requestSince == null) {
            return@dbQuery fetchLatestMessagesAsHardReset(targetVariantId, request.chatSessionId, HARD_RESET_LIMIT, userId)
        }

        val changedRows = MessageSlots
            .selectAll()
            .where {
                (MessageSlots.chatVariantId eq targetVariantId) and
                        (MessageSlots.updatedAt greaterEq requestSince)
            }
            .toList()

        val activeChangesCount = changedRows.count { it[MessageSlots.deletedAt] == null }

        if (activeChangesCount > MAX_GAP_THRESHOLD) {
            return@dbQuery fetchLatestMessagesAsHardReset(targetVariantId, request.chatSessionId, HARD_RESET_LIMIT, userId)
        }

        val deletedIds = changedRows
            .filter { it[MessageSlots.deletedAt] != null }
            .map { it[MessageSlots.id].value }

        val upsertedRows = changedRows
            .filter { it[MessageSlots.deletedAt] == null }

        if (upsertedRows.isEmpty()) {
            return@dbQuery ChatSyncResponse(
                isGapTooLarge = false,
                upserted = emptyList(),
                deletedSlotIds = deletedIds
            )
        }

        val mappedUpsertedSlots = mapSlotsToResponses(upsertedRows, request.chatSessionId, userId)

        ChatSyncResponse(
            isGapTooLarge = false,
            upserted = mappedUpsertedSlots,
            deletedSlotIds = deletedIds
        )
    }

    private fun fetchLatestMessagesAsHardReset(
        variantId: Uuid,
        chatSessionId: Uuid,
        limit: Int,
        userId: Uuid
    ): ChatSyncResponse {
        val slotsRows = MessageSlots
            .selectAll()
            .where {
                (MessageSlots.chatVariantId eq variantId) and
                        (MessageSlots.deletedAt.isNull())
            }
            .orderBy(MessageSlots.createdAt to SortOrder.DESC)
            .limit(limit)
            .toList()

        return ChatSyncResponse(
            isGapTooLarge = true,
            upserted = mapSlotsToResponses(slotsRows, chatSessionId, userId),
            deletedSlotIds = emptyList()
        )
    }

    private fun mapSlotsToResponses(
        slotsRows: List<ResultRow>,
        sessionId: Uuid,
        userId: Uuid
    ): List<MessageSlotResponse> {
        if (slotsRows.isEmpty()) return emptyList()

        val chatSessionRow = ChatSessions
            .selectAll()
            .where { ChatSessions.id eq sessionId }
            .singleOrNull()
        val characterId = chatSessionRow?.get(ChatSessions.characterId)?.value

        val pendingOutreachSentAt = if (characterId != null) {
            OutreachHistoryTable
                .select(OutreachHistoryTable.sentAt)
                .where { (OutreachHistoryTable.characterId eq characterId) and (OutreachHistoryTable.userId eq userId) and (OutreachHistoryTable.feedbackStatus eq FeedbackStatus.PENDING) }
                .singleOrNull()
                ?.get(OutreachHistoryTable.sentAt)
        } else null

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

        val containerIdsInSlots = dataRows.map { it[MessageContainers.id].value }.distinct()
        val readContainerIds = if (containerIdsInSlots.isNotEmpty()) {
            MessageReadReceiptsTable
                .selectAll()
                .where { (MessageReadReceiptsTable.userId eq userId) and (MessageReadReceiptsTable.messageContainerId inList containerIdsInSlots) }
                .map { it[MessageReadReceiptsTable.messageContainerId].value }
                .toSet()
        } else {
            emptySet()
        }

        val slotsMap = dataRows.groupBy { it[MessageContainers.slotId].value }

        return slotsRows.map { slotRow ->
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
                chatSessionId = sessionId,
                activeContainerId = slotRow[MessageSlots.activeMessageId]?.value,
                createdAt = slotRow[MessageSlots.createdAt],
                updatedAt = slotRow[MessageSlots.updatedAt],
                containers = containers
            )
        }
    }
}
