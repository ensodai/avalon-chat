package com.ensodai.avalon.presentation.mainScreen.viewState

import com.ensodai.avalon.data.db.dto.CategoryItemDto
import com.ensodai.avalon.domain.chatEvents.RpcConnectionState
import com.ensodai.avalon.presentation.core.BaseViewState
import com.ensodai.avalon.presentation.mainScreen.component.chatSection.ChatsScreens
import com.ensodai.avalon.utils.EMPTY_STRING
import kotlin.uuid.Uuid

data class MainScreenViewState(
    val selectedChats: List<Uuid> = mutableListOf(),
    val typingCharacters: Set<Uuid> = emptySet(),
    val connectionState: RpcConnectionState = RpcConnectionState.Idle,
    val isShowAlertDialogDeleteChat: Boolean = false,
    val isShowStartMessage: Boolean = false,
    val currentScreen: ChatsScreens = ChatsScreens.UserChatsScreen,
    val allScreenList: List<ChatsScreens> = listOf(
        ChatsScreens.UserChatsScreen,
        ChatsScreens.AllCharactersChatsScreen,
        ChatsScreens.GlobalLorebooksScreen
    ),
    val categories: List<CategoryItemDto> = emptyList(),
    val currentCategory: CategoryItemDto? = categories.firstOrNull(),
    val archivedCharacterNames: List<String> = emptyList(),
    val onboardingViewState: OnboardingViewState = OnboardingViewState(),
    val isShowOpenApiTransitionBottomSheet: Boolean = false,
    val searchQuery: String = EMPTY_STRING,
    val searchLoadingState: SearchLoadingState = SearchLoadingState.Idle,
    val screenMode: ScreenMode = ScreenMode.DefaultMode
) : BaseViewState() {

    val isShowAccountButton = screenMode == ScreenMode.DefaultMode
    val isShowSearchButton = screenMode == ScreenMode.DefaultMode
    val isShowDeleteButton = screenMode == ScreenMode.SelectedChatMode

}

sealed class ScreenMode {
    object DefaultMode : ScreenMode()
    object SelectedChatMode : ScreenMode()
    object SearchMode : ScreenMode()
}

sealed class SearchLoadingState {
    object Idle : SearchLoadingState()
    object Loading : SearchLoadingState()
    object Error : SearchLoadingState()
    object Success : SearchLoadingState()
}

data class OnboardingViewState(
    val isShowTelegramReminder: Boolean = false,
    val isShowOpenApiTransitionAnnouncement: Boolean = true
)

val MainScreenViewState.isEnableCategoryRow: Boolean
    get() = currentScreen == ChatsScreens.AllCharactersChatsScreen

val MainScreenViewState.isShowAddCharacterButton: Boolean
    get() = this.screenMode == ScreenMode.DefaultMode
