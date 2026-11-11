package app.domain.chat.usecases
import org.koin.core.annotation.Single
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import app.data.repository.DeleteMessageRepository

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
class DeleteMessageUseCase(
    private val deleteMessageRepository: DeleteMessageRepository,
    private val messageNotifier: MessageNotifier
) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun delete(userId: Uuid, message: ClientMessage.DeleteMessages) {
        WssLogger.info("[DeleteMessageUseCase] delete: userId=$userId, slotIds=${message.slotIds}, chatSessionId=${message.chatSessionId}, chatVariantId=${message.chatVariantId}")
        applicationScope.launch {
            try {
                val deleteEvent = deleteMessageRepository.deleteMessages(
                    userId = userId,
                    slotIds = message.slotIds,
                    chatSessionId = message.chatSessionId,
                    chatVariantId = message.chatVariantId
                )

                if (deleteEvent != null) {
                    WssLogger.info("[DeleteMessageUseCase] Slots ${deleteEvent.slotIds} successfully deleted by user $userId")

                    messageNotifier.notifyUserWithMessage(
                        userId = userId,
                        message = deleteEvent,
                        excludeSessionId = null
                    )
                } else {
                    WssLogger.fail("[DeleteMessageUseCase] Delete failed (repo returned null) for user $userId slots ${message.slotIds}. Sending Error event.")

                    val errorEvent = ServerMessage.MessagesDeleteError(
                        slotIds = message.slotIds,
                        error = "deletion_failed_or_not_found",
                        timestamp = getUTCTimeNow()
                    )

                    messageNotifier.notifyUserWithMessage(userId, errorEvent, null)
                }
            } catch (e: Throwable) {
                WssLogger.fail("[DeleteMessageUseCase] Exception during delete process: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}