package com.ensodai.avalon

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ensodai.avalon.domain.paymentManager.CurrentActivityProvider
import com.ensodai.avalon.locale.AppLocaleManager
import org.koin.android.ext.android.inject
import java.util.Locale

open class BaseActivity : AppCompatActivity() {
    private val activityProvider: CurrentActivityProvider by inject()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocaleManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityProvider.setCurrentActivity(this)
        AppLocaleManager.setCurrentActivity(this)
    }

    override fun onResume() {
        super.onResume()
        activityProvider.setCurrentActivity(this)
        AppLocaleManager.setCurrentActivity(this)
    }

    override fun createConfigurationContext(overrideConfiguration: Configuration): Context {
        val context = super.createConfigurationContext(overrideConfiguration)
        return AppLocaleManager.wrap(context)
    }

    override fun getApplicationContext(): Context =
        AppLocaleManager.wrap(super.getApplicationContext())

    open fun updateLocale(locale: Locale) {
        AppLocaleManager.apply(this, locale)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
