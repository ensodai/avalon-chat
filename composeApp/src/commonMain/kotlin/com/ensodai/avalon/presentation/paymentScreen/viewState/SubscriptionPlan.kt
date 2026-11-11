package com.ensodai.avalon.presentation.paymentScreen.viewState

import androidx.compose.ui.graphics.vector.ImageVector

data class SubscriptionPlan(
    val id: String,
    val tariff: UserTariff,
    val title: String,
    val price: String,
    val dailyEnergy: String,
    val maxCapacity: String,
    val features: List<String>
) {
    val icon: ImageVector get() = tariff.icon
}
