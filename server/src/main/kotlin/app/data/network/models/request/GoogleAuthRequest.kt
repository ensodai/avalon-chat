package app.data.network.models.request

import kotlinx.serialization.Serializable

@Serializable
data class GoogleAuthRequest(
    val idToken: String,
    val deviceId: String,
    val deviceName: String
)

@Serializable
data class WebGoogleAuthRequest(
    val code: String,
    val redirectUri: String,
    val deviceId: String,
    val deviceName: String
)