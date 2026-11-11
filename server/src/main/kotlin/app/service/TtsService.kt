package app.service

import app.domain.chat.billing.AiExecutionStrategy
import app.domain.chat.billing.AiStrategyResolver
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.shared.avalon.user.models.UserAiSettingsResponse
import app.service.tts.pipeline.VertexTtsPipeline
import app.service.tts.pipeline.OpenRouterTtsPipeline
import app.utils.logger.LlmLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class TtsService(
    private val aiStrategyResolver: AiStrategyResolver,
    private val vertexTtsPipeline: VertexTtsPipeline,
    private val openRouterTtsPipeline: OpenRouterTtsPipeline
) {

    /**
     * Генерирует аудиопоток (LINEAR16 PCM 24000 Hz) для переданного текста и модели голоса персонажа.
     */
    fun generateSpeechStream(
        userId: Uuid,
        text: String, 
        voice: VoiceModel,
        aiSettings: UserAiSettingsResponse?
    ): Flow<ByteArray> = flow {
        val strategy = aiStrategyResolver.resolveStrategy(userId)

        val stream = when (strategy) {
            is AiExecutionStrategy.ServerVertex -> {
                LlmLogger.inf("TtsService", "Routing TTS to VertexTtsPipeline (ServerVertex)")
                vertexTtsPipeline.generateSpeechStream(userId, text, voice, aiSettings)
            }
            is AiExecutionStrategy.CustomProvider -> {
                if (strategy.provider == com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE && strategy.apiKey.isNotBlank()) {
                    LlmLogger.inf("TtsService", "Routing TTS to VertexTtsPipeline (Google AI Studio BYOK)")
                    vertexTtsPipeline.generateSpeechStreamWithApiKey(
                        userId = userId,
                        text = text,
                        voice = voice,
                        aiSettings = aiSettings,
                        apiKey = strategy.apiKey,
                        ttsModelOverride = strategy.ttsModel
                    )
                } else {
                    LlmLogger.inf("TtsService", "Routing TTS to OpenRouterTtsPipeline (CustomProvider BYOK)")
                    openRouterTtsPipeline.generateSpeechStream(userId, text, voice, aiSettings)
                }
            }
        }

        emitAll(stream)
    }
}
