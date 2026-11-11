package app.data.network.models.response

import com.ensodai.shared.avalon.chat.models.Rarity
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class ApiKeyResponse(
    @Contextual
    val id: Uuid,
    val apiKey: String?,
    val rarity: Rarity
)
