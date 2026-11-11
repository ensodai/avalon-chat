package app.data.network.models.request

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class CreateChatVariantRequest(
    @Contextual
    val chatSessionId: Uuid,
    @Contextual
    val chatVariantId: Uuid
)