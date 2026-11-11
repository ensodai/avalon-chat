package app.data.network.models.request

import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.shared.avalon.character.models.WelcomeCardRequest
import kotlinx.serialization.Serializable

@Serializable
data class UpdateCharacterRequest(
    val characterName: String? = null,
    val fullDescription: String? = null,
    val shortDescription: String? = null,
    val temperature: Float? = null,
    val harassment: Int? = null,
    val hate: Int? = null,
    val sexuallyExplicit: Int? = null,
    val dangerousContent: Int? = null,
    val visibility: CharacterVisibility? = null,
    val isPublicFullDescription: Boolean? = null,
    val category: String? = null,
    val firstMessage: String? = null,
    val welcomeCard: WelcomeCardRequest? = null
)
