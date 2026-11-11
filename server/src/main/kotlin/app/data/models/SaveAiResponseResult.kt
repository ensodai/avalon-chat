package app.data.models

import com.ensodai.shared.avalon.chat.wss.ServerMessage

data class SaveAiResponseResult(
    val reactionEvent: ServerMessage.SendCharacterReaction?,
    val responseMessageData: Any // Может быть MessageSlotResponse или MessageContainerResponse
)