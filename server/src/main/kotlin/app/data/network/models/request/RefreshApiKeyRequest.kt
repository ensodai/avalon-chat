package app.data.network.models.request

import kotlinx.serialization.Serializable

@Serializable
class RefreshApiKeyRequest(
    val apiKeyId: String,
    val reason: String? = null
)