package app.data.network.models.response

import com.ensodai.shared.avalon.character.models.CharacterAvatarResponse
import com.ensodai.shared.avalon.character.models.CharacterImageResponse
import com.ensodai.shared.avalon.character.models.WelcomeCardImageResponse
import kotlinx.serialization.Serializable

@Serializable
data class UpdateCharacterResponse(
    val characterAvatar: CharacterAvatarResponse?,
    val characterImages: List<CharacterImageResponse>?,
    val welcomeCardImages: List<WelcomeCardImageResponse>?,
    val characterUpdatedAt: String
)
