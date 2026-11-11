package app.domain.chat
import com.ensodai.shared.avalon.gifts.models.*
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import org.koin.core.annotation.Single

import app.service.chat.*
import app.domain.chat.*
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import app.data.models.UserEnergyBalance
import com.ensodai.shared.avalon.chat.models.SystemEventType
import app.data.repository.ChatRepository
import app.data.repository.UserRepository
import app.service.chat.MessageNotifier
import app.utils.logger.WssLogger
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit
import kotlin.uuid.Uuid

/**
 * Сервис отвечает за уведомление пользователя о том, что закончилась энергия.
 * Содержит логику троттлинга (не чаще 1 раза в минуту для одного варианта чата).
 */
@Single
class QuotaAlertService(
    private val chatRepository: ChatRepository,
    private val messageNotifier: MessageNotifier,
    private val userRepository: UserRepository
) {

    private val quotaAlertThrottler = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build<Uuid, Boolean>()

    /**
     * Возвращает true, если уведомление было отправлено.
     * Возвращает false, если сработал троттлинг (уже предупреждали).
     */
    suspend fun notifyQuotaExceededIfNeeded(
        userId: Uuid,
        chatSessionId: Uuid,
        chatVariantId: Uuid,
        characterId: Uuid,
        currentBalance: UserEnergyBalance
    ): Boolean {
        // 1. Проверяем троттлинг
        val alreadyWarned = quotaAlertThrottler.getIfPresent(chatVariantId) == true
        if (alreadyWarned) {
            WssLogger.info("Quota exceeded, but warning already sent recently. Skipping.")
            return false
        }

        val userPremiumStatus = userRepository.getUserPremiumStatus(userId)

        // 2. Создаем системное сообщение в БД
        val systemMessage = if (userPremiumStatus == PremiumStatus.FREE) {
            chatRepository.createSystemMessage(
                userId = userId,
                chatSessionId = chatSessionId,
                chatVariantId = chatVariantId,
                eventType = SystemEventType.QUOTA_EXCEEDED
            )
        } else {
            chatRepository.createSystemMessage(
                userId = userId,
                chatSessionId = chatSessionId,
                chatVariantId = chatVariantId,
                eventType = SystemEventType.QUOTA_EXCEEDED
            )
        }

        // 3. Уведомляем (excludeSessionId = null, так как это важный алерт для всех)
        WssLogger.info("Отправка алерта о квоте")
        messageNotifier.notifyUserWithMessage(
            userId = userId,
            excludeSessionId = null,
            message = ServerMessage.NewChatMessage(
                characterId = characterId,
                slot = systemMessage,
                currentUserQuota = currentBalance.dailyEnergy,
                reserveEnergy = currentBalance.reserveEnergy,
            )
        )

        // 4. Запоминаем
        quotaAlertThrottler.put(chatVariantId, true)
        return true
    }
}