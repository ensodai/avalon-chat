package app.data.network.models.response

import app.data.network.models.response.images.ChatVariantImageResponse
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class ChatVariantResponse(
    @Contextual
    val id: Uuid,
    val name: String,
    val image: ChatVariantImageResponse?,
    @Contextual
    val sessionId: Uuid,
    @Contextual
    val createdAt: Instant,
    @Contextual
    val updatedAt: Instant,
)
