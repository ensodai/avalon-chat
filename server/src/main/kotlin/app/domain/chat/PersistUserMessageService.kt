package app.domain.chat

import com.ensodai.shared.avalon.chat.wss.ServerMessage
import com.ensodai.shared.avalon.chat.models.MessageSlotResponse
import com.ensodai.shared.avalon.chat.models.Rarity
import app.service.chat.*
import app.domain.chat.*
import com.ensodai.shared.avalon.chat.models.MessageRole
import app.application.chat.PreparedSendMessageContext
import app.data.repository.ChatRepository
import org.koin.core.annotation.Single

@Single
class PersistUserMessageService(
    private val chatRepository: ChatRepository
) {

    suspend fun persist(
        prepared: PreparedSendMessageContext,
        finalContentText: String
    ): MessageSlotResponse {
        val command = prepared.command

        return chatRepository.createNewMessageTurn(
            messageRole = MessageRole.USER,
            messageRarity = Rarity.COMMON,
            slotId = command.slotId,
            contentId = command.contentId,
            containerId = command.containerId,
            chatVariantId = command.chatVariantId,
            chatSessionId = command.chatSessionId,
            contentType = command.contentType,
            text = finalContentText,
            messageSystemInstruction = command.messageSystemInstruction
        )
    }
}
