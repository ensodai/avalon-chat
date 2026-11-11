package app.data.network.models.response.images

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class ChatVariantImageResponse(
    @Contextual
    val id: Uuid,
    val imageUrl: String,
    val thumbHash: String? = null
)
