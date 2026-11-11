package com.ensodai.avalon.locale

expect object AppLocaleManager {
    fun getSavedLanguage(): String?
    fun saveLanguage(language: String)
    fun applyLanguage(language: String)
}
