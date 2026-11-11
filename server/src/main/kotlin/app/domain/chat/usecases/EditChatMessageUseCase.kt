package app.domain.chat.usecases
import org.koin.core.annotation.Single
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import app.data.repository.EditMessageRepository
import app.domain.models.EditMessageResult

import app.service.chat.MessageNotifier
import com.ensodai.shared.avalon.chat.wss.ClientMessage
import app.utils.getUTCTimeNow
import app.utils.logger.WssLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Single
class EditChatMessageUseCase(
    private val editMessageRepository: EditMessageRepository,
    private val messageNotifier: MessageNotifier
) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun edit(
        userId: Uuid,
        message: ClientMessage.EditChatMessage
    ) {
        applicationScope.launch {
            try {

                val result = editMessageRepository.updateMessageText(
                    userId = userId,
                    slotId = message.slotId,
                    containerId = message.containerId,
                    newText = message.text.take(3000),
                    chatSessionId = message.chatSessionId,
                    newMessageSystemInstruction = message.messageSystemInstruction,
                )

                when (result) {
                    is EditMessageResult.Success -> {
                        WssLogger.info("Message ${message.containerId} edited by user $userId")
                        messageNotifier.notifyUserWithMessage(
                            userId = userId,
                            message = result.event,
                            excludeSessionId = null
                        )
                    }

                    is EditMessageResult.NotFound -> {
                        WssLogger.info("Edit failed: Slot or Message deleted. Syncing deletion.")

                        val event = ServerMessage.MessageEditError(
                            containerId = message.containerId,
                            slotId = message.slotId,
                            error = "Message not found",
                            timestamp = getUTCTimeNow()
                        )

                        messageNotifier.notifyUserWithMessage(
                            userId = userId,
                            message = event,
                            excludeSessionId = null
                        )
                    }

                    is EditMessageResult.Error -> {
                        val event = ServerMessage.MessageEditError(
                            containerId = message.containerId,
                            slotId = message.slotId,
                            error = "Failed to edit message",
                            timestamp = getUTCTimeNow()
                        )

                        messageNotifier.notifyUserWithMessage(
                            userId = userId,
                            message = event,
                            excludeSessionId = null
                        )
                    }
                }
            } catch (e: Throwable) {
                WssLogger.fail("Edit error: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}