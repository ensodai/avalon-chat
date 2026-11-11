package com.ensodai.avalon.presentation.navigation

import androidx.compose.runtime.Immutable
import com.ensodai.avalon.navigation.AvalonNavController
import com.ensodai.avalon.navigation.Route
import com.ensodai.avalon.presentation.editCharacterTextScreen.model.CharacterTextType
import kotlin.uuid.Uuid

@Immutable
interface Navigation {
    fun navigateToAddCharacterScreen()
    fun navigateToImportCharacterScreen()

    fun navigateToCharacterProfileScreen(
        characterId: Uuid,
        fullScreenImage: Boolean,
        characterHasAvatars: Boolean
    )

    fun navigateToEditCharacterScreen(characterId: Uuid)

    fun navigateToTimeChat()

    fun navigateToSettingScreen()

    fun navigateToSettingScreenWithGuide()

    fun navigateToCustomizationScreen()

    fun navigateToChatPresetsScreen()

    fun navigateToChatScreen(
        characterPublicId: Uuid
    )

    fun navigateBack()

    fun navigateToChatScreenFromProfile(characterPublicId: Uuid?)

    fun navigateToChatScreenFromAddScreen(chatId: Int)

    fun navigateToUserAccountScreen()

    fun navigateToEditPersonaScreen(id: Uuid)

    fun navigateToCreatePersonaScreen()

    fun navigateToArchiveScreen()

    fun navigateToChatVariantScreen(characterId: Uuid?)

    fun navigateToEditUserProfileScreen(nickname: String)

    fun navigateToEditCharacterFullDescriptionScreen(characterId: Uuid)

    fun navigateToEditCharacterTextScreen(characterId: Uuid, textType: CharacterTextType)

    fun navigateToEditCharacterAvatarScreen(characterId: Uuid)

    fun navigateToEditCharacterPrivatePhotosScreen(characterId: Uuid)

    fun navigateToEditTemperatureScreen(characterId: Uuid)

    fun navigateToEditWelcomeCardScreen(characterId: Uuid)

    fun navigateToCharacterLorebooksScreen(characterId: Uuid)

    fun navigateToEditLorebookScreen(lorebookId: Uuid)

    fun navigateToLorebookDetailsScreen(lorebookId: Uuid)

    fun navigateToPaymentScreen()

    fun navigateToUserCharacterScreen()

    fun navigateToLiveCallScreen(characterId: Uuid)

    fun navigateToCharacterEventsScreen(characterId: Uuid)

    fun navigateToRelationshipRoadScreen(characterId: Uuid)

    fun navigateToCharacterOutreachSettings(characterId: Uuid)

    fun navigateToUserAiSettingsScreen()

    fun navigateToSelectAiProviderScreen()

    fun navigateToSummaryExplanationScreen()
}

