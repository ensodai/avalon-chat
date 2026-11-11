package app.service

import com.google.genai.errors.ClientException
import com.google.genai.errors.GenAiIOException
import com.google.genai.errors.ServerException
import kotlin.coroutines.cancellation.CancellationException
import org.koin.core.annotation.Single

sealed class GeminiError {
    data object QuotaExceeded : GeminiError()      // 429 -> В кулдаун
    data object KeyBanned : GeminiError()          // 403 Suspended -> В вечный бан
    data object ServerError : GeminiError()        // 5xx -> Ретрай
    data class ClientError(val msg: String) : GeminiError() // 4xx -> Фатал
    data class BlockedContent(val reason: String?) : GeminiError()
    data object Cancelled : GeminiError()
    data class Unknown(val cause: Throwable) : GeminiError()
}

@Single
class GeminiExceptionHandler {

    // Регулярка ищет первые 3 цифры в сообщении (код ошибки)
    private val statusCodeRegex = Regex("(\\d{3})")

    fun handle(exception: Throwable): GeminiError {
        return when (exception) {
            is CancellationException -> GeminiError.Cancelled
            is GenAiIOException -> GeminiError.ServerError
            is ServerException -> GeminiError.ServerError

            is ClientException -> {
                val msg = exception.message ?: ""

                // Пытаемся найти код ошибки (403, 429 и т.д.)
                val code = statusCodeRegex.find(msg)?.value?.toIntOrNull()

                return when {
                    // 1. Quota Exceeded (429)
                    code == 429 || msg.contains("429") || msg.contains("quota", ignoreCase = true) ->
                        GeminiError.QuotaExceeded

                    // 2. Key Suspended / Permission Denied (403)
                    code == 403 || msg.contains("suspended", ignoreCase = true) ||
                            msg.contains("permission denied", ignoreCase = true) ||
                                msg.contains("API key not valid", ignoreCase = true)->
                        GeminiError.KeyBanned

                    // 3. Остальные ошибки клиента
                    else -> GeminiError.ClientError(msg)
                }
            }

            else -> {
                if (exception.cause is CancellationException) {
                    GeminiError.Cancelled
                } else {
                    GeminiError.Unknown(exception)
                }
            }
        }
    }
}