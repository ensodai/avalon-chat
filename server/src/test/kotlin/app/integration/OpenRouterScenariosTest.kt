package app.integration

import app.data.network.models.openai.*
import app.data.network.openai.OpenAiApiClient
import app.data.network.openai.OpenAiException
import app.service.LlmResult
import app.service.openai.OpenAiCompatibleClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class OpenRouterScenariosTest {

    private lateinit var fakeOpenAiApiClient: FakeOpenAiApiClient
    private lateinit var openAiCompatibleClient: OpenAiCompatibleClient

    private val testRequest = OpenAiChatRequest(
        model = "openai/gpt-4o-mini",
        messages = listOf(
            OpenAiChatMessage(role = "user", content = "Привет, как дела?")
        )
    )

    @BeforeEach
    fun setup() {
        fakeOpenAiApiClient = FakeOpenAiApiClient()
        openAiCompatibleClient = OpenAiCompatibleClient(fakeOpenAiApiClient)
    }

    @Test
    fun `when OpenRouter returns 200 OK, response text tokens and USD cost are correctly parsed`() = runTest {
        fakeOpenAiApiClient.chatCompletionResult = Result.success(
            OpenAiChatResponse(
                choices = listOf(
                    OpenAiChoice(
                        message = OpenAiChatMessage(role = "assistant", content = "Привет! У меня всё отлично.")
                    )
                ),
                usage = OpenAiUsage(
                    promptTokens = 15,
                    completionTokens = 25,
                    totalTokens = 40,
                    cost = 0.00015
                )
            )
        )

        val result = openAiCompatibleClient.generateChatCompletion(
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "sk-or-v1-test",
            request = testRequest
        )

        assertTrue(result is LlmResult.Success, "Expected LlmResult.Success, got $result")
        val success = result as LlmResult.Success
        assertEquals("Привет! У меня всё отлично.", success.text)
        assertEquals(40L, success.tokens)
        assertNotNull(success.tokenStats)
        assertEquals(BigDecimal.valueOf(0.00015), success.tokenStats?.openRouterCostUsd)
        assertEquals(15L, success.tokenStats?.promptTokens)
        assertEquals(25L, success.tokenStats?.outputTokens)
    }

    @Test
    fun `when OpenRouter returns 401 Unauthorized, client returns INVALID_API_KEY block`() = runTest {
        fakeOpenAiApiClient.chatCompletionResult = Result.failure(
            OpenAiException.InvalidApiKey("Invalid API key provided", statusCode = 401)
        )

        val result = openAiCompatibleClient.generateChatCompletion(
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "bad-key",
            request = testRequest
        )

        assertTrue(result is LlmResult.Blocked)
        assertEquals("INVALID_API_KEY", (result as LlmResult.Blocked).reason)
    }

    @Test
    fun `when OpenRouter returns 402 Payment Required, client returns INSUFFICIENT_CREDITS block`() = runTest {
        fakeOpenAiApiClient.chatCompletionResult = Result.failure(
            OpenAiException.InsufficientCredits("Credit balance is zero", statusCode = 402)
        )

        val result = openAiCompatibleClient.generateChatCompletion(
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "sk-or-v1-test",
            request = testRequest
        )

        assertTrue(result is LlmResult.Blocked)
        assertEquals("INSUFFICIENT_CREDITS", (result as LlmResult.Blocked).reason)
    }

    @Test
    fun `when OpenRouter returns 404 Not Found, client returns MODEL_NOT_FOUND block`() = runTest {
        fakeOpenAiApiClient.chatCompletionResult = Result.failure(
            OpenAiException.ModelNotFound("Model openai/gpt-5-not-found does not exist", statusCode = 404)
        )

        val result = openAiCompatibleClient.generateChatCompletion(
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "sk-or-v1-test",
            request = testRequest
        )

        assertTrue(result is LlmResult.Blocked)
        assertEquals("MODEL_NOT_FOUND", (result as LlmResult.Blocked).reason)
    }

    @Test
    fun `when OpenRouter returns 429 Too Many Requests, client returns RATE_LIMIT_EXCEEDED block`() = runTest {
        fakeOpenAiApiClient.chatCompletionResult = Result.failure(
            OpenAiException.RateLimitExceeded("Rate limit exceeded for model", statusCode = 429)
        )

        val result = openAiCompatibleClient.generateChatCompletion(
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "sk-or-v1-test",
            request = testRequest
        )

        assertTrue(result is LlmResult.Blocked)
        assertEquals("RATE_LIMIT_EXCEEDED", (result as LlmResult.Blocked).reason)
    }

    @Test
    fun `when OpenRouter returns 400 Context Length Exceeded, client returns CONTEXT_LENGTH_EXCEEDED block`() = runTest {
        fakeOpenAiApiClient.chatCompletionResult = Result.failure(
            OpenAiException.ContextLengthExceeded("Prompt and history exceeded context length", statusCode = 400)
        )

        val result = openAiCompatibleClient.generateChatCompletion(
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "sk-or-v1-test",
            request = testRequest
        )

        assertTrue(result is LlmResult.Blocked)
        assertEquals("CONTEXT_LENGTH_EXCEEDED", (result as LlmResult.Blocked).reason)
    }

    @Test
    fun `when OpenRouter returns 503 Provider Unavailable, client returns PROVIDER_UNAVAILABLE block`() = runTest {
        fakeOpenAiApiClient.chatCompletionResult = Result.failure(
            OpenAiException.ProviderUnavailable("Upstream provider is down or overloaded", statusCode = 503)
        )

        val result = openAiCompatibleClient.generateChatCompletion(
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "sk-or-v1-test",
            request = testRequest
        )

        assertTrue(result is LlmResult.Blocked)
        assertEquals("PROVIDER_UNAVAILABLE", (result as LlmResult.Blocked).reason)
    }

    @Test
    fun `when streaming completion emits reaction tags, filter extracts reaction properly`() = runTest {
        fakeOpenAiApiClient.streamChunks = listOf(
            OpenAiStreamChunk(choices = listOf(OpenAiStreamChoice(delta = OpenAiStreamDelta(content = "Привет! ")))),
            OpenAiStreamChunk(choices = listOf(OpenAiStreamChoice(delta = OpenAiStreamDelta(content = "<reaction>smile</reaction>")))),
            OpenAiStreamChunk(choices = listOf(OpenAiStreamChoice(delta = OpenAiStreamDelta(content = "Рад тебя видеть."))))
        )

        val chunks = openAiCompatibleClient.generateChatCompletionStream(
            baseUrl = "https://openrouter.ai/api/v1",
            apiKey = "sk-or-v1-test",
            request = testRequest
        ).toList()

        val fullText = chunks.joinToString("") { it.textDelta }
        val reaction = chunks.firstNotNullOfOrNull { it.reaction }

        assertEquals("Привет! Рад тебя видеть.", fullText)
        assertEquals("smile", reaction)
    }
}

