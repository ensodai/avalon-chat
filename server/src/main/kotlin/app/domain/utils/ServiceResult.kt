package app.domain.utils

sealed class ServiceResult<out T> {

    data class Success<out T>(val data: T) : ServiceResult<T>()

    data class Error(
        val type: ErrorType,
        val message: String? = null
    ) : ServiceResult<Nothing>()

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw Exception(message ?: "Service error: $type")
    }
}

enum class ErrorType {
    NOT_FOUND,      // -> 404
    FORBIDDEN,      // -> 403
    BAD_REQUEST,    // -> 400
    CONFLICT,       // -> 409
    SERVER_ERROR    // -> 500
}