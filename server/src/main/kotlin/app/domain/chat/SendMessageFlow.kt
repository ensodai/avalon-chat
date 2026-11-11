package app.domain.chat
import com.ensodai.shared.avalon.gifts.models.*
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import org.koin.core.annotation.Single

import app.service.chat.*
import app.domain.chat.*
import app.application.chat.PreparedSendMessageContext
import app.data.repository.UserRepository
import app.utils.logger.WssLogger

@Single
class SendMessageFlow(
    private val userRepository: UserRepository,
    private val prepareOutgoingUserMessageService: PrepareOutgoingUserMessageService,
    private val persistUserMessageService: PersistUserMessageService,
    private val handlePersistedUserMessageResultService: HandlePersistedUserMessageResultService
) {

    suspend fun run(
        prepared: PreparedSendMessageContext
    ) {
        WssLogger.info("СОХРАНЕНИЕ СООБЩЕНИЯ ПОЛЬЗОВАТЕЛЯ")

        val outgoingUserMessage = prepareOutgoingUserMessageService.prepare(prepared)
        val userSlotResponse = persistUserMessageService.persist(
            prepared = prepared,
            finalContentText = outgoingUserMessage.finalContentText
        )

        val userPremiumStatus = userRepository.getUserPremiumStatus(prepared.actor.userId)
        handlePersistedUserMessageResultService.handle(
            prepared = prepared,
            userSlotResponse = userSlotResponse,
            energyBalance = outgoingUserMessage.energyBalance,
            userPremiumStatus = userPremiumStatus
        )
    }
}
