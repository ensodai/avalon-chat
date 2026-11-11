package com.ensodai.avalon.utils

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.offsetAt
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

// Хелпер для дополнения нулями (09:05 вместо 9:5)
private fun Int.pad(): String = this.toString().padStart(2, '0')

// 2. Основной метод форматирования
fun Instant.convertOffsetToDisplayTime(
    // В KMP лучше передавать признак языка, так как Locale.getDefault() — это JVM-специфичный код
    isRussian: Boolean = true
): String {
    val zoneId = TimeZone.currentSystemDefault()
    val userLocalDateTime = this.toLocalDateTime(zoneId)

    val targetDate = userLocalDateTime.date
    val today = Clock.System.todayIn(zoneId)
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    val weekAgo = today.minus(7, DateTimeUnit.DAY)

    return when {
        // Сегодня -> HH:mm
        targetDate == today -> {
            "${userLocalDateTime.hour.pad()}:${userLocalDateTime.minute.pad()}"
        }

        // Вчера -> "Вчера" / "Yesterday"
        targetDate == yesterday -> {
            if (isRussian) "Вчера" else "Yesterday"
        }

        // В течение недели -> День недели
        targetDate > weekAgo -> {
            getShortDayOfWeek(targetDate.dayOfWeek, isRussian)
        }

        // Больше недели назад -> dd.MM
        else -> {
            "${targetDate.day.pad()}.${targetDate.month.number.pad()}"
        }
    }
}

// 3. Форматирование даты dd.MM.yyyy
fun Instant?.toUiString(): String {
    if (this == null) return "error"
    val date = this.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.day.pad()}.${date.month.number.pad()}.${date.year}"
}

// Вспомогательная функция для дней недели (замена "EEE")
private fun getShortDayOfWeek(day: DayOfWeek, isRussian: Boolean): String {
    return if (isRussian) {
        when (day) {
            DayOfWeek.MONDAY -> "Пн"
            DayOfWeek.TUESDAY -> "Вт"
            DayOfWeek.WEDNESDAY -> "Ср"
            DayOfWeek.THURSDAY -> "Чт"
            DayOfWeek.FRIDAY -> "Пт"
            DayOfWeek.SATURDAY -> "Сб"
            DayOfWeek.SUNDAY -> "Вс"
        }
    } else {
        // Берем первые 3 буквы английского названия (MON, TUE...)
        day.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
    }
}

//val currentTimeNow : Long
//    get() = System.currentTimeMillis()
fun getUTCTimeNow(): Instant {
    return Clock.System.now()
}


fun String.toUUIDorNull(): Uuid? {
    return try {
        this.trim('"').let { Uuid.parse(it) }
    } catch (_: IllegalArgumentException) {
        null
    }
}

fun getSystemTimezoneOffsetMinutes(): Int {
    val now = Clock.System.now()
    val zone = TimeZone.currentSystemDefault()
    val offset = zone.offsetAt(now)
    return offset.totalSeconds / 60
}

