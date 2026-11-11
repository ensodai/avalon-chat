package app.domain.chat.billing

import app.data.repository.UserRepository
import app.data.repository.UserAiSettingsRepository
import app.service.ai.AiProviderService
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

sealed class AiExecutionStrategy {
    // Серверный Vertex AI (наши серверные мощности + списание энергии)
    data object ServerVertex : AiExecutionStrategy()

    // Кастомный провайдер (BYOK: OpenRouter / Google AI Studio / собственный ключ пользователя)
    data class CustomProvider(
        val provider: com.ensodai.shared.avalon.user.models.AiProviderType = com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE,
        val apiKey: String,
        val baseUrl: String?,
        val textModel: String?,
        val imageModel: String?,
        val imagePromptModel: String?,
        val ttsModel: String?,
        val summaryModel: String?
    ) : AiExecutionStrategy() {
        val providerId: String get() = provider.id
    }
}

@Single
class AiStrategyResolver(
    private val userRepository: UserRepository,
    private val userAiSettingsRepository: UserAiSettingsRepository,
    private val aiProviderService: AiProviderService
) {
    /**
     * Мастер-переключатель "Day-Z":
     * - false: Обычный режим (все пользователи могут использовать Vertex AI со списанием энергии, если не включен свой ключ)
     * - true: Режим перехода (Vertex AI доступен ТОЛЬКО активным платным подписчикам; все Free пользователи обязаны использовать свой ключ)
     */
    var isDayZ: Boolean = true

    /**
     * Список UUID админов / тестировщиков, для которых режим Day-Z включен ВСЕГДА (даже при глобальном isDayZ = false).
     */
    val adminTestUserIds: MutableSet<Uuid> = mutableSetOf(
        Uuid.parse("8bd56bfb-54e2-4d44-b1c7-be6bc29cb99d")
    )

    suspend fun resolveStrategy(userId: Uuid): AiExecutionStrategy {
        val settings = userAiSettingsRepository.getOrCreateSettings(userId)
        val resolvedBaseUrl = aiProviderService.resolveBaseUrl(settings.providerId)

        // 1. Если у пользователя включен кастомный провайдер и заполнен API-ключ
        if (settings.isCustomProviderEnabled && !settings.apiKey.isNullOrBlank()) {
            return AiExecutionStrategy.CustomProvider(
                provider = settings.providerId,
                apiKey = settings.apiKey!!,
                baseUrl = resolvedBaseUrl,
                textModel = settings.textModel,
                imageModel = settings.imageModel,
                imagePromptModel = settings.imagePromptModel,
                ttsModel = settings.ttsModel,
                summaryModel = settings.summaryModel
            )
        }

        // 2. Проверяем режим Day-Z (глобальный флаг ИЛИ персональный флаг для админа-тестировщика)
        val isDayZForThisUser = isDayZ || (userId in adminTestUserIds)

        if (isDayZForThisUser) {
            val isPaidSubscriber = if (userId in adminTestUserIds) {
                // Для админа-тестировщика игнорируем премиум статус для чистоты проверки BYOK
                false
            } else {
                userRepository.getUserPremiumStatus(userId) != PremiumStatus.FREE
            }

            if (isPaidSubscriber) {
                // Платный подписчик без своего ключа продолжает использовать серверный Vertex AI
                return AiExecutionStrategy.ServerVertex
            } else {
                // Free-пользователь в режиме Day-Z обязан использовать собственный ключ
                return if (!settings.apiKey.isNullOrBlank()) {
                    AiExecutionStrategy.CustomProvider(
                        provider = settings.providerId,
                        apiKey = settings.apiKey!!,
                        baseUrl = resolvedBaseUrl,
                        textModel = settings.textModel,
                        imageModel = settings.imageModel,
                        imagePromptModel = settings.imagePromptModel,
                        ttsModel = settings.ttsModel,
                        summaryModel = settings.summaryModel
                    )
                } else {
                    // Ключ не задан -> передаем CustomProvider с пустым apiKey для блокировки роутером
                    AiExecutionStrategy.CustomProvider(
                        provider = settings.providerId,
                        apiKey = "",
                        baseUrl = resolvedBaseUrl,
                        textModel = settings.textModel,
                        imageModel = settings.imageModel,
                        imagePromptModel = settings.imagePromptModel,
                        ttsModel = settings.ttsModel,
                        summaryModel = settings.summaryModel
                    )
                }
            }
        } else {
            // Режим до наступления Day-Z (isDayZ = false для обычного пользователя):
            // Все пользователи могут ходить в Vertex AI с серверной энергией
            return AiExecutionStrategy.ServerVertex
        }
    }
}
