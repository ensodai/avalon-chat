package com.ensodai.avalon.navigation

import androidx.navigation3.runtime.NavKey
import com.ensodai.avalon.presentation.editCharacterTextScreen.model.CharacterTextType
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
sealed interface Route : NavKey {
    fun getDefaultExtraRoute(): Route? = null

    @Serializable
    data object SplashScreen : Route

    @Serializable
    data class AppUpgradeScreen(val requiredVersion: String) : Route

    @Serializable
    data object MainScreen : Route

    @Serializable
    data object LoginScreen : Route

    @Serializable
    data class SettingsScreen(val guide: Boolean) : Route

    @Serializable
    data class ChatScreen(val characterPublicId: Uuid?, val showAiWarning: Boolean) : Route {
        override fun getDefaultExtraRoute(): Route? {
            return if (characterPublicId != null) {
                CharacterProfileScreen(
                    characterId = characterPublicId,
                    fullScreenImage = false,
                    characterHasAvatars = false
                )
            } else null
        }
    }

    @Serializable
    data class CharacterProfileScreen(
        val characterId: Uuid,
        val fullScreenImage: Boolean,
        val characterHasAvatars: Boolean
    ) : Route

    @Serializable
    data object AddCharacterScreen : Route

    @Serializable
    data object ImportCharacterScreen : Route

    @Serializable
    data object UserAccountScreen : Route

    @Serializable
    data class PersonaScreen(val id: Uuid?, val isUpdatePersona: Boolean) : Route

    @Serializable
    data object ArchiveScreen : Route

    @Serializable
    data class ChatVariantScreen(val characterId: Uuid?) : Route

    @Serializable
    data class EditUserProfileScreen(val nickname: String) : Route

    @Serializable
    data class EditCharacterScreen(val characterId: Uuid) : Route

    @Serializable
    data class EditCharacterFullDescriptionScreen(val characterId: Uuid) : Route

    @Serializable
    data class EditCharacterTextScreen(
        val characterId: Uuid,
        val textType: CharacterTextType
    ) : Route

    @Serializable
    data class EditCharacterAvatarScreen(val characterId: Uuid) : Route

    @Serializable
    data class EditTemperatureScreen(val characterId: Uuid) : Route

    @Serializable
    data class EditWelcomeCardScreen(val characterId: Uuid) : Route

    @Serializable
    data class CharacterLorebooksScreen(val characterId: Uuid) : Route

    @Serializable
    data class EditLorebookScreen(val lorebookId: Uuid) : Route

    @Serializable
    data class LorebookDetailsScreen(val lorebookId: Uuid) : Route

    @Serializable
    data object PaymentScreen : Route

    @Serializable
    data object UserCharacterScreen : Route

    @Serializable
    data class LiveCallScreen(
        val characterId: Uuid
    ) : Route

    @Serializable
    data object CustomizationScreen : Route

    @Serializable
    data object ChatPresetsScreen : Route

    @Serializable
    data class CharacterEventsScreen(val characterId: Uuid): Route

    @Serializable
    data class RelationshipRoadScreen(val characterId: Uuid): Route

    @Serializable
    data class CharacterOutreachSettingsScreen(val characterId: Uuid): Route

    @Serializable
    data class EditCharacterPrivatePhotosScreen(val characterId: Uuid) : Route

    @Serializable
    data object UserAiSettingsScreen : Route

    @Serializable
    data object SelectAiProviderScreen : Route

    @Serializable
    data object SummaryExplanationScreen : Route
}
