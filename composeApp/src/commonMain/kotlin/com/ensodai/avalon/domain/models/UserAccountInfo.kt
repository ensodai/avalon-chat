package com.ensodai.avalon.domain.models

import com.ensodai.shared.avalon.gifts.models.*
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class UserAccountInfo(
    val name: String,
    val nickName: String,
    val id: Uuid?,
    val avatarUrl: String,
    val currentQuota: Int,
    val maxQuota: Int,
    val reserveBalance: Int,
    val maxReserveBalance: Int,
    val premiumStatus: PremiumStatus,
    val nextBillingDate: Instant?,
    val paymentProvider: SubscriptionProvider?,
    val charactersCount: Int,
    val totalUnclaimedCreatorEnergy: Double = 0.0,
    val isOutreachEnabled: Boolean,
    val dailyOutreachLimit: Int,
    val isEarlySupporter: Boolean = false
)