package app.domain.user.usecases

import app.data.repository.UserAiSettingsRepository
import app.service.ai.AiProviderService
import com.ensodai.shared.avalon.user.models.UserAiSettingsResponse
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class GetUserAiSettingsUseCase(
    private val userAiSettingsRepository: UserAiSettingsRepository,
    private val aiProviderService: AiProviderService
) {
    suspend fun execute(userId: Uuid): UserAiSettingsResponse {
        val dto = userAiSettingsRepository.getOrCreateSettings(userId)
        val availableProviders = aiProviderService.getActiveProviders()
        val resolvedBaseUrl = aiProviderService.resolveBaseUrl(dto.providerId)

        return UserAiSettingsResponse(
            userId = dto.userId,
            providerId = dto.providerId.id,
            availableProviders = availableProviders,
            baseUrl = resolvedBaseUrl,
            apiKey = dto.apiKey,
            textModel = dto.textModel,
            ttsModel = dto.ttsModel,
            imageModel = dto.imageModel,
            imagePromptModel = dto.imagePromptModel,
            summaryModel = dto.summaryModel,
            isCustomProviderEnabled = dto.isCustomProviderEnabled
        )
    }
}
