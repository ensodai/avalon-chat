package app.data.network.models.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateChatSessionRequest(
    val characterPublicId: String
)
