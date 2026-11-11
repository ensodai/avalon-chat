package com.ensodai.avalon.locale

actual object AppLocaleManager {
    private var savedLanguage: String? = null

    actual fun getSavedLanguage(): String? = savedLanguage

    actual fun saveLanguage(language: String) {
        savedLanguage = language
    }

    actual fun applyLanguage(language: String) {
        saveLanguage(language)
    }
}
