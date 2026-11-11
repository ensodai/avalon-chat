package com.ensodai.avalon.locale

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Handler
import android.os.LocaleList
import android.os.Looper
import androidx.core.content.edit
import java.lang.ref.WeakReference
import java.util.Locale

actual object AppLocaleManager {
    private const val PREFS_NAME = "app_locale_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    private var appContext: Context? = null
    private var currentActivity: WeakReference<Activity>? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun setCurrentActivity(activity: Activity) {
        currentActivity = WeakReference(activity)
    }

    fun wrap(context: Context): Context {
        val language = getSavedLanguage() ?: return context
        return updateContextLocale(context, Locale.forLanguageTag(language))
    }

    fun apply(activity: Activity, locale: Locale) {
        saveLanguage(locale.language)
        updateResources(activity, locale)
    }

    actual fun getSavedLanguage(): String? {
        val context = appContext ?: return null
        return context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, null)
            ?: context.resources.configuration.locales[0]?.language
    }

    actual fun saveLanguage(language: String) {
        val context = appContext ?: return
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(KEY_LANGUAGE, language)
            }
    }

    actual fun applyLanguage(language: String) {
        val normalizedLanguage = Locale.forLanguageTag(language).language.ifEmpty { language }
        val locale = Locale.forLanguageTag(normalizedLanguage)
        val previousLanguage = getSavedLanguage()

        saveLanguage(normalizedLanguage)
        appContext?.let { updateResources(it, locale) }

        val activity = currentActivity?.get() ?: return
        fun applyToActivity() {
            val currentLanguage = activity.resources.configuration.locales[0]?.language
            if (currentLanguage == normalizedLanguage && previousLanguage == normalizedLanguage) {
                return
            }

            updateResources(activity, locale)
            activity.recreate()
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            applyToActivity()
        } else {
            Handler(Looper.getMainLooper()).post {
                applyToActivity()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun updateResources(context: Context, locale: Locale) {
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        configuration.setLocales(LocaleList(locale))
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    private fun updateContextLocale(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLocales(LocaleList(locale))
        return ContextWrapper(context.createConfigurationContext(configuration))
    }
}
