package com.ensodai.avalon.data.db.dto

import com.ensodai.shared.avalon.gifts.models.*
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class AppUserDto(
    val id: Uuid,
    val name: String,
    val nickName: String,
    val avatarUrl: String?,
    val currentQuotaLimit: Int,
    val maxQuotaLimit: Int,
    val reserveBalance: Int,
    val maxReserveBalance: Int,
    val premiumStatus: PremiumStatus,
    val defaultPersonaId: Uuid?,
    val paymentProvider: SubscriptionProvider?,
    val nextBillingDate: Instant?,
    val isOutreachEnabled: Boolean = false,
    val dailyOutreachLimit: Int = 3,
    val isFlowModeEnabled: Boolean = false
)