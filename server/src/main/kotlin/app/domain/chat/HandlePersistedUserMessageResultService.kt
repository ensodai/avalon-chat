package app.domain.chat

import com.ensodai.shared.avalon.chat.wss.ServerMessage
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import com.ensodai.shared.avalon.chat.models.MessageSlotResponse
import app.service.chat.*
import app.domain.chat.*
import app.application.chat.PreparedSendMessageContext
import app.data.models.EnergyDeductionResult
import com.ensodai.shared.avalon.chat.models.SystemEventType
import app.data.repository.ChatRepository
import app.service.chat.MessageNotifier
import app.utils.logger.WssLogger
import org.koin.core.annotation.Single

@Single
class HandlePersistedUserMessageResultService(
    private val messageNotifier: MessageNotifier,
    private val quotaAlertService: QuotaAlertService,
    private val chatGenerationService: ChatGenerationService,
    private val chatRepository: ChatRepository
) {

    suspend fun handle(
        prepared: PreparedSendMessageContext,
        userSlotResponse: MessageSlotResponse,
        energyBalance: EnergyDeductionResult,
        userPremiumStatus: PremiumStatus
    ) {
        val actor = prepared.actor
        val command = prepared.command

        WssLogger.info("Отправка ACK")
        val messageAck = ServerMessage.MessageAck(
            containerId = command.containerId,
            serverId = command.containerId,
            timestamp = userSlotResponse.createdAt,
            slotId = command.slotId
        )
        messageNotifier.notifyUserWithMessage(
            userId = actor.userId,
            message = messageAck,
            excludeSessionId = null
        )

        when (energyBalance) {
            is EnergyDeductionResult.RouteError -> {
                WssLogger.info("Ошибка маршрутизации биллинга: ${energyBalance.eventType}")
                val systemMessage = chatRepository.createSystemMessage(
                    userId = actor.userId,
                    chatSessionId = command.chatSessionId,
                    chatVariantId = command.chatVariantId,
                    eventType = energyBalance.eventType
                )
                messageNotifier.notifyUserWithMessage(
                    userId = actor.userId,
                    excludeSessionId = null,
                    message = ServerMessage.NewChatMessage(
                        characterId = command.characterId,
                        slot = systemMessage,
                        currentUserQuota = -1,
                        reserveEnergy = 0,
                    )
                )
            }

            is EnergyDeductionResult.InsufficientFunds -> {
                WssLogger.info("Уведомляем другие устройства (Недостаточно средств)")
                messageNotifier.notifyUserWithMessage(
                    userId = actor.userId,
                    excludeSessionId = actor.sessionId,
                    message = ServerMessage.NewChatMessage(
                        characterId = command.characterId,
                        slot = userSlotResponse,
                        currentUserQuota = energyBalance.currentBalance.dailyEnergy,
                        reserveEnergy = energyBalance.currentBalance.reserveEnergy,
                    )
                )

                quotaAlertService.notifyQuotaExceededIfNeeded(
                    userId = actor.userId,
                    chatSessionId = command.chatSessionId,
                    chatVariantId = command.chatVariantId,
                    characterId = command.characterId,
                    currentBalance = energyBalance.currentBalance
                )
            }

            is EnergyDeductionResult.Success -> {
                WssLogger.info("Уведомляем другие устройства")
                messageNotifier.notifyUserWithMessage(
                    userId = actor.userId,
                    excludeSessionId = actor.sessionId,
                    message = ServerMessage.NewChatMessage(
                        characterId = command.characterId,
                        slot = userSlotResponse,
                        currentUserQuota = energyBalance.newBalance.dailyEnergy,
                        reserveEnergy = energyBalance.newBalance.reserveEnergy,
                    )
                )

                WssLogger.info("Запуск процесса генерации ответа")
                chatGenerationService.launchGenerationJob(
                    userId = actor.userId,
                    chatSessionId = command.chatSessionId,
                    variantId = command.chatVariantId,
                    characterId = command.characterId,
                    preValidatedConfig = prepared.characterConfig,
                    targetSlotId = null,
                    llmConfig = prepared.llmConfig,
                    energyBalance = energyBalance.newBalance,
                    userPremiumStatus = userPremiumStatus,
                    energyHistoryRecordId = energyBalance.energyHistoryRecordId
                )
            }

            EnergyDeductionResult.UserNotFound -> {
                return
            }
        }
    }
}
