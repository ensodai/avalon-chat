package com.ensodai.shared.avalon.user.models

import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import com.ensodai.shared.avalon.gifts.models.SubscriptionProvider
import com.ensodai.shared.avalon.persona.models.PersonaResponse
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class UpdateNicknameRequest(
    val nickname: String
)

@Serializable
data class UpdateNicknameResponse(
    val success: Boolean,
    val message: String? = null
)

@Serializable
data class CheckNicknameRequest(val nickname: String)

@Serializable
data class AvailableResultResponse(
    val available: Boolean
)

@Serializable
data class UserAccountInfoResponse(
    val name: String,
    val nickName: String,
    val id: Uuid,
    val avatarUrl: String,
    val currentQuota: Int,
    val maxQuota: Int,
    val reserveBalance: Int,
    val maxReserveBalance: Int,
    val premiumStatus: PremiumStatus,
    val defaultPersona: PersonaResponse?,
    val userPersonas: List<PersonaResponse>,
    val paymentProvider: SubscriptionProvider?,
    val nextBillingDate: Instant?,
    val totalUnclaimedCreatorEnergy: Double = 0.0,
    val isOutreachEnabled: Boolean,
    val dailyOutreachLimit: Int = 3,
    val isFlowModeEnabled: Boolean = false,
    val isEarlySupporter: Boolean = false
)

@Serializable
data class UpdateUserOutreachRequest(
    val isOutreachEnabled: Boolean,
    val dailyOutreachLimit: Int = 3
)

@Serializable
data class UpdateUserFlowModeRequest(
    val isFlowModeEnabled: Boolean
)
