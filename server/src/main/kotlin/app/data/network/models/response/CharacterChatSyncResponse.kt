package app.data.network.models.response

import com.ensodai.shared.avalon.character.models.WelcomeCardResponse
import com.ensodai.shared.avalon.character.models.CharacterAvatarResponse
import com.ensodai.shared.avalon.character.models.CharacterImageResponse
import com.ensodai.shared.avalon.chat.models.ChatVariantResponse
import kotlinx.serialization.Serializable

@Serializable
class CharacterChatSyncResponse(
    val publicCharacterId: String,
    val owner: String,
    val ownerNick: String,
    val name: String,
    val shortDescription: String,
    val fullDescription: String?,
    val avatar: CharacterAvatarResponse?,
    val imageUrls: List<CharacterImageResponse>?,
    val isCharacterPublic: Boolean,
    val isPublicFullDescription: Boolean,
    val temperature: Float,
    val harassment: Int,
    val hate: Int,
    val sexuallyExplicit: Int,
    val dangerousContent: Int,
    val topK: Int?,
    val topP: Float,
    val maxOutputTokens: Int,
    val maxContextTokens: Int,
    val geminiModel: String,
    val isArchived: Boolean,
    val sessionId: String,
    val chatVariants: List<ChatVariantResponse>,
    val firstMessage: String?,
    val welcomeCard: WelcomeCardResponse?
)
