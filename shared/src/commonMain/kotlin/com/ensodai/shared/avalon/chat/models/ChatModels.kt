@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package com.ensodai.shared.avalon.chat.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
enum class MessageRole {
    USER,
    MODEL,
    SYSTEM
}

@Serializable
enum class Rarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    MYTHIC
}

@Serializable
enum class AvalonMessageContentType {
    TEXT,
    IMAGE,
    GENERATED_IMAGE_DESCRIPTION,
    STICKER_GIFT,
    GIFT,
    SYSTEM_EVENT,
    MESSAGE_SYSTEM_INSTRUCTION,
    AUDIO
}

@Serializable
sealed class MessageStatus {
    @Serializable
    @SerialName("PENDING")
    data object PENDING : MessageStatus()

    @Serializable
    @SerialName("SENDING")
    data object SENDING : MessageStatus()

    @Serializable
    @SerialName("SENT")
    data object SENT : MessageStatus()

    @Serializable
    @SerialName("FAILED")
    data class FAILED(val reason: String) : MessageStatus()
}

@Serializable
data class MessageContentResponse(
    @Contextual
    val id: Uuid,
    val type: AvalonMessageContentType,
    val content: String,
    val orderIndex: Int
)

@Serializable
data class MessageContainerResponse(
    @Contextual
    val containerId: Uuid,
    @Contextual
    val slotId: Uuid,
    val role: MessageRole,
    val rarity: Rarity,
    val status: MessageStatus,
    val userReaction: String?,
    val characterReaction: String?,
    val isRead: Boolean = false,
    val showOutreachFeedback: Boolean = false,
    @Contextual
    val createdAt: Instant,
    @Contextual
    val updatedAt: Instant,
    val contents: List<MessageContentResponse>
)

@Serializable
data class MessageSlotResponse(
    @Contextual
    val id: Uuid,
    @Contextual
    val chatVariantId: Uuid,
    @Contextual
    val chatSessionId: Uuid,
    @Contextual
    val activeContainerId: Uuid?,
    @Contextual
    val createdAt: Instant,
    @Contextual
    val updatedAt: Instant,
    val containers: List<MessageContainerResponse>
)

@Serializable
data class GetChatHistoryRequest(
    @Contextual
    val chatSessionId: Uuid,
    @Contextual
    val variantId: Uuid? = null,
    @Contextual
    val cursor: Instant? = null,
    val limit: Int = 20
)

@Serializable
data class ChatSyncRequest(
    @Contextual
    val chatSessionId: Uuid,
    @Contextual
    val variantId: Uuid? = null,
    @Contextual
    val since: Instant? = null
)

@Serializable
data class ChatSyncResponse(
    val isGapTooLarge: Boolean,
    val upserted: List<MessageSlotResponse>,
    val deletedSlotIds: List<@Contextual Uuid>
)

@Serializable
data class PaginatedChatResponse(
    val items: List<MessageSlotResponse>,
    @Contextual
    val nextCursor: Instant?
)

@Serializable
data class ChatVariantImageResponse(
    @Contextual
    val id: Uuid,
    val imageUrl: String,
    val thumbHash: String? = null
)

@Serializable
data class ChatVariantResponse(
    @Contextual
    val id: Uuid,
    val name: String,
    val image: ChatVariantImageResponse?,
    @Contextual
    val sessionId: Uuid,
    @Contextual
    val createdAt: Instant,
    @Contextual
    val updatedAt: Instant,
    val unreadCount: Int = 0
)

@Serializable
data class CreateChatVariantRequest(
    @Contextual
    val chatSessionId: Uuid,
    @Contextual
    val chatVariantId: Uuid
)

@Serializable
data class UpdateChatVariantRequest(
    @Contextual
    val variantId: Uuid,
    val name: String? = null,
    val imageToken: String? = null,
    val removeImage: Boolean = false
)

@Serializable
data class ChatPreviewMessageResponse(
    @Contextual
    val id: Uuid,
    val type: AvalonMessageContentType,
    val content: String,
    val role: MessageRole,
    val isRead: Boolean = false,
    @Contextual
    val createdAt: Instant
)

@Serializable
data class ChatSessionPreviewResponse(
    @Contextual
    val id: Uuid,
    @Contextual
    val characterId: Uuid,
    @Contextual
    val activeVariantId: Uuid?,
    val isArchived: Boolean,
    @Contextual
    val updatedAt: Instant,
    @Contextual
    val createdAt: Instant,
    val lastMessage: ChatPreviewMessageResponse?
)

@Serializable
data class ChatUpdateItem(
    @Contextual
    val id: Uuid,
    val isArchived: Boolean? = null
)

@Serializable
data class UpdateChatSessionsResponse(
    val updatedResult: Map<@Contextual Uuid, @Contextual Instant>
)

@Serializable
data class BatchUpdateChatsRequest(
    val items: List<ChatUpdateItem>
)

@Serializable
data class BatchDeleteChatsRequest(
    val ids: List<@Contextual Uuid>
)

@Serializable
data class TypingState(
    @Contextual
    val chatId: Uuid,
    val isTyping: Boolean,
    val rarity: Rarity
)

