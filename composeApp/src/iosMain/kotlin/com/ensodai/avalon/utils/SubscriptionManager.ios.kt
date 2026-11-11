package com.ensodai.avalon.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ensodai.shared.avalon.gifts.models.SubscriptionProvider

private object StubSubscriptionManager : SubscriptionManager {
    override fun manage(provider: SubscriptionProvider?) = Unit
}

@Composable
actual fun rememberSubscriptionManager(): SubscriptionManager {
    return remember { StubSubscriptionManager }
}
