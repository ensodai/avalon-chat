package app.apiv4.mappers

import com.ensodai.shared.avalon.chat.models.*
import com.ensodai.shared.avalon.chat.wss.*
import app.data.db.tables.chatSessions.ChatVariantInternalDto

/**
 * В новой архитектуре RPC мы используем KMP модели напрямую.
 */

fun MessageStatus.toSharedMessageStatus(): MessageStatus = this

fun MessageContentResponse.toSharedMessageContent(): MessageContentResponse = this

fun MessageContainerResponse.toSharedMessageContainer(): MessageContainerResponse = this

fun MessageSlotResponse.toSharedMessageSlot(): MessageSlotResponse = this

fun PaginatedChatResponse.toBackPaginatedResponse(): PaginatedChatResponse = this

fun ChatSyncResponse.toBackChatSyncResponse(): ChatSyncResponse = this

fun ChatVariantInternalDto.toSharedChatVariant(): ChatVariantResponse {
    return ChatVariantResponse(
        id = this.id,
        name = this.name,
        image = null,
        sessionId = this.chatSessionId,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
