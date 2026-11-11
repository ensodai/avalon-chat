package app.domain.user.usecases

import app.data.repository.UserAiSettingsRepository
import app.service.ai.AiModelCatalogService
import app.service.ai.AiProviderService
import com.ensodai.shared.avalon.user.models.AiModelCategory
import com.ensodai.shared.avalon.user.models.AiProviderType
import com.ensodai.shared.avalon.user.models.UpdateUserAiSettingsRequest
import com.ensodai.shared.avalon.user.models.UserAiSettingsResponse
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class UpdateUserAiSettingsUseCase(
    private val userAiSettingsRepository: UserAiSettingsRepository,
    private val aiProviderService: AiProviderService,
    private val aiModelCatalogService: AiModelCatalogService
) {
    suspend fun execute(request: UpdateUserAiSettingsRequest, userId: Uuid): UserAiSettingsResponse {
        val providerType = AiProviderType.fromId(request.providerId)
        val resolvedBaseUrl = aiProviderService.resolveBaseUrl(providerType)
        val existing = userAiSettingsRepository.getSettingsByUserId(userId)
        val isProviderChanged = existing == null || existing.providerId != providerType

        val defaultText = aiModelCatalogService.getRecommendedModel(providerType, AiModelCategory.TEXT)
        val defaultTts = aiModelCatalogService.getRecommendedModel(providerType, AiModelCategory.TTS)
        val defaultImage = aiModelCatalogService.getRecommendedModel(providerType, AiModelCategory.IMAGE)
        val defaultImagePrompt = aiModelCatalogService.getRecommendedModel(providerType, AiModelCategory.IMAGE_PROMPT)
        val defaultSummary = aiModelCatalogService.getRecommendedModel(providerType, AiModelCategory.SUMMARY)

        val validText = aiModelCatalogService.getModels(providerType, AiModelCategory.TEXT).map { it.id }.toSet()
        val validTts = aiModelCatalogService.getModels(providerType, AiModelCategory.TTS).map { it.id }.toSet()
        val validImage = aiModelCatalogService.getModels(providerType, AiModelCategory.IMAGE).map { it.id }.toSet()
        val validPrompt = aiModelCatalogService.getModels(providerType, AiModelCategory.IMAGE_PROMPT).map { it.id }.toSet()
        val validSummary = aiModelCatalogService.getModels(providerType, AiModelCategory.SUMMARY).map { it.id }.toSet()

        val textModel = if (isProviderChanged) {
            request.textModel?.takeIf { it in validText } ?: defaultText
        } else {
            request.textModel?.takeIf { it in validText } ?: existing?.textModel?.takeIf { it in validText } ?: defaultText
        }

        val ttsModel = if (isProviderChanged) {
            request.ttsModel?.takeIf { it in validTts } ?: defaultTts
        } else {
            request.ttsModel?.takeIf { it in validTts } ?: existing?.ttsModel?.takeIf { it in validTts } ?: defaultTts
        }

        val imageModel = if (isProviderChanged) {
            request.imageModel?.takeIf { it in validImage } ?: defaultImage
        } else {
            request.imageModel?.takeIf { it in validImage } ?: existing?.imageModel?.takeIf { it in validImage } ?: defaultImage
        }

        val imagePromptModel = if (isProviderChanged) {
            request.imagePromptModel?.takeIf { it in validPrompt } ?: defaultImagePrompt
        } else {
            request.imagePromptModel?.takeIf { it in validPrompt } ?: existing?.imagePromptModel?.takeIf { it in validPrompt } ?: defaultImagePrompt
        }

        val summaryModel = if (isProviderChanged) {
            request.summaryModel?.takeIf { it in validSummary } ?: defaultSummary
        } else {
            request.summaryModel?.takeIf { it in validSummary } ?: existing?.summaryModel?.takeIf { it in validSummary } ?: defaultSummary
        }

        val effectiveApiKey = if (isProviderChanged) {
            request.apiKey
        } else {
            request.apiKey ?: existing?.apiKey
        }

        val effectiveCustomProviderEnabled = if (isProviderChanged) {
            if (!effectiveApiKey.isNullOrBlank()) {
                request.isCustomProviderEnabled ?: true
            } else {
                false
            }
        } else {
            request.isCustomProviderEnabled ?: existing?.isCustomProviderEnabled ?: false
        }

        val updated = userAiSettingsRepository.saveOrUpdateSettings(
            userId = userId,
            providerId = providerType,
            baseUrl = resolvedBaseUrl,
            apiKey = effectiveApiKey,
            textModel = textModel,
            ttsModel = ttsModel,
            imageModel = imageModel,
            imagePromptModel = imagePromptModel,
            summaryModel = summaryModel,
            isCustomProviderEnabled = effectiveCustomProviderEnabled
        )

        val availableProviders = aiProviderService.getActiveProviders()

        return UserAiSettingsResponse(
            userId = updated.userId,
            providerId = updated.providerId.id,
            availableProviders = availableProviders,
            baseUrl = resolvedBaseUrl,
            apiKey = updated.apiKey,
            textModel = updated.textModel,
            ttsModel = updated.ttsModel,
            imageModel = updated.imageModel,
            imagePromptModel = updated.imagePromptModel,
            summaryModel = updated.summaryModel,
            isCustomProviderEnabled = updated.isCustomProviderEnabled
        )
    }
}
