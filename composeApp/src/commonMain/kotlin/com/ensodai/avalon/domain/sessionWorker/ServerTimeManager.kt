package com.ensodai.avalon.domain.sessionWorker

import com.ensodai.avalon.config.AppBuildConfig
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.koin.core.annotation.Single

import kotlin.concurrent.Volatile
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

@Single
class ServerTimeManager {

    // В Kotlin используем kotlin.time.Duration
    @Volatile
    private var clockOffset: Duration = Duration.ZERO

    @Volatile
    private var isSynchronized = false

    /**
     * Возвращает ТЕКУЩЕЕ скорректированное время в UTC.
     */
    fun getCorrectedNow(): Instant {
        return Clock.System.now() + clockOffset
    }

    fun updateServerTime(serverDateHeader: String?) {
        if (serverDateHeader.isNullOrBlank()) return

        try {
            // 1. Парсим заголовок (RFC 1123) в Instant
            val serverInstant = parseRfc1123ToInstant(serverDateHeader) ?: return

            // 2. Фиксируем момент получения на клиенте
            val clientTime = Clock.System.now()

            // 3. Считаем разницу (в Kotlin Instant минус Instant дает Duration)
            val newOffset = serverInstant - clientTime

            clockOffset = newOffset
            isSynchronized = true

            if (AppBuildConfig.IS_DEBUG) {
                println("TimeSync: Time synced. Offset: ${newOffset.inWholeMilliseconds} ms. Server: $serverDateHeader")
            }

        } catch (e: Exception) {
            if (AppBuildConfig.IS_DEBUG) {
                println("TimeSync: Failed to parse server Date header: $serverDateHeader")
            }
        }
    }

    private val monthMap = mapOf(
        "Jan" to 1, "Feb" to 2, "Mar" to 3, "Apr" to 4, "May" to 5, "Jun" to 6,
        "Jul" to 7, "Aug" to 8, "Sep" to 9, "Oct" to 10, "Nov" to 11, "Dec" to 12
    )

    fun parseRfc1123ToInstant(header: String): Instant? {
        return try {
            // Пример: "Tue, 05 Oct 2021 15:30:00 GMT"
            val parts = header.split(" ")
            if (parts.size < 5) return null

            // Индексы могут плавать, если есть/нет дня недели, поэтому ищем по структуре
            // Обычно формат: [Day,] DD Mon YYYY HH:mm:ss GMT
            val dayIndex = if (parts[0].endsWith(",")) 1 else 0

            val day = parts[dayIndex].toInt()
            val month = monthMap[parts[dayIndex + 1]] ?: return null
            val year = parts[dayIndex + 2].toInt()

            val timeParts = parts[dayIndex + 3].split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            val second = timeParts[2].toInt()

            val ldt = LocalDateTime(year, month, day, hour, minute, second)
            ldt.toInstant(TimeZone.UTC)
        } catch (e: Exception) {
            null
        }
    }
}
