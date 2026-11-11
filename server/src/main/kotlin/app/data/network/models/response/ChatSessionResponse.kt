package app.data.network.models.response

import com.ensodai.shared.avalon.chat.models.ChatVariantResponse
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class ChatSessionResponse(
    @Contextual
    val id: Uuid,
    @Contextual
    val characterId: Uuid,
    val isArchived: Boolean,
    @Contextual
    val activeVariantId: Uuid?,
    @Contextual
    val updatedAt: Instant,
    val variants: List<ChatVariantResponse>
)
