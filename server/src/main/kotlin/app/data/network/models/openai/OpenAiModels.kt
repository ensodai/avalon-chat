package app.data.network.models.openai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class OpenAiChatMessage(
    val role: String,
    val content: String? = null,
    val name: String? = null,
    val reasoning: String? = null,
    val refusal: String? = null
)

@Serializable
data class OpenAiStreamOptions(
    @SerialName("include_usage")
    val includeUsage: Boolean = true
)

@Serializable
data class OpenAiResponseFormat(
    val type: String
)

@Serializable
data class OpenAiChatRequest(
    val model: String,
    val messages: List<OpenAiChatMessage>,
    val temperature: Float? = null,
    @SerialName("top_p")
    val topP: Float? = null,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    @SerialName("frequency_penalty")
    val frequencyPenalty: Float? = null,
    @SerialName("presence_penalty")
    val presencePenalty: Float? = null,
    val stream: Boolean = false,
    @SerialName("stream_options")
    val streamOptions: OpenAiStreamOptions? = null,
    @SerialName("response_format")
    val responseFormat: OpenAiResponseFormat? = null
)

@Serializable
data class OpenAiChatResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<OpenAiChoice> = emptyList(),
    val usage: OpenAiUsage? = null
)

@Serializable
data class OpenAiChoice(
    val index: Int = 0,
    val message: OpenAiChatMessage? = null,
    @SerialName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class OpenAiUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Long = 0L,
    @SerialName("completion_tokens")
    val completionTokens: Long = 0L,
    @SerialName("total_tokens")
    val totalTokens: Long = 0L,
    @SerialName("prompt_tokens_details")
    val promptTokensDetails: OpenAiPromptTokensDetails? = null,
    val cost: Double? = null
)

@Serializable
data class OpenAiPromptTokensDetails(
    @SerialName("cached_tokens")
    val cachedTokens: Long = 0L
)

@Serializable
data class OpenAiStreamChunk(
    val id: String? = null,
    val model: String? = null,
    val choices: List<OpenAiStreamChoice> = emptyList(),
    val usage: OpenAiUsage? = null,
    val error: OpenAiErrorDetails? = null
)

@Serializable
data class OpenAiStreamChoice(
    val index: Int = 0,
    val delta: OpenAiStreamDelta? = null,
    @SerialName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class OpenAiStreamDelta(
    val role: String? = null,
    val content: String? = null,
    val reasoning: String? = null,
    val refusal: String? = null
)

@Serializable
data class OpenAiModelItem(
    val id: String,
    @SerialName("object")
    val objectType: String? = null,
    val created: Long? = null,
    @SerialName("owned_by")
    val ownedBy: String? = null
)

@Serializable
data class OpenAiModelsListResponse(
    @SerialName("object")
    val objectType: String? = null,
    val data: List<OpenAiModelItem> = emptyList()
)

@Serializable
data class OpenAiErrorResponse(
    val error: OpenAiErrorDetails? = null
)

@Serializable
data class OpenAiErrorDetails(
    val message: String? = null,
    val type: String? = null,
    val param: String? = null,
    val code: JsonElement? = null,
    val metadata: JsonElement? = null
) {
    val codeString: String?
        get() = when (val c = code) {
            is JsonPrimitive -> c.content
            else -> c?.toString()
        }
}

@Serializable
data class OpenAiImageRequest(
    val prompt: String,
    val model: String? = null,
    val n: Int? = 1,
    val quality: String? = null,
    @SerialName("response_format")
    val responseFormat: String? = "b64_json",
    val size: String? = null
)

@Serializable
data class OpenAiImageResponse(
    val created: Long? = null,
    val data: List<OpenAiImage> = emptyList()
)

@Serializable
data class OpenAiImage(
    val url: String? = null,
    @SerialName("b64_json")
    val b64Json: String? = null,
    @SerialName("revised_prompt")
    val revisedPrompt: String? = null
)

@Serializable
data class OpenAiTtsRequest(
    val model: String,
    val input: String,
    val voice: String,
    @SerialName("response_format")
    val responseFormat: String = "pcm"
)
