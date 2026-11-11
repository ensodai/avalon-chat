package com.ensodai.avalon.navigation

import com.ensodai.avalon.presentation.editCharacterTextScreen.model.CharacterTextType
import com.ensodai.avalon.presentation.navigation.Navigation
import kotlin.uuid.Uuid

/**
 * Оркестратор навигации для рабочего пространства (Shallow Stack Extractor).
 * Паттерн: Декоратор над [Navigation].
 * 
 * Гарантирует, что логический стек не засоряется устаревшими DETAIL и EXTRA экранами.
 * Реализует алгоритм "Плоского стека":
 * - Перед открытием нового DETAIL-экрана удаляются старые DETAIL и EXTRA.
 * - Перед открытием нового EXTRA-экрана удаляются старые EXTRA.
 * 
 * Таким образом, в памяти всегда хранится не более одного DETAIL и одного EXTRA,
 * а кнопка "Назад" всегда возвращает к логически правильному родительскому экрану.
 */
class WorkspaceNavigation(
    private val rootController: AvalonNavController<Route>,
    private val baseNavigation: Navigation
) : Navigation by baseNavigation {

    private fun cleanStackForDetail() {
        val stack = rootController.backStack
        // Идем с конца, чтобы безопасно удалять элементы по индексу
        var i = stack.size - 1
        while (i >= 0) {
            val role = stack[i].getPaneRole()
            if (role == PaneRole.DETAIL || role == PaneRole.EXTRA) {
                stack.removeAt(i)
            }
            i--
        }
    }

    private fun cleanStackForExtra() {
        val stack = rootController.backStack
        var i = stack.size - 1
        while (i >= 0) {
            val role = stack[i].getPaneRole()
            if (role == PaneRole.EXTRA) {
                stack.removeAt(i)
            }
            i--
        }
    }

    // --- DETAIL ROUTES ---

    override fun navigateToAddCharacterScreen() {
        cleanStackForDetail()
        baseNavigation.navigateToAddCharacterScreen()
    }

    override fun navigateToImportCharacterScreen() {
        cleanStackForDetail()
        baseNavigation.navigateToImportCharacterScreen()
    }

    override fun navigateToCustomizationScreen() {
        cleanStackForDetail()
        baseNavigation.navigateToCustomizationScreen()
    }

    override fun navigateToChatScreen(characterPublicId: Uuid) {
        cleanStackForDetail()
        baseNavigation.navigateToChatScreen(characterPublicId)
    }

    override fun navigateToChatScreenFromProfile(characterPublicId: Uuid?) {
        cleanStackForDetail()
        baseNavigation.navigateToChatScreenFromProfile(characterPublicId)
    }

    override fun navigateToChatScreenFromAddScreen(chatId: Int) {
        cleanStackForDetail()
        baseNavigation.navigateToChatScreenFromAddScreen(chatId)
    }

    override fun navigateToUserAccountScreen() {
        cleanStackForDetail()
        baseNavigation.navigateToUserAccountScreen()
    }

    override fun navigateToEditCharacterScreen(characterId: Uuid) {
        cleanStackForDetail()
        baseNavigation.navigateToEditCharacterScreen(characterId)
    }

    // --- EXTRA ROUTES ---

    override fun navigateToEditUserProfileScreen(nickname: String) {
        cleanStackForExtra()
        baseNavigation.navigateToEditUserProfileScreen(nickname)
    }

    override fun navigateToChatPresetsScreen() {
        cleanStackForExtra()
        baseNavigation.navigateToChatPresetsScreen()
    }

    override fun navigateToEditPersonaScreen(id: Uuid) {
        cleanStackForExtra()
        baseNavigation.navigateToEditPersonaScreen(id)
    }

    override fun navigateToCreatePersonaScreen() {
        cleanStackForExtra()
        baseNavigation.navigateToCreatePersonaScreen()
    }

    override fun navigateToChatVariantScreen(characterId: Uuid?) {
        cleanStackForExtra()
        baseNavigation.navigateToChatVariantScreen(characterId)
    }

    // --- PRIMARY ROUTES (No stack cleaning) ---
    override fun navigateToSettingScreen() {
        baseNavigation.navigateToSettingScreen()
    }

    override fun navigateToSettingScreenWithGuide() {
        baseNavigation.navigateToSettingScreenWithGuide()
    }

    override fun navigateToCharacterProfileScreen(
        characterId: Uuid,
        fullScreenImage: Boolean,
        characterHasAvatars: Boolean
    ) {
        cleanStackForExtra()
        baseNavigation.navigateToCharacterProfileScreen(characterId, fullScreenImage, characterHasAvatars)
    }



    override fun navigateToEditCharacterFullDescriptionScreen(characterId: Uuid) {
        cleanStackForExtra()
        baseNavigation.navigateToEditCharacterFullDescriptionScreen(characterId)
    }

    override fun navigateToEditCharacterTextScreen(characterId: Uuid, textType: CharacterTextType) {
        cleanStackForExtra()
        baseNavigation.navigateToEditCharacterTextScreen(characterId, textType)
    }

    override fun navigateToEditCharacterAvatarScreen(characterId: Uuid) {
        cleanStackForExtra()
        baseNavigation.navigateToEditCharacterAvatarScreen(characterId)
    }

    override fun navigateToEditCharacterPrivatePhotosScreen(characterId: Uuid) {
        cleanStackForExtra()
        baseNavigation.navigateToEditCharacterPrivatePhotosScreen(characterId)
    }

    override fun navigateToEditTemperatureScreen(characterId: Uuid) {
        cleanStackForExtra()
        baseNavigation.navigateToEditTemperatureScreen(characterId)
    }

    override fun navigateToEditWelcomeCardScreen(characterId: Uuid) {
        cleanStackForExtra()
        baseNavigation.navigateToEditWelcomeCardScreen(characterId)
    }

    override fun navigateToCharacterLorebooksScreen(characterId: Uuid) {
        cleanStackForExtra()
        baseNavigation.navigateToCharacterLorebooksScreen(characterId)
    }

    override fun navigateToEditLorebookScreen(lorebookId: Uuid) {
        cleanStackForExtra()
        baseNavigation.navigateToEditLorebookScreen(lorebookId)
    }

    override fun navigateToLorebookDetailsScreen(lorebookId: Uuid) {
        cleanStackForExtra()
        baseNavigation.navigateToLorebookDetailsScreen(lorebookId)
    }

    override fun navigateToCharacterEventsScreen(characterId: Uuid) {
        cleanStackForExtra()
        baseNavigation.navigateToCharacterEventsScreen(characterId)
    }

    override fun navigateToRelationshipRoadScreen(characterId: Uuid) {
        cleanStackForExtra()
        baseNavigation.navigateToRelationshipRoadScreen(characterId)
    }

    override fun navigateToCharacterOutreachSettings(characterId: Uuid) {
        cleanStackForExtra()
        baseNavigation.navigateToCharacterOutreachSettings(characterId)
    }

    override fun navigateToUserAiSettingsScreen() {
        cleanStackForDetail()
        baseNavigation.navigateToUserAiSettingsScreen()
    }

    override fun navigateToSelectAiProviderScreen() {
        cleanStackForExtra()
        baseNavigation.navigateToSelectAiProviderScreen()
    }

    override fun navigateToSummaryExplanationScreen() {
        cleanStackForExtra()
        baseNavigation.navigateToSummaryExplanationScreen()
    }
}
