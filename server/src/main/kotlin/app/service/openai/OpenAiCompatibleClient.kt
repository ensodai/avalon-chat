package app.service.openai

import app.data.network.models.openai.OpenAiChatMessage
import app.data.network.models.openai.OpenAiChatRequest
import app.data.network.openai.OpenAiApiClient
import app.data.network.openai.OpenAiException
import app.service.LlmResult
import app.service.LlmStreamChunk
import app.service.StreamingReactionFilter
import app.utils.logger.LlmLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.koin.core.annotation.Single

@Single
class OpenAiCompatibleClient(
    private val openAiApiClient: OpenAiApiClient
) {

    /**
     * Обычный синхронный запрос к OpenAI/OpenRouter API через OpenAiApiClient.
     */
    suspend fun generateChatCompletion(
        baseUrl: String,
        apiKey: String,
        request: OpenAiChatRequest,
        reqId: String = "openai-req"
    ): LlmResult {
        LlmLogger.processing(reqId, "${request.model} via $baseUrl")

        val result = openAiApiClient.createChatCompletion(
            baseUrl = baseUrl,
            apiKey = apiKey,
            request = request
        )

        return result.fold(
            onSuccess = { response ->
                val choice = response.choices.firstOrNull()
                val text = choice?.message?.content.orEmpty()

                val usage = response.usage
                val totalTokens = usage?.totalTokens ?: 0L
                val promptTokens = usage?.promptTokens ?: 0L
                val outputTokens = usage?.completionTokens ?: 0L
                val cachedTokens = usage?.promptTokensDetails?.cachedTokens ?: 0L
                val costUsd = usage?.cost?.toBigDecimal()

                val (cleanText, reaction) = app.service.chat.ReactionParser.parseFullText(text)

                val tokenStats = LlmResult.TokenStats(
                    totalTokens = totalTokens,
                    promptTokens = promptTokens,
                    outputTokens = outputTokens,
                    cachedTokens = cachedTokens,
                    thoughtsTokens = 0L,
                    openRouterCostUsd = costUsd
                )

                LlmLogger.response(
                    reqId,
                    "OpenAI Success. Length: ${cleanText.length}. Total: $totalTokens (prompt: $promptTokens, output: $outputTokens). Reaction: $reaction"
                )

                LlmResult.Success(
                    text = cleanText,
                    tokens = totalTokens,
                    reaction = reaction,
                    tokenStats = tokenStats
                )
            },
            onFailure = { error ->
                LlmLogger.error(reqId, "OpenAI API Failure: [${error::class.simpleName}] ${error.message}")
                when (error) {
                    is OpenAiException.InvalidApiKey -> LlmResult.Blocked("INVALID_API_KEY")
                    is OpenAiException.InsufficientCredits -> LlmResult.Blocked("INSUFFICIENT_CREDITS")
                    is OpenAiException.RateLimitExceeded -> LlmResult.Blocked("RATE_LIMIT_EXCEEDED")
                    is OpenAiException.ContentModerated -> LlmResult.Blocked("CONTENT_MODERATED")
                    is OpenAiException.ModelNotFound -> LlmResult.Blocked("MODEL_NOT_FOUND")
                    is OpenAiException.ContextLengthExceeded -> LlmResult.Blocked("CONTEXT_LENGTH_EXCEEDED")
                    is OpenAiException.ProviderUnavailable -> LlmResult.Blocked("PROVIDER_UNAVAILABLE")
                    is OpenAiException -> LlmResult.Error(error)
                    else -> LlmResult.Error(error as? Exception ?: Exception(error))
                }
            }
        )
    }

    /**
     * Потоковая генерация через SSE с фильтрацией тегов <reaction>.
     */
    fun generateChatCompletionStream(
        baseUrl: String,
        apiKey: String,
        request: OpenAiChatRequest,
        reqId: String = "openai-stream"
    ): Flow<LlmStreamChunk> = flow {
        LlmLogger.processing(reqId, "${request.model} (stream) via $baseUrl")

        val reactionFilter = StreamingReactionFilter()
        var totalTokens = 0L
        var promptTokens = 0L
        var outputTokens = 0L
        var cachedTokens = 0L
        var costUsd: java.math.BigDecimal? = null

        openAiApiClient.createChatCompletionStream(
            baseUrl = baseUrl,
            apiKey = apiKey,
            request = request
        ).collect { chunk ->
            chunk.usage?.let { u ->
                totalTokens = u.totalTokens
                promptTokens = u.promptTokens
                outputTokens = u.completionTokens
                cachedTokens = u.promptTokensDetails?.cachedTokens ?: 0L
                if (u.cost != null) costUsd = u.cost.toBigDecimal()
            }

            val deltaText = chunk.choices.firstOrNull()?.delta?.content.orEmpty()
            if (deltaText.isNotEmpty()) {
                val (cleanDelta, reaction) = reactionFilter.process(deltaText)
                if (cleanDelta.isNotEmpty() || reaction != null) {
                    emit(
                        LlmStreamChunk(
                            textDelta = cleanDelta,
                            isFinished = false,
                            reaction = reaction,
                            tokenStats = null
                        )
                    )
                }
            }
        }

        // Завершаем и сбрасываем остаток буфера реакций
        val finalFlush = reactionFilter.flush()
        val pendingReaction = reactionFilter.getPendingReaction()
        val tokenStats = LlmResult.TokenStats(
            totalTokens = totalTokens,
            promptTokens = promptTokens,
            outputTokens = outputTokens,
            cachedTokens = cachedTokens,
            thoughtsTokens = 0L,
            openRouterCostUsd = costUsd
        )

        emit(
            LlmStreamChunk(
                textDelta = finalFlush,
                isFinished = true,
                reaction = pendingReaction,
                tokenStats = tokenStats
            )
        )

        LlmLogger.response(reqId, "OpenAI Stream Complete. Total: $totalTokens")
    }.flowOn(Dispatchers.IO)

    /**
     * Быстрый тест подключения (проверка валидности URL, API-ключа и модели).
     */
    suspend fun testConnection(
        baseUrl: String,
        apiKey: String,
        model: String
    ): Result<String> {
        val request = OpenAiChatRequest(
            model = model,
            messages = listOf(
                OpenAiChatMessage(
                    role = "user",
                    content = "ping"
                )
            ),
            maxTokens = 100,
            stream = false
        )

        val result = openAiApiClient.createChatCompletion(
            baseUrl = baseUrl,
            apiKey = apiKey,
            request = request
        )

        return result.map { response ->
            val choice = response.choices.firstOrNull()
            choice?.message?.content?.takeIf { it.isNotBlank() }
                ?: choice?.message?.reasoning?.takeIf { it.isNotBlank() }
                ?: "OK"
        }
    }

    /**
     * Генерация картинки (через OpenAI DALL-E /v1/images/generations формат).
     */
    suspend fun generateImage(
        baseUrl: String,
        apiKey: String,
        request: app.data.network.models.openai.OpenAiImageRequest,
        reqId: String = "openai-image"
    ): Result<app.data.network.models.openai.OpenAiImageResponse> {
        LlmLogger.processing(reqId, "${request.model} image gen via $baseUrl")
        return openAiApiClient.createImageGeneration(baseUrl, apiKey, request)
    }

    /**
     * Генерация речи (TTS) через /v1/audio/speech.
     */
    fun generateSpeechStream(
        baseUrl: String,
        apiKey: String,
        request: app.data.network.models.openai.OpenAiTtsRequest,
        reqId: String = "openai-tts"
    ): Flow<ByteArray> {
        LlmLogger.processing(reqId, "${request.model} TTS via $baseUrl")
        return openAiApiClient.createSpeechStream(baseUrl, apiKey, request)
    }

    /**
     * Тест TTS подключения.
     */
    suspend fun testTtsConnection(
        baseUrl: String,
        apiKey: String,
        model: String
    ): Result<String> {
        return runCatching {
            var receivedBytes = 0
            val req = app.data.network.models.openai.OpenAiTtsRequest(
                model = model,
                input = "ping",
                voice = "alloy"
            )
            generateSpeechStream(baseUrl, apiKey, req, "tts-test").collect { chunk ->
                receivedBytes += chunk.size
            }
            if (receivedBytes > 0) "OK (Audio received: $receivedBytes bytes)" else "OK"
        }
    }

    /**
     * Тест Image подключения.
     */
    suspend fun testImageConnection(
        baseUrl: String,
        apiKey: String,
        model: String
    ): Result<String> {
        val req = app.data.network.models.openai.OpenAiImageRequest(
            model = model,
            prompt = "a cute robot holding a flower",
            n = 1,
            size = "512x512"
        )
        return generateImage(baseUrl, apiKey, req, "image-test").map { resp ->
            val url = resp.data.firstOrNull()?.url ?: resp.data.firstOrNull()?.b64Json
            if (!url.isNullOrBlank()) "OK (Image generated)" else "OK"
        }
    }
}
