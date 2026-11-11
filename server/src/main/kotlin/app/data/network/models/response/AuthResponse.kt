package app.data.network.models.response

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    @Contextual
    val userId: Uuid
)