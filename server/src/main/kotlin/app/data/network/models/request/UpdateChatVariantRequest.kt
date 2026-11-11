package app.data.network.models.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateChatVariantRequest(
    val variantId: String,
    val name: String? = null,
    val removeImage: Boolean = false
)