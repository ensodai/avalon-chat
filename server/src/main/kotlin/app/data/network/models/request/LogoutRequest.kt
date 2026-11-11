package app.data.network.models.request

import kotlinx.serialization.Serializable

@Serializable
data class LogoutRequest(val refreshToken: String)