@Immutable
class NavigationAvalon(
    private val navController: AvalonNavController<Route>,
    private val showAiWarning: Boolean
) : Navigation {

    override fun navigateToAddCharacterScreen() {
        navController.navigate(Route.AddCharacterScreen)
    }

    override fun navigateToImportCharacterScreen() {
        navController.navigate(Route.ImportCharacterScreen)
    }

    override fun navigateToCharacterProfileScreen(
        characterId: Uuid,
        fullScreenImage: Boolean,
        characterHasAvatars: Boolean
    ) {
        navController.navigate(
            Route.CharacterProfileScreen(
                characterId = characterId,
                fullScreenImage = fullScreenImage,
                characterHasAvatars = characterHasAvatars
            )
        )
    }

    override fun navigateToEditCharacterScreen(characterId: Uuid) {
        navController.navigate(Route.EditCharacterScreen(characterId))
    }

    override fun navigateToTimeChat() {
        // TODO: Time chat flow is still undefined in the migrated navigation contract.
    }

    override fun navigateToSettingScreen() {
        navController.navigate(Route.SettingsScreen(guide = false))
    }

    override fun navigateToSettingScreenWithGuide() {
        navController.navigate(Route.SettingsScreen(guide = true))
    }

    override fun navigateToCustomizationScreen() {
        navController.navigate(Route.CustomizationScreen)
    }

    override fun navigateToChatPresetsScreen() {
        navController.navigate(Route.ChatPresetsScreen)
    }

    override fun navigateToChatScreen(characterPublicId: Uuid) {
        navController.navigate(Route.ChatScreen(characterPublicId = characterPublicId, showAiWarning = showAiWarning))
    }

    override fun navigateBack() {
        navController.pop()
    }

    override fun navigateToChatScreenFromProfile(characterPublicId: Uuid?) {
        navController.navigate(Route.ChatScreen(characterPublicId = characterPublicId, showAiWarning = showAiWarning))
    }

    override fun navigateToChatScreenFromAddScreen(chatId: Int) {
        // TODO: Legacy add->chat transition is not modeled in Avalon routes yet.
    }

    override fun navigateToUserAccountScreen() {
        navController.navigate(Route.UserAccountScreen)
    }

    override fun navigateToEditPersonaScreen(id: Uuid) {
        navController.navigate(
            Route.PersonaScreen(
                id = id,
                isUpdatePersona = true
            )
        )
    }

    override fun navigateToCreatePersonaScreen() {
        navController.navigate(
            Route.PersonaScreen(
                id = null,
                isUpdatePersona = false
            )
        )
    }

    override fun navigateToArchiveScreen() {
        navController.navigate(Route.ArchiveScreen)
    }

    override fun navigateToChatVariantScreen(characterId: Uuid?) {
        navController.navigate(Route.ChatVariantScreen(characterId))
    }

    override fun navigateToEditUserProfileScreen(nickname: String) {
        navController.navigate(Route.EditUserProfileScreen(nickname))
    }

    override fun navigateToEditCharacterFullDescriptionScreen(characterId: Uuid) {
        navController.navigate(Route.EditCharacterFullDescriptionScreen(characterId))
    }

    override fun navigateToEditCharacterTextScreen(characterId: Uuid, textType: CharacterTextType) {
        navController.navigate(Route.EditCharacterTextScreen(characterId, textType))
    }

    override fun navigateToEditCharacterAvatarScreen(characterId: Uuid) {
        navController.navigate(Route.EditCharacterAvatarScreen(characterId))
    }

    override fun navigateToEditCharacterPrivatePhotosScreen(characterId: Uuid) {
        navController.navigate(Route.EditCharacterPrivatePhotosScreen(characterId))
    }

    override fun navigateToEditTemperatureScreen(characterId: Uuid) {
        navController.navigate(Route.EditTemperatureScreen(characterId))
    }

    override fun navigateToEditWelcomeCardScreen(characterId: Uuid) {
        navController.navigate(Route.EditWelcomeCardScreen(characterId))
    }

    override fun navigateToCharacterLorebooksScreen(characterId: Uuid) {
        navController.navigate(Route.CharacterLorebooksScreen(characterId))
    }

    override fun navigateToEditLorebookScreen(lorebookId: Uuid) {
        navController.navigate(Route.EditLorebookScreen(lorebookId = lorebookId))
    }

    override fun navigateToLorebookDetailsScreen(lorebookId: Uuid) {
        navController.navigate(Route.LorebookDetailsScreen(lorebookId = lorebookId))
    }

    override fun navigateToPaymentScreen() {
        navController.navigate(Route.PaymentScreen)
    }

    override fun navigateToUserCharacterScreen() {
        navController.navigate(Route.UserCharacterScreen)
    }

    override fun navigateToLiveCallScreen(characterId: Uuid) {
        navController.navigate(Route.LiveCallScreen(characterId = characterId))
    }

    override fun navigateToCharacterEventsScreen(characterId: Uuid) {
        navController.navigate(Route.CharacterEventsScreen(characterId = characterId))
    }

    override fun navigateToRelationshipRoadScreen(characterId: Uuid) {
        navController.navigate(Route.RelationshipRoadScreen(characterId = characterId))
    }

    override fun navigateToCharacterOutreachSettings(characterId: Uuid) {
        navController.navigate(Route.CharacterOutreachSettingsScreen(characterId = characterId))
    }

    override fun navigateToUserAiSettingsScreen() {
        navController.navigate(Route.UserAiSettingsScreen)
    }

    override fun navigateToSelectAiProviderScreen() {
        navController.navigate(Route.SelectAiProviderScreen)
    }

    override fun navigateToSummaryExplanationScreen() {
        navController.navigate(Route.SummaryExplanationScreen)
    }
}
