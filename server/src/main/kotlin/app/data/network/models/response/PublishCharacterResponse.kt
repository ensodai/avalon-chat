package app.data.network.models.response

import com.ensodai.shared.avalon.character.models.CharacterResponse
import com.ensodai.shared.avalon.chat.models.ChatSessionPreviewResponse
import com.ensodai.shared.avalon.chat.models.ChatVariantResponse
import kotlinx.serialization.Serializable

@Serializable
data class PublishCharacterResponse(
    val character: CharacterResponse,
    val chatSessionResponse: ChatSessionPreviewResponse,
    val chatVariant: ChatVariantResponse
)