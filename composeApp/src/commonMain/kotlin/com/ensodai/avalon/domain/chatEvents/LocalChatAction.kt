package com.ensodai.avalon.domain.chatEvents

import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.wss.ClientMessage
import kotlin.uuid.Uuid

/**
 * Локальные модели действий чата для очереди [MessageQueueManager].
 * Это позволяет отвязать логику очереди от сетевых моделей KMP.
 */
sealed class LocalChatAction {
    abstract val chatSessionId: Uuid
    abstract val chatVariantId: Uuid

    data class SendChatMessage(
        val slotId: Uuid,
        val contentId: Uuid,
        val containerId: Uuid,
        override val chatVariantId: Uuid,
        override val chatSessionId: Uuid,
        val contentType: AvalonMessageContentType,
        val text: String,
        val characterId: Uuid,
        val llmModelCode: String? = null,
        val messageSystemInstruction: String? = null
    ) : LocalChatAction()

    data class EditChatMessage(
        val slotId: Uuid,
        val containerId: Uuid,
        override val chatVariantId: Uuid,
        override val chatSessionId: Uuid,
        val text: String,
        val messageSystemInstruction: String? = null
    ) : LocalChatAction()

    data class DeleteMessages(
        val slotIds: List<Uuid>,
        override val chatVariantId: Uuid,
        override val chatSessionId: Uuid
    ) : LocalChatAction()

    data class ChangeActiveContainer(
        override val chatVariantId: Uuid,
        override val chatSessionId: Uuid,
        val slotId: Uuid,
        val newActiveContainerId: Uuid
    ) : LocalChatAction()

    data class SendUserReaction(
        val reaction: String?,
        val characterId: Uuid,
        override val chatVariantId: Uuid,
        override val chatSessionId: Uuid,
        val slotId: Uuid,
        val containerId: Uuid
    ) : LocalChatAction()

    data class ForceStartGenerateResponse(
        override val chatSessionId: Uuid,
        override val chatVariantId: Uuid,
        val characterId: Uuid,
        val llmModelCode: String? = null
    ) : LocalChatAction()

    data class GenerateNewContainer(
        override val chatSessionId: Uuid,
        override val chatVariantId: Uuid,
        val characterId: Uuid,
        val slotId: Uuid,
        val llmModelCode: String? = null
    ) : LocalChatAction()

    data class StopGenerateResponse(
        override val chatSessionId: Uuid,
        override val chatVariantId: Uuid
    ) : LocalChatAction()

    /**
     * Конвертация в сетевую модель KMP.
     */
    fun toKmpModel(): ClientMessage = when (this) {
        is SendChatMessage -> ClientMessage.SendChatMessage(
            slotId = slotId,
            contentId = contentId,
            containerId = containerId,
            chatVariantId = chatVariantId,
            chatSessionId = chatSessionId,
            contentType = contentType,
            text = text,
            messageSystemInstruction = messageSystemInstruction,
            characterId = characterId,
            llmModelCode = llmModelCode
        )
        is EditChatMessage -> ClientMessage.EditChatMessage(
            slotId, containerId, chatVariantId, chatSessionId, text, messageSystemInstruction
        )
        is DeleteMessages -> ClientMessage.DeleteMessages(
            slotIds, chatVariantId, chatSessionId
        )
        is ChangeActiveContainer -> ClientMessage.ChangeActiveContainer(
            chatVariantId, chatSessionId, slotId, newActiveContainerId
        )
        is SendUserReaction -> ClientMessage.SendUserReaction(
            reaction, characterId, chatVariantId, chatSessionId, slotId, containerId
        )
        is ForceStartGenerateResponse -> ClientMessage.ForceStartGenerateResponse(
            chatSessionId, chatVariantId, characterId, llmModelCode
        )
        is GenerateNewContainer -> ClientMessage.GenerateNewContainer(
            chatSessionId, chatVariantId, characterId, slotId, llmModelCode
        )
        is StopGenerateResponse -> ClientMessage.StopGenerateResponse(
            chatSessionId, chatVariantId
        )
    }
}
