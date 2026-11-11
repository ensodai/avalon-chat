package com.ensodai.shared.avalon.chat.wss

import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed class ClientMessage {

    abstract val chatVariantId: @Contextual Uuid
    abstract val chatSessionId: @Contextual Uuid

    @Serializable
    @SerialName("authenticate")
    data class Authenticate(
        val accessToken: String
    ) : ClientMessage() {
        override val chatVariantId: @Contextual Uuid = Uuid.NIL
        override val chatSessionId: @Contextual Uuid = Uuid.NIL
    }

    @Serializable
    @SerialName("send_chat_message")
    data class SendChatMessage(
        val slotId: @Contextual Uuid,
        val contentId: @Contextual Uuid,
        val containerId: @Contextual Uuid,
        override val chatVariantId: @Contextual Uuid,
        override val chatSessionId: @Contextual Uuid,
        val contentType: AvalonMessageContentType,
        val text: String,
        val messageSystemInstruction: String? = null,
        val characterId: @Contextual Uuid,
        val llmModelCode: String? = null
    ) : ClientMessage()

    @Serializable
    @SerialName("change_active_container")
    data class ChangeActiveContainer(
        override val chatVariantId: @Contextual Uuid,
        override val chatSessionId: @Contextual Uuid,
        val slotId: @Contextual Uuid,
        val newActiveContainerId: @Contextual Uuid
    ) : ClientMessage()

    @Serializable
    @SerialName("edit_chat_message")
    data class EditChatMessage(
        val slotId: @Contextual Uuid,
        val containerId: @Contextual Uuid,
        override val chatVariantId: @Contextual Uuid,
        override val chatSessionId: @Contextual Uuid,
        val text: String,
        val messageSystemInstruction: String? = null,
    ) : ClientMessage()

    @Serializable
    @SerialName("force_start_generate_response")
    data class ForceStartGenerateResponse(
        override val chatVariantId: @Contextual Uuid,
        override val chatSessionId: @Contextual Uuid,
        val characterId: @Contextual Uuid,
        val llmModelCode: String? = null
    ) : ClientMessage()

    @Serializable
    @SerialName("stop_generate_response")
    data class StopGenerateResponse(
        override val chatVariantId: @Contextual Uuid,
        override val chatSessionId: @Contextual Uuid,
    ) : ClientMessage()

    @Serializable
    @SerialName("delete_messages")
    data class DeleteMessages(
        val slotIds: List<@Contextual Uuid>,
        override val chatVariantId: @Contextual Uuid,
        override val chatSessionId: @Contextual Uuid
    ) : ClientMessage()

    @Serializable
    @SerialName("generate_new_container")
    data class GenerateNewContainer(
        val slotId: @Contextual Uuid,
        override val chatVariantId: @Contextual Uuid,
        override val chatSessionId: @Contextual Uuid,
        val characterId: @Contextual Uuid,
        val llmModelCode: String? = null
    ) : ClientMessage()

    @Serializable
    @SerialName("send_user_reaction")
    data class SendUserReaction(
        val reaction: String?,
        val characterId: @Contextual Uuid,
        override val chatVariantId: @Contextual Uuid,
        override val chatSessionId: @Contextual Uuid,
        val slotId: @Contextual Uuid,
        val containerId: @Contextual Uuid
    ) : ClientMessage()
}
