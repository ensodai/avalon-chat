package com.ensodai.avalon.data.db.dto

import com.ensodai.shared.avalon.gifts.models.*

data class UserTariffInfoDto(
    val premiumStatus: PremiumStatus,
    val paymentProvider: SubscriptionProvider?
)