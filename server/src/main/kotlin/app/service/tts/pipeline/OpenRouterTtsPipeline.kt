package app.service.tts.pipeline

import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.shared.avalon.user.models.UserAiSettingsResponse
import app.data.network.models.openai.OpenAiTtsRequest
import app.service.openai.OpenAiCompatibleClient
import app.utils.logger.LlmLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class OpenRouterTtsPipeline(
    private val openAiCompatibleClient: OpenAiCompatibleClient
) : TtsPipelineProvider {

    private fun cleanTextForTts(rawText: String): String {
        if (rawText.isBlank()) return ""
        var cleaned = rawText
            .replace(Regex("""\*.*?\*"""), "")
            .replace(Regex("""_.*?_"""), "")
            .replace(Regex("""\*\*"""), "")
            .replace(Regex("""__"""), "")
            .replace(Regex("""~~"""), "")
            .replace(Regex("""#+\s+"""), "")
            .replace(Regex("""`"""), "")
            .replace(Regex("""[\uD83C-\uDBFF\uDC00-\uDFFF]+"""), "")
            .replace(Regex("""[\u2600-\u27BF]+"""), "")
            .replace(Regex("""\n+"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
        return cleaned
    }

    override fun generateSpeechStream(
        userId: Uuid,
        text: String,
        voice: VoiceModel,
        aiSettings: UserAiSettingsResponse?
    ): Flow<ByteArray> {
        if (aiSettings == null || aiSettings.apiKey.isNullOrBlank()) {
            throw Exception("OpenRouter TTS settings are missing or API key is empty")
        }

        val cleanedText = cleanTextForTts(text)
        LlmLogger.inf("OpenRouterTtsPipeline", "📝 Text sizes: original=${text.length}, cleaned=${cleanedText.length}")
        
        if (cleanedText.isBlank()) {
            LlmLogger.inf("OpenRouterTtsPipeline", "📝 Text empty after cleaning. Skipping.")
            return flow { }
        }

        val resolvedVoice = resolveOpenAiVoice(voice)
        LlmLogger.inf("OpenRouterTtsPipeline", "🎙️ Mapping voice '${voice.modelName}' (${voice.gender}) -> '$resolvedVoice' for model '${aiSettings.ttsModel}'")

        val request = OpenAiTtsRequest(
            model = aiSettings.ttsModel,
            input = cleanedText,
            voice = resolvedVoice,
            responseFormat = "pcm"
        )

        val reqId = "tts-${userId}-${System.currentTimeMillis()}"
        return openAiCompatibleClient.generateSpeechStream(
            baseUrl = aiSettings.baseUrl,
            apiKey = aiSettings.apiKey!!,
            request = request,
            reqId = reqId
        )
    }

    private fun resolveOpenAiVoice(voice: VoiceModel): String {
        val modelLower = voice.modelName.lowercase()
        if (modelLower in OPENAI_VOICES) {
            return modelLower
        }
        return if (voice.gender == VoiceModel.Gender.FEMALE) "nova" else "alloy"
    }

    companion object {
        private val OPENAI_VOICES = setOf(
            "alloy", "echo", "fable", "onyx", "nova", "shimmer",
            "ash", "coral", "sage", "verse", "ballad", "marin", "cedar"
        )
    }
}
