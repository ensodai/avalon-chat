package app.domain.chat.usecases
import org.koin.core.annotation.Single
import app.data.repository.ChangeContainerRepository

import app.service.chat.MessageNotifier
import com.ensodai.shared.avalon.chat.wss.ClientMessage
import app.utils.logger.WssLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@Single
class ChangeActiveContainerUseCase(
    private val changeContainerRepository: ChangeContainerRepository,
    private val messageNotifier: MessageNotifier
) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun change(
        userId: Uuid,
        message: ClientMessage.ChangeActiveContainer
    ) {
        applicationScope.launch {
            try {
                val event = changeContainerRepository.setActiveContainer(
                    userId = userId,
                    slotId = message.slotId,
                    containerId = message.newActiveContainerId
                )

                if (event != null) {
                    WssLogger.info("Slot ${message.slotId} switched to container ${message.newActiveContainerId}")

                    messageNotifier.notifyUserWithMessage(
                        userId = userId,
                        message = event,
                        excludeSessionId = null
                    )
                } else {
                    messageNotifier.notifyUserWithError(userId, "Failed to change active container")
                }
            } catch (e: Throwable) {
                WssLogger.fail("Swipe error: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}