package app.domain.chat
import org.koin.core.annotation.Single
import com.ensodai.shared.avalon.chat.wss.ServerMessage

import app.data.repository.GiftsRepository
import app.service.chat.*
import app.domain.chat.*
import app.service.ChatActionType
import app.application.chat.PreparedSendMessageContext
import app.data.models.EnergyDeductionResult
import com.ensodai.shared.avalon.chat.services.MessageContentCodec
import com.ensodai.shared.avalon.chat.models.StickerGiftPayload
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.gifts.models.GiftEffectType
import app.data.repository.UserRepository
import app.utils.toServerUrl
import kotlin.uuid.Uuid
import app.domain.chat.billing.BillingRouter
import app.domain.chat.billing.GenerationActionType
import app.domain.chat.billing.RouteResult

data class PreparedOutgoingUserMessage(
    val finalContentText: String,
    val energyBalance: EnergyDeductionResult
)

@Single
class PrepareOutgoingUserMessageService(
    private val giftsRepository: GiftsRepository,
    private val userRepository: UserRepository,
    private val billingRouter: BillingRouter
) {

    suspend fun prepare(
        prepared: PreparedSendMessageContext
    ): PreparedOutgoingUserMessage {
        val actor = prepared.actor
        val command = prepared.command

        return if (command.contentType == AvalonMessageContentType.GIFT) {
            prepareGiftMessage(prepared)
        } else {

            val route = billingRouter.checkRoute(actor.userId, GenerationActionType.TEXT_REPLY)
            val balance = when(route) {
                is RouteResult.Error -> EnergyDeductionResult.RouteError(route.eventType)
                is RouteResult.FreeCustomKey -> EnergyDeductionResult.Success(newBalance = app.data.models.UserEnergyBalance(-1, -1), energyHistoryRecordId = null)
                is RouteResult.ServerEnergy -> userRepository.deductAndGetBalance(
                    userId = actor.userId,
                    llmConfig = prepared.llmConfig,
                    actionType = ChatActionType.REPLY,
                    contentId = command.contentId,
                    metaInfo = mapOf("chat_session_id" to command.chatSessionId.toString()),
                )
            }
            PreparedOutgoingUserMessage(
                finalContentText = command.text,
                energyBalance = balance
            )

        }
    }

    private suspend fun prepareGiftMessage(
        prepared: PreparedSendMessageContext
    ): PreparedOutgoingUserMessage {
        val actor = prepared.actor
        val command = prepared.command

        val giftId = Uuid.parse(command.text)
        val gift = giftsRepository.getGiftById(giftId) ?: error("Gift not found")

        // Проверяем роутинг (фейс-контроль биллинга)
        val route = billingRouter.checkRoute(actor.userId, GenerationActionType.IMAGE_GIFT)
        
        val energyBalance = when (route) {
            is RouteResult.Error -> {
                EnergyDeductionResult.RouteError(route.eventType)
            }
            is RouteResult.FreeCustomKey -> {
                // Полный безлимит для своих ключей! Подарки бесплатно.
                EnergyDeductionResult.Success(newBalance = app.data.models.UserEnergyBalance(-1, -1), energyHistoryRecordId = null)
            }
            is RouteResult.ServerEnergy -> {
                userRepository.deductEnergyForGift(
                    userId = actor.userId,
                    cost = gift.price,
                    giftId = gift.id,
                    contentId = command.contentId,
                    metaInfo = mapOf("chat_session_id" to command.chatSessionId.toString())
                )
            }
        }

        // Эффект и сохранение пака применяются только при успешном списании
        val effectToUse = if (energyBalance is EnergyDeductionResult.Success) {
            giftsRepository.savePackForUser(actor.userId, gift.packId)
            gift.effectType
        } else {
            GiftEffectType.NONE
        }

        val snapshot = StickerGiftPayload(
            giftId = gift.id,
            packId = gift.packId,
            giftName = gift.giftName,
            url = gift.fileUrl.toServerUrl(),
            actionType = gift.actionType,
            effectType = effectToUse
        )

        return PreparedOutgoingUserMessage(
            finalContentText = MessageContentCodec.encode(
                type = AvalonMessageContentType.GIFT,
                payload = snapshot
            ),
            energyBalance = energyBalance
        )
    }
}
