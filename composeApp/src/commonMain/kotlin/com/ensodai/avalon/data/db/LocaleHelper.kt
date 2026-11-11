package com.ensodai.avalon.data.db

import com.ensodai.avalon.locale.AppLocaleManager

object LocaleHelper {

    private val localeMap = mapOf(
        "ru" to "Русский",
        "en" to "English",
//        "Español" to "es",  // Испанский
//        "Português" to "pt",  // Португальский
//        "Français" to "fr",  // Французский
//        "Deutsch" to "de",  // Немецкий
//        "中文 (简体)" to "zh-CN",  // Китайский (упрощённый)
//        "中文 (繁體)" to "zh-TW",  // Китайский (традиционный)
//        "日本語" to "ja",  // Японский
//        "한국어" to "ko",  // Корейский
//        "العربية" to "ar",  // Арабский
//        "हिन्दी" to "hi",  // Хинди
//        "Türkçe" to "tr",  // Турецкий
//        "Italiano" to "it",  // Итальянский
//        "Polski" to "pl",  // Польский
//        "Українська" to "uk",  // Украинский
//        "Tiếng Việt" to "vi",  // Вьетнамский
//        "Bahasa Indonesia" to "id"  // Индонезийский
    )

    private const val defaultLocaleCode = "en"

    fun getLocales(): List<String> {
        return localeMap.values.toList()
    }

    fun getLocaleCodeByName(locale: String?): String {
        val normalizedLocale = locale
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: AppLocaleManager.getSavedLanguage()
            ?: defaultLocaleCode

        val directCode = normalizeLanguageCode(normalizedLocale)
        if (localeMap.containsKey(directCode)) {
            return directCode
        }

        localeMap.entries.find { it.value == normalizedLocale }?.let { return it.key }

        val languageTagCode = normalizeLanguageCode(normalizedLocale)
        if (localeMap.containsKey(languageTagCode)) {
            return languageTagCode
        }

        return defaultLocaleCode
    }

    fun getLocaleNameByCode(locale: String?): String {
        return localeMap[getLocaleCodeByName(locale)] ?: localeMap.getValue(defaultLocaleCode)
    }

    private fun normalizeLanguageCode(locale: String): String {
        return locale
            .trim()
            .substringBefore('-')
            .substringBefore('_')
            .lowercase()
    }
}
