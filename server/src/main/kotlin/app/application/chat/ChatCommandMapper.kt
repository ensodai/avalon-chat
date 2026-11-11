package app.application.chat

import com.ensodai.shared.avalon.chat.wss.ClientMessage

fun ClientMessage.toChatCommandOrNull(): ChatCommand? = when (this) {
    is ClientMessage.Authenticate -> null
    is ClientMessage.SendChatMessage -> ChatCommand.SendMessage(
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
    is ClientMessage.ChangeActiveContainer -> ChatCommand.ChangeActiveContainer(
        chatVariantId = chatVariantId,
        chatSessionId = chatSessionId,
        slotId = slotId,
        newActiveContainerId = newActiveContainerId
    )
    is ClientMessage.EditChatMessage -> ChatCommand.EditMessage(
        slotId = slotId,
        containerId = containerId,
        chatVariantId = chatVariantId,
        chatSessionId = chatSessionId,
        text = text,
        messageSystemInstruction = messageSystemInstruction
    )
    is ClientMessage.ForceStartGenerateResponse -> ChatCommand.ForceStartGenerateResponse(
        chatVariantId = chatVariantId,
        chatSessionId = chatSessionId,
        characterId = characterId,
        llmModelCode = llmModelCode
    )
    is ClientMessage.StopGenerateResponse -> ChatCommand.StopGenerateResponse(
        chatVariantId = chatVariantId,
        chatSessionId = chatSessionId
    )
    is ClientMessage.DeleteMessages -> ChatCommand.DeleteMessages(
        slotIds = slotIds,
        chatVariantId = chatVariantId,
        chatSessionId = chatSessionId
    )
    is ClientMessage.GenerateNewContainer -> ChatCommand.GenerateNewContainer(
        slotId = slotId,
        chatVariantId = chatVariantId,
        chatSessionId = chatSessionId,
        characterId = characterId,
        llmModelCode = llmModelCode
    )
    is ClientMessage.SendUserReaction -> ChatCommand.SendUserReaction(
        reaction = reaction,
        characterId = characterId,
        chatVariantId = chatVariantId,
        chatSessionId = chatSessionId,
        slotId = slotId,
        containerId = containerId
    )
}
