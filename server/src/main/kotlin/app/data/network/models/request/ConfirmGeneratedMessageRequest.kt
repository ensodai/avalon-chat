package app.data.network.models.request

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmGeneratedMessageRequest(
    val characterPublicId: String,
    val tokens: Int
)