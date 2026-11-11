package com.ensodai.avalon.domain.paymentManager

import android.content.Context

class AndroidBillingPlatformHelper(private val context: Context) : BillingPlatformHelper {
    override fun isRuStoreInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("ru.vk.store", 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
