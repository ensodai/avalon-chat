package com.ensodai.avalon.utils

import androidx.compose.runtime.Composable
import com.ensodai.shared.avalon.gifts.models.SubscriptionProvider

interface SubscriptionManager {
    fun manage(provider: SubscriptionProvider?)
}

@Composable
expect fun rememberSubscriptionManager(): SubscriptionManager
