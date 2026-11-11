package com.ensodai.avalon.domain.repository.local

import com.ensodai.avalon.data.db.dto.ChatVariant
import com.ensodai.avalon.data.db.dto.chat.CreateSlotDto
import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.avalon.data.db.models.message.MessageSlotEntity
import com.ensodai.avalon.data.db.models.message.SlotInfo
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import com.ensodai.shared.avalon.chat.models.MessageContainerResponse
import com.ensodai.shared.avalon.chat.models.MessageSlotResponse
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant
import kotlin.uuid.Uuid
import com.ensodai.avalon.data.db.models.chat.SlotWithContainers

interface LocalChatHistoryRepository {

    suspend fun saveNewMessage(dto: CreateSlotDto)

    suspend fun addMessageContent(
        containerId: Uuid,
        contentId: Uuid,
        type: AvalonMessageContentType,
        content: String,
        orderIndex: Int
    )

    suspend fun addNewContainerForSlot(
        container: MessageContainerResponse,
        sessionId: Uuid
    )

    fun getChatHistoryPagingSource(
        chatVariantId: Uuid
    )

    fun getChatHistoryFlow(
        variantId: Uuid,
        limit: Int
    ): Flow<List<SlotWithContainers>>

    fun getSharedMediaFlow(
        characterId: Uuid
    ): Flow<List<String>>

    suspend fun confirmMessageSentError(
        containerId: Uuid,
        serverTimestamp: Instant,
        slotId: Uuid
    )

    suspend fun deleteMessagesByIds(ids: List<Uuid>)

    suspend fun updateMessageTextContentByContainerId(
        messageContainerId: Uuid,
        newMessage: String,
        syncAction: SyncAction,
        updatedAt: Instant?
    )

    suspend fun updateMessageSystemInstructionContentByContainerId(
        messageContainerId: Uuid,
        newSystemInstruction: String,
        syncAction: SyncAction,
        updatedAt: Instant?
    )

    suspend fun processMessageUpdateEvent(event: ServerMessage.MessageUpdated)

    suspend fun deleteAllChatMessagesByChatSessionId(chatSessionId: Uuid)

    suspend fun searchMessages(query: String, chatVariantId: Uuid): List<Uuid>

    suspend fun getLastTwoMessagesFlow(chatVariantId: Uuid)

    suspend fun getLastMessagesFlow(chatVariantId: Uuid)

    suspend fun getLastSlotWithMessagesFlow(chatVariantId: Uuid)

    suspend fun getChatVariants(sessionId: Uuid?): Flow<List<ChatVariant>>

    suspend fun getChatVariantsCountFlow(sessionId: Uuid): Flow<Int>

    suspend fun updateActiveMessageInSlot(
        messageSlotId: Uuid,
        newActiveContainerId: Uuid,
        updatedAt: Instant
    )

    suspend fun softDeleteSlotWithMessagesBySlotIds(slotIds: List<Uuid>)

    suspend fun rpcHardDeleteSlotWithMessagesBySlotIds(event: ServerMessage.MessagesDeleted)

    suspend fun hardDeleteSlotWithMessagesBySlotIds(slotIds: List<Uuid>)

    suspend fun clearChatVariant(chatVariantServerId: Uuid)

    suspend fun confirmMessageSent(
        containerId: Uuid,
        serverTimestamp: Instant,
        slotId: Uuid
    )

    suspend fun saveChatHistoryResponse(variantId: Uuid, slots: List<MessageSlotResponse>)

    suspend fun getNewestSlot(variantId: Uuid): MessageSlotEntity?

    suspend fun clearHistory(variantId: Uuid)

    suspend fun getLastMessageTime(variantId: Uuid): Instant?

    suspend fun getLastSlotInfoFlow(variantId: Uuid): Flow<SlotInfo?>

    suspend fun updateActiveMessageFromRPCEvent(
        event: ServerMessage.ActiveContainerChanged
    )

    suspend fun variantHasUnsyncedMessages(variantId: Uuid): Boolean

    suspend fun isVariantExists(variantId: Uuid): Boolean

    suspend fun getSlotsByIds(ids: List<Uuid>): List<MessageSlotEntity>

    suspend fun updateSlotsSyncAction(ids: List<Uuid>, action: SyncAction)

    suspend fun toggleMessageUserReaction(messageContainerId: Uuid, reaction: String?): String?

    suspend fun toggleMessageCharacterReaction(messageContainerId: Uuid, reaction: String?): String?

    suspend fun updateMessageUserReaction(messageContainerId: Uuid, reaction: String?)

    suspend fun updateMessageCharacterReaction(messageContainerId: Uuid, reaction: String?)

    suspend fun markMessagesAsReadLocally(containerIds: List<Uuid>)

    suspend fun hideOutreachFeedbackForContainer(containerId: Uuid)

    suspend fun getLastServerUpdateTime(variantId: Uuid): Instant?

    suspend fun getOldestSlot(variantId: Uuid): MessageSlotEntity?

    suspend fun getSlotsCount(variantId: Uuid): Int

    suspend fun clearAndSaveChatHistory(
        variantId: Uuid,
        upsertedMessages: List<MessageSlotResponse>
    )

    suspend fun getUnreadModelContainerIds(variantId: Uuid): List<Uuid>

}
