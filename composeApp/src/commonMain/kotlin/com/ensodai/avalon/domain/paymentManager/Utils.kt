package com.ensodai.avalon.domain.paymentManager

// Utils.kt
import kotlinx.coroutines.delay
import kotlinx.io.IOException

suspend fun <T> retryIO(
    times: Int = 3,
    initialDelay: Long = 500,
    factor: Double = 2.0,
    block: suspend () -> T
): Result<T> {
    var currentDelay = initialDelay

    repeat(times - 1) {
        try {
            return Result.success(block())
        } catch (e: Throwable) {
            if (e is IOException) {
                // Если это сетевая ошибка - ждем и пробуем снова
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong()
            } else {
                // Если это другая ошибка (например, юзер не найден 404),
                // нет смысла пробовать снова - возвращаем ошибку сразу
                return Result.failure(e)
            }
        }
    }

    // Последняя попытка
    return try {
        Result.success(block())
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
