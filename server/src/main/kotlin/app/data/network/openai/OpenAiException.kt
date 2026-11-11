package app.data.network.openai

import app.data.network.models.openai.OpenAiErrorDetails
import app.data.network.models.openai.OpenAiErrorResponse
import com.ensodai.shared.avalon.chat.models.SystemEventType
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

sealed class OpenAiException(
    override val message: String,
    val statusCode: Int? = null,
    val errorCode: String? = null,
    val errorType: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {

    class InvalidApiKey(
        message: String,
        statusCode: Int = 401,
        errorCode: String? = null
    ) : OpenAiException(message, statusCode, errorCode)

    class InsufficientCredits(
        message: String,
        statusCode: Int = 402,
        errorCode: String? = null
    ) : OpenAiException(message, statusCode, errorCode)

    class RateLimitExceeded(
        message: String,
        statusCode: Int = 429,
        val retryAfterSeconds: Long? = null,
        errorCode: String? = null
    ) : OpenAiException(message, statusCode, errorCode)

    class ContentModerated(
        message: String,
        statusCode: Int = 403,
        errorCode: String? = null
    ) : OpenAiException(message, statusCode, errorCode)

    class ModelNotFound(
        message: String,
        statusCode: Int = 404,
        errorCode: String? = null
    ) : OpenAiException(message, statusCode, errorCode)

    class ContextLengthExceeded(
        message: String,
        statusCode: Int = 400,
        errorCode: String? = null
    ) : OpenAiException(message, statusCode, errorCode)

    class GuardrailsRestricted(
        message: String,
        statusCode: Int = 400,
        errorCode: String? = null
    ) : OpenAiException(message, statusCode, errorCode)

    class ProviderUnavailable(
        message: String,
        statusCode: Int = 503,
        errorCode: String? = null
    ) : OpenAiException(message, statusCode, errorCode)

    class BadRequest(
        message: String,
        statusCode: Int = 400,
        errorCode: String? = null,
        errorType: String? = null
    ) : OpenAiException(message, statusCode, errorCode, errorType)

    class ServerError(
        message: String,
        statusCode: Int = 500,
        errorCode: String? = null
    ) : OpenAiException(message, statusCode, errorCode)

    class NetworkError(
        message: String,
        cause: Throwable
    ) : OpenAiException(message, cause = cause)

    class StreamError(
        message: String,
        errorCode: String? = null,
        cause: Throwable? = null
    ) : OpenAiException(message, errorCode = errorCode, cause = cause)
}

object OpenAiErrorParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun parse(statusCode: HttpStatusCode, body: String, retryAfter: Long? = null): OpenAiException {
        val details = parseErrorDetails(body)
        val msg = details?.message?.takeIf { it.isNotBlank() } ?: "HTTP ${statusCode.value}: $body"
        val code = details?.codeString
        val type = details?.type

        if (code == "context_length_exceeded" || msg.contains("context length", ignoreCase = true) || msg.contains("maximum context", ignoreCase = true)) {
            return OpenAiException.ContextLengthExceeded(msg, statusCode.value, code)
        }

        if (code == "guardrail_restrictions" ||
            msg.contains("guardrail", ignoreCase = true) ||
            msg.contains("data policy", ignoreCase = true) ||
            msg.contains("privacy", ignoreCase = true) && msg.contains("endpoint", ignoreCase = true)
        ) {
            return OpenAiException.GuardrailsRestricted(msg, statusCode.value, code)
        }

        if (code == "provider_routing_failed" || statusCode.value in listOf(502, 503, 504)) {
            return OpenAiException.ProviderUnavailable(msg, statusCode.value, code)
        }

        return when (statusCode.value) {
            401 -> OpenAiException.InvalidApiKey(msg, statusCode.value, code)
            402 -> OpenAiException.InsufficientCredits(msg, statusCode.value, code)
            403 -> OpenAiException.ContentModerated(msg, statusCode.value, code)
            404 -> OpenAiException.ModelNotFound(msg, statusCode.value, code)
            429 -> OpenAiException.RateLimitExceeded(msg, statusCode.value, retryAfter, code)
            400 -> OpenAiException.BadRequest(msg, statusCode.value, code, type)
            in 500..599 -> OpenAiException.ServerError(msg, statusCode.value, code)
            else -> OpenAiException.ServerError(msg, statusCode.value, code)
        }
    }

    fun parseErrorDetails(body: String): OpenAiErrorDetails? {
        return try {
            json.decodeFromString<OpenAiErrorResponse>(body).error
        } catch (_: Exception) {
            null
        }
    }
}

fun OpenAiException.toSystemEventType(): SystemEventType = when (this) {
    is OpenAiException.InvalidApiKey -> SystemEventType.CUSTOM_KEY_INVALID
    is OpenAiException.InsufficientCredits -> SystemEventType.CUSTOM_INSUFFICIENT_CREDITS
    is OpenAiException.RateLimitExceeded -> SystemEventType.CUSTOM_RATE_LIMIT
    is OpenAiException.ModelNotFound -> SystemEventType.CUSTOM_MODEL_NOT_FOUND
    is OpenAiException.ContextLengthExceeded -> SystemEventType.CUSTOM_CONTEXT_LENGTH_EXCEEDED
    is OpenAiException.GuardrailsRestricted -> SystemEventType.CUSTOM_GUARDRAILS_RESTRICTED
    is OpenAiException.ProviderUnavailable -> SystemEventType.CUSTOM_PROVIDER_UNAVAILABLE
    is OpenAiException.ContentModerated -> SystemEventType.MESSAGE_BLOCKED
    is OpenAiException.BadRequest,
    is OpenAiException.ServerError,
    is OpenAiException.NetworkError,
    is OpenAiException.StreamError -> SystemEventType.SERVER_ERROR
}
