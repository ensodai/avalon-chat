package com.ensodai.avalon.domain.useCases.editUserProfileScreenUseCase

import org.koin.core.annotation.Single

@Single
class NicknameValidator {

    companion object {
        const val MAX_LENGTH = 20
        const val MIN_LENGTH = 3
    }

    private val ALLOWED_SYMBOLS = "[a-zA-Z0-9_]".toRegex()
    private val CONSECUTIVE_SPECIAL_CHARS_REGEX = "_{2,}".toRegex()
    private val INAPPROPRIATE_WORDS =
        setOf("admin", "moderator", "root", "support", "null", "undefined")
    private val SYSTEM_PREFIXES = setOf("sys", "system", "user", "usr", "account", "test")

    fun validate(nickname: String): ValidationResult {
        if (nickname.isEmpty()) return ValidationResult.Empty

        val trimmed = nickname.trim()

        return when {
            trimmed.length < MIN_LENGTH -> ValidationResult.TooShort
            trimmed.length > MAX_LENGTH -> ValidationResult.TooLong
            !isAllValidCharacters(trimmed) -> ValidationResult.InvalidCharacters
            startsOrEndsWithSpecialChar(trimmed) -> ValidationResult.InvalidStartOrEnd
            hasConsecutiveSpecialChars(trimmed) -> ValidationResult.ConsecutiveSpecialChars
            containsInvalidWords(trimmed) -> ValidationResult.Inappropriate
            isSystemName(trimmed) -> ValidationResult.SystemName
            else -> ValidationResult.Available
        }
    }

    private fun isAllValidCharacters(nickname: String): Boolean {
        return nickname.all { char ->
            ALLOWED_SYMBOLS.matches(char.toString())
        }
    }

    private fun startsOrEndsWithSpecialChar(nickname: String): Boolean {
        return nickname.first() == '_' || nickname.last() == '_'
    }

    private fun hasConsecutiveSpecialChars(nickname: String): Boolean {
        return CONSECUTIVE_SPECIAL_CHARS_REGEX.containsMatchIn(nickname)
    }

    private fun containsInvalidWords(nickname: String): Boolean {
        val lowerCaseNick = nickname.lowercase()
        return INAPPROPRIATE_WORDS.any { word ->
            lowerCaseNick.contains(word)
        }
    }

    private fun isSystemName(nickname: String): Boolean {
        if (nickname.length < 3) return false

        val lowerCaseNick = nickname.lowercase()

        if (SYSTEM_PREFIXES.contains(lowerCaseNick)) return true

        return SYSTEM_PREFIXES.any { prefix ->
            lowerCaseNick.startsWith(prefix) &&
                    (nickname.length == prefix.length || nickname[prefix.length].isDigit())
        }
    }

    sealed class ValidationResult {
        // Финальное состояние успеха - никнейм точно доступен
        object Available : ValidationResult()

        // Локальные ошибки
        object Empty : ValidationResult()
        object TooShort : ValidationResult()
        object TooLong : ValidationResult()
        object InvalidCharacters : ValidationResult()
        object InvalidStartOrEnd : ValidationResult()
        object ConsecutiveSpecialChars : ValidationResult()
        object Inappropriate : ValidationResult()
        object SystemName : ValidationResult()

        // Промежуточные и серверные состояния
        object CheckingAvailability : ValidationResult() // Идет проверка
        object Taken : ValidationResult() // Занят по мнению сервера

        // Можно добавить состояние для сетевых ошибок
        object NetworkError : ValidationResult()
    }
}
