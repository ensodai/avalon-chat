package app.domain.chat
import com.ensodai.shared.avalon.gifts.models.*
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import org.koin.core.annotation.Single

import app.service.chat.*
import app.domain.chat.*
import app.service.ChatActionType
import app.service.LlmConfigService
import app.data.models.EnergyDeductionResult
import app.data.repository.ChatRepository
import app.data.repository.UserRepository
import app.utils.logger.WssLogger
import kotlin.uuid.Uuid

@Single
class GenerateVariantFlow(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val llmConfigService: LlmConfigService,
    private val quotaAlertService: QuotaAlertService,
    private val chatContextService: ChatContextService,
    private val chatGenerationService: ChatGenerationService,
    private val billingRouter: app.domain.chat.billing.BillingRouter,
    private val messageNotifier: app.service.chat.MessageNotifier
) {

    suspend fun run(
        userId: Uuid,
        chatSessionId: Uuid,
        chatVariantId: Uuid,
        characterId: Uuid,
        slotId: Uuid,
        llmModelCode: String?
    ) {
        WssLogger.info("[GenerateVariantFlow] Начало выполнения run: userId=$userId, chatSessionId=$chatSessionId, chatVariantId=$chatVariantId, slotId=$slotId")
        try {
            val userPremiumStatus = userRepository.getUserPremiumStatus(userId)
            WssLogger.info("[GenerateVariantFlow] Премиум статус пользователя: $userPremiumStatus")

            val llmConfig = llmConfigService.getConfig(llmModelCode, userPremiumStatus)
            WssLogger.info("[GenerateVariantFlow] Получен llmConfig: code=${llmConfig.code}, cost=${llmConfig.energyCost}")

            val giftPrice = chatRepository.getPreviousMessageGiftInfo(
                chatVariantId = chatVariantId,
                slotId = slotId
            )
            WssLogger.info("[GenerateVariantFlow] Информация о предыдущем подарке: $giftPrice")

            val actionType = if (giftPrice != null) {
                app.domain.chat.billing.GenerationActionType.IMAGE_GIFT
            } else {
                app.domain.chat.billing.GenerationActionType.TEXT_REPLY
            }
            val route = billingRouter.checkRoute(userId, actionType)

            val energyBalance = when (route) {
                is app.domain.chat.billing.RouteResult.Error -> EnergyDeductionResult.RouteError(route.eventType)
                is app.domain.chat.billing.RouteResult.FreeCustomKey -> EnergyDeductionResult.Success(
                    newBalance = app.data.models.UserEnergyBalance(-1, -1),
                    energyHistoryRecordId = null
                )
                is app.domain.chat.billing.RouteResult.ServerEnergy -> when {
                    giftPrice != null -> {
                        WssLogger.info("[GenerateVariantFlow] Списание энергии за подарок: cost=${giftPrice.price}, giftId=${giftPrice.giftId}")
                        userRepository.deductEnergyForGift(
                            userId = userId,
                            cost = giftPrice.price,
                            giftId = giftPrice.giftId,
                            contentId = null,
                            metaInfo = mapOf("chat_session_id" to chatSessionId.toString())
                        )
                    }
                    else -> {
                        WssLogger.info("[GenerateVariantFlow] Списание энергии за обычную регенерацию чата")
                        userRepository.deductAndGetBalance(
                            userId = userId,
                            llmConfig = llmConfig,
                            actionType = ChatActionType.REGEN,
                            contentId = null,
                            metaInfo = mapOf("chat_session_id" to chatSessionId.toString()),
                        )
                    }
                }
            }
            WssLogger.info("[GenerateVariantFlow] Результат списания энергии: $energyBalance")

            WssLogger.info("[GenerateVariantFlow] Валидация контекста персонажа")
            val characterLLMConfig = chatContextService.getOrCreateContext(
                userId = userId,
                chatSessionId = chatSessionId,
                chatVariantId = chatVariantId,
                characterId = characterId,
                llmConfig = llmConfig
            )
            WssLogger.info("[GenerateVariantFlow] Результат getOrCreateContext: $characterLLMConfig")
            if (characterLLMConfig == null) {
                WssLogger.fail("[GenerateVariantFlow] Контекст персонажа равен null! Прерывание флоу.")
                return
            }

            when (energyBalance) {
                is EnergyDeductionResult.InsufficientFunds -> {
                    WssLogger.fail("[GenerateVariantFlow] Недостаточно энергии для пользователя $userId. Баланс daily=${energyBalance.currentBalance.dailyEnergy}, reserve=${energyBalance.currentBalance.reserveEnergy}. Отправка алерта квоты.")
                    quotaAlertService.notifyQuotaExceededIfNeeded(
                        userId = userId,
                        chatSessionId = chatSessionId,
                        chatVariantId = chatVariantId,
                        characterId = characterId,
                        currentBalance = energyBalance.currentBalance
                    )
                }

                is EnergyDeductionResult.Success -> {
                    WssLogger.info("[GenerateVariantFlow] Списание успешно. Запуск джобы генерации.")
                    chatGenerationService.launchGenerationJob(
                        userId = userId,
                        chatSessionId = chatSessionId,
                        variantId = chatVariantId,
                        characterId = characterId,
                        preValidatedConfig = characterLLMConfig,
                        targetSlotId = slotId,
                        llmConfig = llmConfig,
                        energyBalance = energyBalance.newBalance,
                        userPremiumStatus = userPremiumStatus,
                        energyHistoryRecordId = energyBalance.energyHistoryRecordId
                    )
                    WssLogger.info("[GenerateVariantFlow] launchGenerationJob успешно вызван")
                }

                is EnergyDeductionResult.RouteError -> {
                    WssLogger.info("[GenerateVariantFlow] Ошибка маршрутизации биллинга: ${energyBalance.eventType}")
                    val systemMessage = chatRepository.createSystemMessage(
                        userId = userId,
                        chatSessionId = chatSessionId,
                        chatVariantId = chatVariantId,
                        eventType = energyBalance.eventType
                    )
                    messageNotifier.notifyUserWithMessage(
                        userId = userId,
                        message = ServerMessage.NewChatMessage(
                            characterId = characterId,
                            slot = systemMessage,
                            currentUserQuota = -1,
                            reserveEnergy = 0,
                        )
                    )
                }
                EnergyDeductionResult.UserNotFound -> {
                    WssLogger.fail("[GenerateVariantFlow] Пользователь не найден в системе: $userId")
                    return
                }
            }
        } catch (e: Throwable) {
            WssLogger.fail("[GenerateVariantFlow] КРИТИЧЕСКОЕ ИСКЛЮЧЕНИЕ: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
