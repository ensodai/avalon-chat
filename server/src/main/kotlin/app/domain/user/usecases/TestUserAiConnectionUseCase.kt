package app.domain.user.usecases

import app.service.ai.AiProviderService
import app.service.openai.OpenAiCompatibleClient
import app.service.withConfiguredProxy
import app.utils.logger.LlmLogger
import app.utils.orNullOpt
import com.ensodai.shared.avalon.user.models.TestUserAiConnectionRequest
import com.ensodai.shared.avalon.user.models.TestUserAiConnectionResponse
import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import org.koin.core.annotation.Factory
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Factory
class TestUserAiConnectionUseCase(
    private val openAiCompatibleClient: OpenAiCompatibleClient,
    private val aiProviderService: AiProviderService
) {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

    suspend fun execute(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse = testChat(request)

    suspend fun testChat(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse =
        performTest(request, "Chat") { effectiveModel, client, resolvedBaseUrl ->
            if (client != null) {
                val content = Content.builder()
                    .role("user")
                    .parts(listOf(Part.fromText("ping")))
                    .build()
                val response = runInterruptible(Dispatchers.IO) {
                    client.models.generateContent(effectiveModel, content, null)
                }
                val text = response.candidates().orNullOpt()?.firstOrNull()
                    ?.content()?.orNullOpt()?.parts()?.orNullOpt()?.firstOrNull()
                    ?.text()?.orNullOpt()
                text?.takeIf { it.isNotBlank() } ?: "OK"
            } else {
                openAiCompatibleClient.testConnection(
                    baseUrl = resolvedBaseUrl,
                    apiKey = request.apiKey,
                    model = request.model
                ).getOrThrow()
            }
        }

    suspend fun testTts(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse =
        performTest(request, "TTS") { effectiveModel, client, resolvedBaseUrl ->
            if (client != null) {
                val config = GenerateContentConfig.builder()
                    .responseModalities(listOf("AUDIO"))
                    .build()
                val content = Content.builder()
                    .role("user")
                    .parts(listOf(Part.fromText("ping")))
                    .build()
                runInterruptible(Dispatchers.IO) {
                    client.models.generateContent(effectiveModel, content, config)
                }
                "OK (Audio synthesized)"
            } else {
                openAiCompatibleClient.testTtsConnection(
                    baseUrl = resolvedBaseUrl,
                    apiKey = request.apiKey,
                    model = request.model
                ).getOrThrow()
            }
        }

    suspend fun testImage(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse =
        performTest(request, "Image") { effectiveModel, client, resolvedBaseUrl ->
            if (client != null) {
                if (effectiveModel.startsWith("imagen-", ignoreCase = true)) {
                    val imgResp = runInterruptible(Dispatchers.IO) {
                        client.models.generateImages(effectiveModel, "a cute robot holding a flower", null)
                    }
                    val imgCount = imgResp.generatedImages().orNullOpt()?.size ?: 0
                    "OK ($imgCount image generated)"
                } else {
                    val config = GenerateContentConfig.builder()
                        .responseModalities(listOf("IMAGE", "TEXT"))
                        .build()
                    val content = Content.builder()
                        .role("user")
                        .parts(listOf(Part.fromText("a cute robot holding a flower")))
                        .build()
                    runInterruptible(Dispatchers.IO) {
                        client.models.generateContent(effectiveModel, content, config)
                    }
                    "OK (Image generated)"
                }
            } else {
                openAiCompatibleClient.testImageConnection(
                    baseUrl = resolvedBaseUrl,
                    apiKey = request.apiKey,
                    model = request.model
                ).getOrThrow()
            }
        }

    private suspend fun performTest(
        request: TestUserAiConnectionRequest,
        testType: String,
        testBlock: suspend (effectiveModel: String, client: Client?, resolvedBaseUrl: String) -> String
    ): TestUserAiConnectionResponse {
        val reqId = "test-${System.currentTimeMillis()}"
        val startTimeStr = LocalTime.now().format(formatter)
        val provider = com.ensodai.shared.avalon.user.models.AiProviderType.fromId(request.providerId)
        val isGoogle = provider == com.ensodai.shared.avalon.user.models.AiProviderType.GOOGLE
        val resolvedBaseUrl = if (isGoogle) "https://generativelanguage.googleapis.com (Google GenAI SDK)" else aiProviderService.resolveBaseUrl(provider)

        val startLog = "$startTimeStr [🧠 LLM] [⚙️ INFO] ($reqId) Testing $testType connection to $resolvedBaseUrl with model ${request.model}"
        LlmLogger.inf(reqId, "Testing $testType connection to $resolvedBaseUrl with model ${request.model}")

        val result = runCatching {
            val effectiveModel = request.model
                .trim()
                .removePrefix("google/")
                .removePrefix("models/")
                .takeIf { it.isNotBlank() }
                ?: if (isGoogle) "gemini-2.5-flash" else request.model

            val client = if (isGoogle) Client.builder().apiKey(request.apiKey).withConfiguredProxy().build() else null
            try {
                testBlock(effectiveModel, client, resolvedBaseUrl)
            } finally {
                if (client != null) {
                    try { client.close() } catch (_: Exception) {}
                }
            }
        }

        val endTimeStr = LocalTime.now().format(formatter)
        return result.fold(
            onSuccess = { responseText ->
                val endLog = "$endTimeStr [🧠 LLM] [✅ SUCCESS] ($reqId) Connected successfully. Model responded: $responseText"
                LlmLogger.response(reqId, "Connection test success (response length: ${responseText.length} chars)")
                TestUserAiConnectionResponse(
                    success = true,
                    message = "$startLog\n$endLog"
                )
            },
            onFailure = { err ->
                val endLog = "$endTimeStr [🧠 LLM] [❌ FAIL] ($reqId) Failed: ${err.message}"
                LlmLogger.error(reqId, "Failed: ${err.message}")
                TestUserAiConnectionResponse(
                    success = false,
                    message = "$startLog\n$endLog"
                )
            }
        )
    }
}
