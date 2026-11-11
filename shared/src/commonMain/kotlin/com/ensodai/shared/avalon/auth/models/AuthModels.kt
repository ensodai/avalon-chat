@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.ensodai.shared.avalon.auth.models

import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import com.ensodai.shared.avalon.gifts.models.SubscriptionProvider
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class AppUserResponse(
    @Contextual
    val id: Uuid,
    val name: String,
    val nickName: String,
    val avatarUrl: String?,
    val currentQuotaLimit: Int,
    val maxQuotaLimit: Int,
    val reserveBalance: Int,
    val maxReserveBalance: Int,
    val premiumStatus: PremiumStatus,
    @Contextual
    val defaultPersonaId: Uuid?,
    val paymentProvider: SubscriptionProvider?,
    @Contextual
    val nextBillingDate: Instant?,
    val isOutreachEnabled: Boolean = false,
    val dailyOutreachLimit: Int = 3,
    val isFlowModeEnabled: Boolean = false
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: Uuid
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    val deviceId: String,
    val deviceName: String,
    val timezoneOffset: Int? = null,
    val fcmToken: String? = null
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val deviceId: String,
    val deviceName: String,
    val timezoneOffset: Int? = null,
    val fcmToken: String? = null
)

@Serializable
data class GoogleAuthRequest(
    val idToken: String,
    val deviceId: String,
    val deviceName: String,
    val timezoneOffset: Int? = null,
    val fcmToken: String? = null
)

@Serializable
data class WebGoogleAuthRequest(
    val code: String,
    val redirectUri: String,
    val deviceId: String,
    val deviceName: String,
    val timezoneOffset: Int? = null,
    val fcmToken: String? = null
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
    val timezoneOffset: Int? = null,
    val fcmToken: String? = null
)

@Serializable
data class LogoutRequest(val refreshToken: String)

@Serializable
sealed class AuthState {
    @Serializable
    @SerialName("guest")
    data object Guest : AuthState()

    @Serializable
    @SerialName("authorized")
    data class Authorized(
        @Contextual val userId: Uuid,
        @Contextual val sessionId: Uuid,
        val username: String
    ) : AuthState()

    @Serializable
    @SerialName("expired")
    data object TokenExpired : AuthState()
}
