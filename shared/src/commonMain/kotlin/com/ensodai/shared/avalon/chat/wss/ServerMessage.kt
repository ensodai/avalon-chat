package com.ensodai.shared.avalon.chat.wss

import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.MessageContainerResponse
import com.ensodai.shared.avalon.chat.models.MessageSlotResponse
import com.ensodai.shared.avalon.chat.models.Rarity
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
sealed class ServerMessage {

    @Serializable
    @SerialName("message_stream_chunk")
    data class MessageStreamChunk(
        @Contextual val characterId: Uuid,
        @Contextual val sessionId: Uuid,
        @Contextual val variantId: Uuid,
        @Contextual val slotId: Uuid,
        @Contextual val containerId: Uuid,
        @Contextual val contentId: Uuid,
        val textChunk: String // Дельта (новый кусок) текста
    ) : ServerMessage()

    @Serializable
    @SerialName("message_ack")
    data class MessageAck(
        @Contextual
        val containerId: Uuid,
        @Contextual
        val serverId: Uuid,
        @Contextual
        val slotId: Uuid,
        @Contextual
        val timestamp: Instant
    ) : ServerMessage()

    @Serializable
    @SerialName("message_ack_error")
    data class MessageAckError(
        @Contextual
        val containerId: Uuid,
        @Contextual
        val slotId: Uuid,
        @Contextual
        val serverId: Uuid,
        @Contextual
        val timestamp: Instant
    ) : ServerMessage()

    @Serializable
    @SerialName("messages_delete_error")
    data class MessagesDeleteError(
        val slotIds: List<@Contextual Uuid>,
        val error: String,
        @Contextual
        val timestamp: Instant
    ) : ServerMessage()

    @Serializable
    @SerialName("message_edit_error")
    data class MessageEditError(
        @Contextual val containerId: Uuid,
        @Contextual val slotId: Uuid,
        val error: String,
        @Contextual val timestamp: Instant
    ) : ServerMessage()

    /** Общая ошибка, если что-то пошло не так. */
    @Serializable
    @SerialName("error")
    data class Error(val message: String) : ServerMessage()

    // --- СИГНАЛЫ (Sync) ---
    @Serializable
    @SerialName("sync_trigger")
    data class SyncTrigger(val entity: String) : ServerMessage()

    @Serializable
    @SerialName("new_chat_message")
    data class NewChatMessage(
        val characterId: @Contextual Uuid,
        val slot: MessageSlotResponse,
        val currentUserQuota: Int,
        val reserveEnergy: Int
    ) : ServerMessage()

    @Serializable
    @SerialName("message_updated")
    data class MessageUpdated(
        @Contextual
        val containerId: Uuid,
        val newText: String,
        val newMessageSystemInstruction: String? = null,
        @Contextual
        val updatedAt: Instant,
        @Contextual
        val sessionId: Uuid,
        val isLastMessage: Boolean
    ) : ServerMessage()

    @Serializable
    @SerialName("messages_deleted")
    data class MessagesDeleted(
        val slotIds: List<@Contextual Uuid>,
        val deletedAt: @Contextual Instant,
        val sessionId: @Contextual Uuid,
        val previewContent: String?,
        val previewType: AvalonMessageContentType?,
        val previewRole: MessageRole?,
        val previewTime: @Contextual Instant?
    ) : ServerMessage()

    @Serializable
    @SerialName("new_container_added")
    data class NewContainerAdded(
        val sessionId: @Contextual Uuid,
        val container: MessageContainerResponse,
        val currentUserQuota: Int,
        val reserveEnergy: Int
    ) : ServerMessage()

    @Serializable
    @SerialName("active_container_changed")
    data class ActiveContainerChanged(
        val slotId: @Contextual Uuid,
        val newContainerId: @Contextual Uuid,
        val updatedAt: @Contextual Instant,
        val content: String,
        val contentType: AvalonMessageContentType,
        val role: MessageRole,
        val sessionId: @Contextual Uuid
    ) : ServerMessage()

    @Serializable
    @SerialName("send_user_reaction")
    data class SendUserReaction(
        val reaction: String?,
        val messageContainerId: @Contextual Uuid
    ) : ServerMessage()

    @Serializable
    @SerialName("send_character_reaction")
    data class SendCharacterReaction(
        val reaction: String?,
        val messageContainerId: @Contextual Uuid
    ) : ServerMessage()

    @Serializable
    @SerialName("messages_read")
    data class MessagesRead(
        val messageContainerIds: List<@Contextual Uuid>,
        @Contextual
        val userId: Uuid,
        @Contextual
        val timestamp: Instant
    ) : ServerMessage()

    @Serializable
    @SerialName("delete_character")
    data class DeleteCharacter(val characterID: @Contextual Uuid) : ServerMessage()

    @Serializable
    @SerialName("message_content_added")
    data class MessageContentAdded(
        val containerId: @Contextual Uuid,
        val contentId: @Contextual Uuid,
        @SerialName("content_type")
        val contentType: AvalonMessageContentType,
        val content: String,
        val orderIndex: Int
    ) : ServerMessage()

    @Serializable
    @SerialName("relationship_level_up")
    data class RelationshipLevelUp(
        @Contextual val characterId: Uuid,
        val oldLevel: Int,
        val newLevel: Int,
        val currentExperience: Long,
        val maxExperienceForNextLevel: Long
    ) : ServerMessage()
}
