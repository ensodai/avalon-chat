package app.service.tts.pipeline

import app.service.VertexAuthData
import app.service.withConfiguredProxy
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.shared.avalon.user.models.UserAiSettingsResponse
import app.utils.logger.LlmLogger
import com.google.genai.Client
import com.google.genai.types.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class VertexTtsPipeline(
    private val vertexAuthData: VertexAuthData
) : TtsPipelineProvider {

    private val clientMutex = Mutex()
    @Volatile
    private var cachedClient: Client? = null

    private suspend fun getVertexClient(): Client {
        cachedClient?.let { return it }
        return clientMutex.withLock {
            cachedClient?.let { return it }
            val newClient = Client.builder()
                .vertexAI(true)
                .project(vertexAuthData.projectId)
                .location("us-central1")
                .credentials(vertexAuthData.credentials)
                .withConfiguredProxy()
                .build()
            cachedClient = newClient
            newClient
        }
    }

    private fun cleanTextForTts(rawText: String): String {
        if (rawText.isBlank()) return ""

        var cleaned = rawText
            .replace(Regex("""\*.*?\*"""), "")
            .replace(Regex("""_.*?_"""), "")

        cleaned = cleaned
            .replace(Regex("""\*\*"""), "")
            .replace(Regex("""__"""), "")
            .replace(Regex("""~~"""), "")
            .replace(Regex("""#+\s+"""), "")
            .replace(Regex("""`"""), "")

        cleaned = cleaned
            .replace(Regex("""[\uD83C-\uDBFF\uDC00-\uDFFF]+"""), "")
            .replace(Regex("""[\u2600-\u27BF]+"""), "")

        cleaned = cleaned
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
    ): Flow<ByteArray> = flow {
        val client = getVertexClient()
        emitAll(generateSpeechWithClient(client = client, text = text, voice = voice, modelName = "gemini-2.5-flash-tts"))
    }.flowOn(Dispatchers.IO)

    fun generateSpeechStreamWithApiKey(
        userId: Uuid,
        text: String,
        voice: VoiceModel,
        aiSettings: UserAiSettingsResponse?,
        apiKey: String,
        ttsModelOverride: String? = null
    ): Flow<ByteArray> = flow {
        val client = Client.builder()
            .apiKey(apiKey)
            .withConfiguredProxy()
            .build()
        try {
            val effectiveModel = ttsModelOverride
                ?.trim()
                ?.removePrefix("google/")
                ?.removePrefix("models/")
                ?.takeIf { it.isNotBlank() }
                ?: "gemini-2.5-flash-preview-tts"

            emitAll(generateSpeechWithClient(client = client, text = text, voice = voice, modelName = effectiveModel))
        } finally {
            try { client.close() } catch (_: Exception) {}
        }
    }.flowOn(Dispatchers.IO)

    private fun generateSpeechWithClient(
        client: Client,
        text: String,
        voice: VoiceModel,
        modelName: String
    ): Flow<ByteArray> = flow {
        val cleanedText = cleanTextForTts(text)
        LlmLogger.inf("VertexTtsPipeline", "📝 Text sizes: original=${text.length} chars, cleaned=${cleanedText.length} chars")
        if (cleanedText.isBlank()) {
            LlmLogger.inf("VertexTtsPipeline", "📝 Text for TTS is empty after cleaning (likely actions or emojis only). Skipping generation.")
            return@flow
        }

        val hasCyrillic = cleanedText.any { it in 'а'..'я' || it in 'А'..'Я' }
        val langCode = if (hasCyrillic) "ru-RU" else "en-US"

        LlmLogger.inf("VertexTtsPipeline", "🔊 Starting Gemini-TTS (model: $modelName, length: ${cleanedText.length} chars) with voice: ${voice.modelName} (Language: $langCode)")

        val responseBytes = try {
            runInterruptible(Dispatchers.IO) {
                val content = Content.builder()
                    .role("user")
                    .parts(listOf(Part.fromText(cleanedText)))
                    .build()

                val safetySettings = listOf(
                    SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_HARASSMENT)
                        .threshold(HarmBlockThreshold.Known.OFF)
                        .build(),
                    SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_HATE_SPEECH)
                        .threshold(HarmBlockThreshold.Known.OFF)
                        .build(),
                    SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_SEXUALLY_EXPLICIT)
                        .threshold(HarmBlockThreshold.Known.OFF)
                        .build(),
                    SafetySetting.builder()
                        .category(HarmCategory.Known.HARM_CATEGORY_DANGEROUS_CONTENT)
                        .threshold(HarmBlockThreshold.Known.OFF)
                        .build()
                )

                val config = GenerateContentConfig.builder()
                    .responseModalities(listOf("AUDIO"))
                    .safetySettings(safetySettings)
                    .speechConfig(
                        SpeechConfig.builder()
                            .languageCode(langCode)
                            .voiceConfig(
                                VoiceConfig.builder()
                                    .prebuiltVoiceConfig(
                                        PrebuiltVoiceConfig.builder()
                                            .voiceName(voice.modelName)
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build()

                val response = client.models.generateContent(
                    modelName,
                    content,
                    config
                )

                val audioBytes = response.candidates().orElse(null)?.firstOrNull()
                    ?.content()?.orElse(null)?.parts()?.orElse(null)?.firstOrNull()
                    ?.inlineData()?.orElse(null)?.data()?.orElse(null)

                audioBytes ?: throw IllegalStateException("Gemini-TTS returned empty audio response")
            }
        } catch (e: Exception) {
            LlmLogger.error("VertexTtsPipeline", "❌ Gemini-TTS generation failed: ${e.message}")
            e.printStackTrace()
            throw e
        }

        if (responseBytes.isNotEmpty()) {
            LlmLogger.inf("VertexTtsPipeline", "✅ Gemini-TTS completed. Received total ${responseBytes.size} bytes.")
            emit(responseBytes)
        } else {
            LlmLogger.error("VertexTtsPipeline", "❌ Gemini-TTS returned empty audio bytes.")
        }
    }
}
