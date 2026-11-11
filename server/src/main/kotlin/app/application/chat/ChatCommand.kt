package app.application.chat

import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import kotlin.uuid.Uuid

sealed interface ChatCommand {
    val chatVariantId: Uuid
    val chatSessionId: Uuid

    data class SendMessage(
        val slotId: Uuid,
        val contentId: Uuid,
        val containerId: Uuid,
        override val chatVariantId: Uuid,
        override val chatSessionId: Uuid,
        val contentType: AvalonMessageContentType,
        val text: String,
        val messageSystemInstruction: String?,
        val characterId: Uuid,
        val llmModelCode: String?
    ) : ChatCommand

    data class ChangeActiveContainer(
        override val chatVariantId: Uuid,
        override val chatSessionId: Uuid,
        val slotId: Uuid,
        val newActiveContainerId: Uuid
    ) : ChatCommand

    data class DeleteMessages(
        val slotIds: List<Uuid>,
        override val chatVariantId: Uuid,
        override val chatSessionId: Uuid
    ) : ChatCommand

    data class EditMessage(
        val slotId: Uuid,
        val containerId: Uuid,
        override val chatVariantId: Uuid,
        override val chatSessionId: Uuid,
        val text: String,
        val messageSystemInstruction: String?
    ) : ChatCommand

    data class ForceStartGenerateResponse(
        override val chatVariantId: Uuid,
        override val chatSessionId: Uuid,
        val characterId: Uuid,
        val llmModelCode: String?
    ) : ChatCommand

    data class StopGenerateResponse(
        override val chatVariantId: Uuid,
        override val chatSessionId: Uuid
    ) : ChatCommand

    data class GenerateNewContainer(
        val slotId: Uuid,
        override val chatVariantId: Uuid,
        override val chatSessionId: Uuid,
        val characterId: Uuid,
        val llmModelCode: String?
    ) : ChatCommand

    data class SendUserReaction(
        val reaction: String?,
        val characterId: Uuid,
        override val chatVariantId: Uuid,
        override val chatSessionId: Uuid,
        val slotId: Uuid,
        val containerId: Uuid
    ) : ChatCommand
}

data class ChatActor(
    val userId: Uuid,
    val sessionId: Uuid
)
