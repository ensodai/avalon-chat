package app.data.network.openai

import app.data.network.models.openai.OpenAiChatRequest
import app.data.network.models.openai.OpenAiChatResponse
import app.data.network.models.openai.OpenAiModelsListResponse
import app.data.network.models.openai.OpenAiStreamChunk
import kotlinx.coroutines.flow.Flow

interface OpenAiApiClient {
    /**
     * Executes standard (non-streaming) chat completion request.
     */
    suspend fun createChatCompletion(
        baseUrl: String,
        apiKey: String,
        request: OpenAiChatRequest,
        headers: Map<String, String> = emptyMap()
    ): Result<OpenAiChatResponse>

    /**
     * Executes streaming SSE chat completion request, emitting chunks as they arrive.
     */
    fun createChatCompletionStream(
        baseUrl: String,
        apiKey: String,
        request: OpenAiChatRequest,
        headers: Map<String, String> = emptyMap()
    ): Flow<OpenAiStreamChunk>

    /**
     * Fetches list of available models from provider.
     */
    suspend fun listModels(
        baseUrl: String,
        apiKey: String
    ): Result<OpenAiModelsListResponse>

    /**
     * Executes image generation request.
     */
    suspend fun createImageGeneration(
        baseUrl: String,
        apiKey: String,
        request: app.data.network.models.openai.OpenAiImageRequest,
        headers: Map<String, String> = emptyMap()
    ): Result<app.data.network.models.openai.OpenAiImageResponse>
    /**
     * Executes TTS request, emitting bytes.
     */
    fun createSpeechStream(
        baseUrl: String,
        apiKey: String,
        request: app.data.network.models.openai.OpenAiTtsRequest,
        headers: Map<String, String> = emptyMap()
    ): kotlinx.coroutines.flow.Flow<ByteArray>
}