/**
 * Тестовый Fake-клиент для OpenAiApiClient.
 */
class FakeOpenAiApiClient : OpenAiApiClient {

    var chatCompletionResult: Result<OpenAiChatResponse>? = null
    var streamChunks: List<OpenAiStreamChunk> = emptyList()

    override suspend fun createChatCompletion(
        baseUrl: String,
        apiKey: String,
        request: OpenAiChatRequest,
        headers: Map<String, String>
    ): Result<OpenAiChatResponse> {
        return chatCompletionResult ?: error("chatCompletionResult was not configured for test!")
    }

    override fun createChatCompletionStream(
        baseUrl: String,
        apiKey: String,
        request: OpenAiChatRequest,
        headers: Map<String, String>
    ): Flow<OpenAiStreamChunk> {
        return flowOf(*streamChunks.toTypedArray())
    }

    override suspend fun listModels(baseUrl: String, apiKey: String): Result<OpenAiModelsListResponse> {
        return Result.success(OpenAiModelsListResponse(data = emptyList()))
    }

    override suspend fun createImageGeneration(
        baseUrl: String,
        apiKey: String,
        request: OpenAiImageRequest,
        headers: Map<String, String>
    ): Result<OpenAiImageResponse> {
        return Result.failure(NotImplementedError())
    }

    override fun createSpeechStream(
        baseUrl: String,
        apiKey: String,
        request: OpenAiTtsRequest,
        headers: Map<String, String>
    ): Flow<ByteArray> {
        return emptyFlow()
    }
}
