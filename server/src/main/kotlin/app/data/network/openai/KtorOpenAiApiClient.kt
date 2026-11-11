package app.data.network.openai

import app.data.network.models.openai.OpenAiChatRequest
import app.data.network.models.openai.OpenAiChatResponse
import app.data.network.models.openai.OpenAiImage
import app.data.network.models.openai.OpenAiImageRequest
import app.data.network.models.openai.OpenAiImageResponse
import app.data.network.models.openai.OpenAiModelsListResponse
import app.data.network.models.openai.OpenAiStreamChunk
import app.data.network.models.openai.OpenAiTtsRequest
import app.utils.logger.LlmLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.koin.core.annotation.Single
import java.util.Base64

@Single(binds = [OpenAiApiClient::class])
class KtorOpenAiApiClient : OpenAiApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        explicitNulls = false
        prettyPrint = false
    }

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 120_000
            connectTimeoutMillis = 20_000
            socketTimeoutMillis = 120_000
        }
    }

    override suspend fun createChatCompletion(
        baseUrl: String,
        apiKey: String,
        request: OpenAiChatRequest,
        headers: Map<String, String>
    ): Result<OpenAiChatResponse> {
        val endpoint = resolveEndpoint(baseUrl, "chat/completions")

        return try {
            val response = httpClient.post(endpoint) {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                header("HTTP-Referer", "https://avalon.chat")
                header("X-Title", "Avalon Avatars")
                headers.forEach { (key, value) -> header(key, value) }
                setBody(json.encodeToString(request))
            }

            val responseBody = response.bodyAsText()

            if (response.status.isSuccess()) {
                val parsed = json.decodeFromString<OpenAiChatResponse>(responseBody)
                Result.success(parsed)
            } else {
                val retryAfter = response.headers["Retry-After"]?.toLongOrNull()
                val error = OpenAiErrorParser.parse(response.status, responseBody, retryAfter)
                Result.failure(error)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: OpenAiException) {
            Result.failure(e)
        } catch (e: Exception) {
            LlmLogger.error("KtorOpenAiApiClient", "createChatCompletion network error: ${e.message}")
            Result.failure(OpenAiException.NetworkError(e.message ?: "Network error", e))
        }
    }

    override fun createChatCompletionStream(
        baseUrl: String,
        apiKey: String,
        request: OpenAiChatRequest,
        headers: Map<String, String>
    ): Flow<OpenAiStreamChunk> = flow {
        val endpoint = resolveEndpoint(baseUrl, "chat/completions")
        val streamingRequest = request.copy(stream = true)

        try {
            httpClient.preparePost(endpoint) {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                header("HTTP-Referer", "https://avalon.chat")
                header("X-Title", "Avalon Avatars")
                headers.forEach { (key, value) -> header(key, value) }
                setBody(json.encodeToString(streamingRequest))
            }.execute { response ->
                if (!response.status.isSuccess()) {
                    val errorBody = response.bodyAsText()
                    val retryAfter = response.headers["Retry-After"]?.toLongOrNull()
                    val error = OpenAiErrorParser.parse(response.status, errorBody, retryAfter)
                    throw error
                }

                val channel = response.bodyAsChannel()
                while (currentCoroutineContext().isActive && !channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break
                    val trimmed = line.trim()

                    if (trimmed.isEmpty() || trimmed.startsWith(":")) {
                        continue
                    }

                    if (trimmed == "data: [DONE]") {
                        break
                    }

                    if (trimmed.startsWith("data: ")) {
                        val jsonStr = trimmed.substring(6).trim()
                        if (jsonStr.isEmpty() || jsonStr == "[DONE]") {
                            break
                        }

                        try {
                            val chunk = json.decodeFromString<OpenAiStreamChunk>(jsonStr)
                            emit(chunk)
                        } catch (e: Exception) {
                            LlmLogger.inf("KtorOpenAiApiClient", "Failed to parse SSE chunk: $jsonStr, error: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: OpenAiException) {
            LlmLogger.error("KtorOpenAiApiClient", "createChatCompletionStream API error: ${e.message}")
            throw e
        } catch (e: Exception) {
            LlmLogger.error("KtorOpenAiApiClient", "createChatCompletionStream failure: ${e.message}")
            throw OpenAiException.NetworkError(e.message ?: "Stream network error", e)
        }
    }

    override suspend fun listModels(
        baseUrl: String,
        apiKey: String
    ): Result<OpenAiModelsListResponse> {
        val endpoint = resolveEndpoint(baseUrl, "models")

        return try {
            val response = httpClient.get(endpoint) {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                header("HTTP-Referer", "https://avalon.chat")
                header("X-Title", "Avalon Avatars")
            }

            val body = response.bodyAsText()
            if (response.status.isSuccess()) {
                val parsed = json.decodeFromString<OpenAiModelsListResponse>(body)
                Result.success(parsed)
            } else {
                val retryAfter = response.headers["Retry-After"]?.toLongOrNull()
                val error = OpenAiErrorParser.parse(response.status, body, retryAfter)
                Result.failure(error)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: OpenAiException) {
            Result.failure(e)
        } catch (e: Exception) {
            LlmLogger.error("KtorOpenAiApiClient", "listModels error: ${e.message}")
            Result.failure(OpenAiException.NetworkError(e.message ?: "Network error", e))
        }
    }

    override suspend fun createImageGeneration(
        baseUrl: String,
        apiKey: String,
        request: OpenAiImageRequest,
        headers: Map<String, String>
    ): Result<OpenAiImageResponse> {
        val endpoint = resolveEndpoint(baseUrl, "images/generations")

        return try {
            val response = httpClient.post(endpoint) {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                header("HTTP-Referer", "https://avalon.chat")
                header("X-Title", "Avalon Avatars")
                headers.forEach { (key, value) -> header(key, value) }
                setBody(json.encodeToString(request))
            }

            val responseBody = response.bodyAsText()

            if (response.status.isSuccess()) {
                val parsed = json.decodeFromString<OpenAiImageResponse>(responseBody)
                Result.success(parsed)
            } else {
                // Если эндпоинт images/generations вернул 400/404 (для чат-моделей генерации изображений OpenRouter),
                // пробуем fallback через chat/completions
                if (response.status.value in listOf(400, 404)) {
                    val chatFallbackEndpoint = resolveEndpoint(baseUrl, "chat/completions")
                    val chatRequest = buildJsonObject {
                        put("model", request.model ?: "")
                        putJsonArray("messages") {
                            addJsonObject {
                                put("role", "user")
                                put("content", request.prompt)
                            }
                        }
                    }
                    val chatResponse = httpClient.post(chatFallbackEndpoint) {
                        contentType(ContentType.Application.Json)
                        header(HttpHeaders.Authorization, "Bearer $apiKey")
                        header("HTTP-Referer", "https://avalon.chat")
                        header("X-Title", "Avalon Avatars")
                        headers.forEach { (key, value) -> header(key, value) }
                        setBody(chatRequest.toString())
                    }
                    if (chatResponse.status.isSuccess()) {
                        val chatBody = chatResponse.bodyAsText()
                        val root = json.parseToJsonElement(chatBody).jsonObject
                        val firstChoice = root["choices"]?.jsonArray?.firstOrNull()?.jsonObject
                        val message = firstChoice?.get("message")?.jsonObject
                        val images = message?.get("images")?.jsonArray
                        if (!images.isNullOrEmpty()) {
                            val firstImg = images.first()
                            val b64 = if (firstImg is JsonObject) {
                                val url = firstImg["image_url"]?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull
                                    ?: firstImg["url"]?.jsonPrimitive?.contentOrNull
                                if (url != null && url.contains(",")) url.substringAfter(",") else url
                            } else {
                                val url = firstImg.jsonPrimitive.content
                                if (url.contains(",")) url.substringAfter(",") else url
                            }
                            if (b64 != null) {
                                return Result.success(
                                    OpenAiImageResponse(
                                        data = listOf(OpenAiImage(b64Json = b64))
                                    )
                                )
                            }
                        }
                    }
                }

                val retryAfter = response.headers["Retry-After"]?.toLongOrNull()
                val error = OpenAiErrorParser.parse(response.status, responseBody, retryAfter)
                Result.failure(error)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: OpenAiException) {
            Result.failure(e)
        } catch (e: Exception) {
            LlmLogger.error("KtorOpenAiApiClient", "createImageGeneration network error: ${e.message}")
            Result.failure(OpenAiException.NetworkError(e.message ?: "Network error", e))
        }
    }

    override fun createSpeechStream(
        baseUrl: String,
        apiKey: String,
        request: OpenAiTtsRequest,
        headers: Map<String, String>
    ): Flow<ByteArray> = flow {
        val endpoint = resolveEndpoint(baseUrl, "audio/speech")

        try {
            var handled = false
            httpClient.preparePost(endpoint) {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                header("HTTP-Referer", "https://avalon.chat")
                header("X-Title", "Avalon Avatars")
                headers.forEach { (key, value) -> header(key, value) }
                setBody(json.encodeToString(request))
            }.execute { response ->
                if (response.status.isSuccess()) {
                    handled = true
                    val channel = response.bodyAsChannel()
                    val buffer = ByteArray(4096)
                    while (currentCoroutineContext().isActive && !channel.isClosedForRead) {
                        val bytesRead = channel.readAvailable(buffer)
                        if (bytesRead > 0) {
                            emit(buffer.copyOf(bytesRead))
                        } else if (bytesRead == -1) {
                            break
                        }
                    }
                }
            }

            if (!handled) {
                // Fallback через chat/completions streaming audio (для OpenRouter моделей вроде openai/gpt-audio-mini)
                val chatEndpoint = resolveEndpoint(baseUrl, "chat/completions")
                val chatPayload = buildJsonObject {
                    put("model", request.model)
                    put("stream", true)
                    putJsonArray("modalities") {
                        add("text")
                        add("audio")
                    }
                    putJsonObject("audio") {
                        put("voice", request.voice)
                        put("format", "pcm16")
                    }
                    putJsonArray("messages") {
                        addJsonObject {
                            put("role", "system")
                            put("content", "You are a professional text-to-speech narrator. Your ONLY task is to read the provided user text aloud verbatim, word for word. Do not respond, do not reply, do not add any greetings, apologies, or comments. Output ONLY the exact spoken narration of the text.")
                        }
                        addJsonObject {
                            put("role", "user")
                            put("content", request.input)
                        }
                    }
                }

                httpClient.preparePost(chatEndpoint) {
                    contentType(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $apiKey")
                    header("HTTP-Referer", "https://avalon.chat")
                    header("X-Title", "Avalon Avatars")
                    headers.forEach { (key, value) -> header(key, value) }
                    setBody(chatPayload.toString())
                }.execute { response ->
                    if (!response.status.isSuccess()) {
                        val errorBody = response.bodyAsText()
                        val retryAfter = response.headers["Retry-After"]?.toLongOrNull()
                        val error = OpenAiErrorParser.parse(response.status, errorBody, retryAfter)
                        throw error
                    }

                    val channel = response.bodyAsChannel()
                    while (currentCoroutineContext().isActive && !channel.isClosedForRead) {
                        val line = channel.readUTF8Line() ?: break
                        if (line.startsWith("data: ") && line != "data: [DONE]") {
                            try {
                                val jsonStr = line.removePrefix("data: ").trim()
                                val parsed = json.parseToJsonElement(jsonStr).jsonObject
                                val choices = parsed["choices"]?.jsonArray
                                val delta = choices?.firstOrNull()?.jsonObject?.get("delta")?.jsonObject
                                val audioObj = delta?.get("audio")?.jsonObject
                                val b64Data = audioObj?.get("data")?.jsonPrimitive?.contentOrNull
                                if (!b64Data.isNullOrBlank()) {
                                    val bytes = Base64.getDecoder().decode(b64Data)
                                    emit(bytes)
                                }
                            } catch (_: Exception) {}
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: OpenAiException) {
            LlmLogger.error("KtorOpenAiApiClient", "createSpeechStream API error: ${e.message}")
            throw e
        } catch (e: Exception) {
            LlmLogger.error("KtorOpenAiApiClient", "createSpeechStream failure: ${e.message}")
            throw OpenAiException.NetworkError(e.message ?: "Stream network error", e)
        }
    }

    private fun resolveEndpoint(baseUrl: String, path: String): String {
        val cleanBase = baseUrl.trim().trimEnd('/')
        val cleanPath = path.trim().trimStart('/')
        return "$cleanBase/$cleanPath"
    }
}
