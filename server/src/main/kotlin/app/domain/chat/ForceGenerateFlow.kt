package app.domain.chat

import app.data.models.EnergyDeductionResult
import app.data.repository.ChatRepository
import app.data.repository.UserRepository
import app.domain.chat.billing.BillingRouter
import app.domain.chat.billing.GenerationActionType
import app.domain.chat.billing.RouteResult
import app.service.ChatActionType
import app.service.LlmConfigService
import app.service.chat.MessageNotifier
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class ForceGenerateFlow(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val llmConfigService: LlmConfigService,
    private val quotaAlertService: QuotaAlertService,
    private val chatGenerationService: ChatGenerationService,
    private val billingRouter: BillingRouter,
    private val messageNotifier: MessageNotifier
) {

    suspend fun run(
        userId: Uuid,
        chatSessionId: Uuid,
        chatVariantId: Uuid,
        characterId: Uuid,
        llmModelCode: String?
    ) {
        val userPremiumStatus = userRepository.getUserPremiumStatus(userId)
        val llmConfig = llmConfigService.getConfig(llmModelCode, userPremiumStatus)
        val giftPrice = chatRepository.getLastGiftInfo(chatVariantId = chatVariantId)

        val actionType = if (giftPrice != null) {
            GenerationActionType.IMAGE_GIFT
        } else {
            GenerationActionType.TEXT_REPLY
        }
        val route = billingRouter.checkRoute(userId, actionType)

        val energyBalance = when (route) {
            is RouteResult.Error -> EnergyDeductionResult.RouteError(route.eventType)
            is RouteResult.FreeCustomKey -> EnergyDeductionResult.Success(
                newBalance = app.data.models.UserEnergyBalance(-1, -1),
                energyHistoryRecordId = null
            )
            is RouteResult.ServerEnergy -> when {
                giftPrice != null -> {
                    userRepository.deductEnergyForGift(
                        userId = userId,
                        cost = giftPrice.price,
                        giftId = giftPrice.giftId,
                        contentId = null,
                        metaInfo = mapOf("chat_session_id" to chatSessionId.toString())
                    )
                }
                else -> {
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

        when (energyBalance) {
            is EnergyDeductionResult.InsufficientFunds -> {
                quotaAlertService.notifyQuotaExceededIfNeeded(
                    userId = userId,
                    chatSessionId = chatSessionId,
                    chatVariantId = chatVariantId,
                    characterId = characterId,
                    currentBalance = energyBalance.currentBalance
                )
            }

            is EnergyDeductionResult.Success -> {
                chatGenerationService.launchGenerationJob(
                    userId = userId,
                    chatSessionId = chatSessionId,
                    variantId = chatVariantId,
                    characterId = characterId,
                    preValidatedConfig = null,
                    targetSlotId = null,
                    llmConfig = llmConfig,
                    energyBalance = energyBalance.newBalance,
                    userPremiumStatus = userPremiumStatus,
                    energyHistoryRecordId = energyBalance.energyHistoryRecordId
                )
            }

            is EnergyDeductionResult.RouteError -> {
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
                return
            }
        }
    }
}
