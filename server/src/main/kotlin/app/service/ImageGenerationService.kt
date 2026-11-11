package app.service

import com.ensodai.shared.avalon.gifts.models.GiftActionType
import com.ensodai.shared.avalon.chat.models.ImagePayload
import app.data.models.AICharacterConfigLLMDto
import app.domain.chat.billing.AiExecutionStrategy
import app.domain.chat.billing.AiStrategyResolver
import app.domain.models.DomainMessage
import app.domain.user.usecases.GetUserAiSettingsUseCase
import app.service.image.TokenUsage
import app.service.ImageSaver
import app.service.image.pipeline.VertexImagePipeline
import app.service.image.pipeline.OpenRouterImagePipeline
import app.utils.logger.LlmLogger
import app.utils.toServerUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

sealed class ImageGenerationResult {
    data class Success(
        val imagePayload: ImagePayload,
        val description: String,
        val tokenUsage: TokenUsage = TokenUsage.ZERO
    ) : ImageGenerationResult()
    data class Error(val reason: ErrorReason, val exception: Throwable? = null) : ImageGenerationResult() {
        enum class ErrorReason {
            SAFETY_VIOLATION,
            PERSON_GENERATION_UNSUPPORTED,
            UNKNOWN
        }
    }
}

@Single
class ImageGenerationService(
    private val getUserAiSettingsUseCase: GetUserAiSettingsUseCase,
    private val aiStrategyResolver: AiStrategyResolver,
    private val vertexImagePipeline: VertexImagePipeline,
    private val openRouterImagePipeline: OpenRouterImagePipeline,
    private val imageSaver: ImageSaver
) {

    suspend fun generateImage(
        aICharacterConfigLLMDto: AICharacterConfigLLMDto,
        giftName: String,
        history: List<DomainMessage>,
        actionType: GiftActionType
    ): ImageGenerationResult {
        val reqId = "gen-${aICharacterConfigLLMDto.userId}-${System.currentTimeMillis()}"

        return try {
            val aiSettings = getUserAiSettingsUseCase.execute(aICharacterConfigLLMDto.userId)
            val strategy = aiStrategyResolver.resolveStrategy(aICharacterConfigLLMDto.userId)

            val generatedData = when (strategy) {
                is AiExecutionStrategy.ServerVertex -> {
                    LlmLogger.inf(reqId, "Routing to VertexImagePipeline (ServerVertex)")
                    vertexImagePipeline.generate(aICharacterConfigLLMDto, giftName, history, actionType, reqId, aiSettings)
                }
                is AiExecutionStrategy.CustomProvider -> {
                    if (strategy.provider == com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE && strategy.apiKey.isNotBlank()) {
                        LlmLogger.inf(reqId, "Routing to VertexImagePipeline (Google AI Studio BYOK)")
                        vertexImagePipeline.generateWithApiKey(
                            configDto = aICharacterConfigLLMDto,
                            giftName = giftName,
                            history = history,
                            actionType = actionType,
                            reqId = reqId,
                            aiSettings = aiSettings,
                            apiKey = strategy.apiKey,
                            imageModelOverride = strategy.imageModel
                        )
                    } else {
                        LlmLogger.inf(reqId, "Routing to OpenRouterImagePipeline (CustomProvider BYOK)")
                        openRouterImagePipeline.generate(aICharacterConfigLLMDto, giftName, history, actionType, reqId, aiSettings)
                    }
                }
            }

            val savedPathResult = imageSaver.saveGeneratedImage(generatedData.imageBytes, aICharacterConfigLLMDto.userId)
            val imagePayload = ImagePayload(
                id = Uuid.random(),
                url = savedPathResult.path.toServerUrl(),
                width = savedPathResult.width,
                height = savedPathResult.height,
                blurhash = savedPathResult.thumbHash,
                sizeBytes = savedPathResult.sizeBytes
            )

            ImageGenerationResult.Success(
                imagePayload = imagePayload,
                description = generatedData.visualDescription,
                tokenUsage = generatedData.tokenUsage
            )
        } catch (e: Exception) {
            LlmLogger.error(reqId, "Image generation error: ${e.message}")
            val reason = when {
                e.message?.contains("SAFETY", ignoreCase = true) == true -> ImageGenerationResult.Error.ErrorReason.SAFETY_VIOLATION
                e.message?.contains("person", ignoreCase = true) == true -> ImageGenerationResult.Error.ErrorReason.PERSON_GENERATION_UNSUPPORTED
                else -> ImageGenerationResult.Error.ErrorReason.UNKNOWN
            }
            ImageGenerationResult.Error(reason, e)
        }
    }
}
