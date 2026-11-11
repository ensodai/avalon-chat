package com.ensodai.avalon.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ensodai.shared.avalon.gifts.models.SubscriptionProvider

private class AndroidSubscriptionManager(
    private val context: Context
) : SubscriptionManager {
    override fun manage(provider: SubscriptionProvider?) {
        L.i("SubscriptionManager", "Self-hosted: subscription management is disabled")
    }
}

@Composable
actual fun rememberSubscriptionManager(): SubscriptionManager {
    val context = LocalContext.current
    return remember(context) {
        AndroidSubscriptionManager(context)
    }
}
