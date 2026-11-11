package com.ensodai.avalon.presentation.mainScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.data.ResourceWrapper
import com.ensodai.avalon.data.db.dto.CategoryItemDto
import com.ensodai.avalon.domain.sessionWorker.DeletionType
import com.ensodai.avalon.domain.sessionWorker.DeletionUndoManager
import com.ensodai.avalon.domain.useCases.commonUseCase.ArchiveChatSessionsUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.auth.AuthenticationUserFromChatUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.promoFlags.DisableOpenApiTransitionAnnouncementPromoFlagUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.promoFlags.DisableTelegramReminderPromoFlagUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.promoFlags.GetShowPromoFlagsFlowUseCase
import com.ensodai.avalon.domain.useCases.mainScreenUseCase.CheckSavedUserChatsUseCase
import com.ensodai.avalon.domain.useCases.mainScreenUseCase.GetAllTypingCharactersIdsFlowUseCase
import com.ensodai.avalon.domain.useCases.mainScreenUseCase.GetArchivedCharacterNamesFlowUseCase
import com.ensodai.avalon.domain.useCases.mainScreenUseCase.GetCategoriesFlowUseCase
import com.ensodai.avalon.domain.useCases.mainScreenUseCase.LoadCharactersCategoryUseCase
import com.ensodai.avalon.domain.useCases.mainScreenUseCase.RefreshCategoryCountersUseCase
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.deleted_chats_count
import com.ensodai.avalon.presentation.mainScreen.MainScreenViewModel
import com.ensodai.avalon.presentation.mainScreen.component.chatSection.ChatsScreens
import com.ensodai.avalon.presentation.mainScreen.viewState.ScreenMode
import com.ensodai.avalon.presentation.mainScreen.viewState.SearchLoadingState
import com.ensodai.avalon.utils.EMPTY_STRING
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

fun MainScreenViewModel.openUserChatsScreen() {
    updateViewState {
        it.copy()
    }
}

fun MainScreenViewModel.openAllCharactersChatsScreen() {
    updateViewState {
        it.copy(currentScreen = ChatsScreens.AllCharactersChatsScreen)
    }
}

fun MainScreenViewModel.updateTypingStatusForChats(getAllTypingCharactersIdsFlow: GetAllTypingCharactersIdsFlowUseCase) {
    viewModelScope.launch(context = IODispatcher) {
        getAllTypingCharactersIdsFlow
            .get()
            .distinctUntilChanged()
            .collect { value ->
                updateViewState {
                    it.copy(typingCharacters = value)
                }
            }
    }
}


fun MainScreenViewModel.authentication(
    tokenId: String?,
    authenticationUser: AuthenticationUserFromChatUseCase
) {
    if (tokenId == null) return
    viewModelScope.launch(context = IODispatcher) {
        authenticationUser.auth(tokenId)
    }
}

fun MainScreenViewModel.selectChat(chatId: Uuid) {
    if (viewState.value.screenMode == ScreenMode.SelectedChatMode) {
        updateViewState {
            val updatedSelectedChats = if (chatId in it.selectedChats) {
                it.selectedChats - chatId
            } else {
                it.selectedChats + chatId
            }
            it.copy(selectedChats = updatedSelectedChats)
        }
        if (viewState.value.selectedChats.isEmpty()) {
            updateViewState { it.copy(screenMode = ScreenMode.DefaultMode) }
        }
    }
}

fun MainScreenViewModel.enableSelectionChats(chatId: Uuid) {
    updateViewState {
        it.copy(
            selectedChats = it.selectedChats + chatId,
            screenMode = ScreenMode.SelectedChatMode
        )
    }
}

fun MainScreenViewModel.setDefaultScreenMode() {
    updateViewState {
        it.copy(
            selectedChats = emptyList(),
            screenMode = ScreenMode.DefaultMode,
            searchQuery = EMPTY_STRING
        )
    }
}

fun MainScreenViewModel.syncCategory(loadCharactersCategory: LoadCharactersCategoryUseCase) {
    viewModelScope.launch(context = IODispatcher) {
        loadCharactersCategory.load()
    }
}

fun MainScreenViewModel.refreshCategoryCounters(refreshCategoryCounters: RefreshCategoryCountersUseCase) {
    viewModelScope.launch(IODispatcher) {
        refreshCategoryCounters.refresh()
    }
}

fun MainScreenViewModel.getCategories(
    getCategoriesFlow: GetCategoriesFlowUseCase
) {
    viewModelScope.launch(context = IODispatcher) {
        getCategoriesFlow
            .get()
            .collect { listCategories ->
                updateViewState { viewState ->
                    // Сохраняем текущую категорию по categoryCode
                    val currentCategoryCode = viewState.currentCategory?.categoryCode
                    val newCurrentCategory = if (currentCategoryCode != null) {
                        listCategories.find { it.categoryCode == currentCategoryCode }
                    } else {
                        null
                    } ?: listCategories.firstOrNull()

                    viewState.copy(
                        categories = listCategories,
                        currentCategory = newCurrentCategory,
                    )
                }
            }
    }
}

fun MainScreenViewModel.deleteChatConfigAndMessage(
    deletionUndoManager: DeletionUndoManager,
    resourceWrapper: ResourceWrapper
) {
    viewModelScope.launch(context = IODispatcher) {
        val selectedChats = viewState.value.selectedChats
        val displayText = resourceWrapper.getQuantityString(
            resource = Res.plurals.deleted_chats_count,
            quantity = selectedChats.size,
            selectedChats.size
        )

        deletionUndoManager.startDeletion(
            ids = selectedChats,
            type = DeletionType.CHAT_SESSION,
            displayText = displayText,
        )
        updateViewState {
            it.copy(
                selectedChats = emptyList(),
                isShowAlertDialogDeleteChat = false,
                screenMode = ScreenMode.DefaultMode
            )
        }
    }
}

fun MainScreenViewModel.showAlertDialogDeleteShat() {
    updateViewState {
        it.copy(isShowAlertDialogDeleteChat = true)
    }
}

fun MainScreenViewModel.closeAlertDialog() {
    updateViewState {
        it.copy(isShowAlertDialogDeleteChat = false)
    }
}

fun MainScreenViewModel.updateStartMessageVisible(checkSavedUserChats: CheckSavedUserChatsUseCase) {
    viewModelScope.launch(IODispatcher) {
        checkSavedUserChats
            .check()
            .distinctUntilChanged()
            .collect { value ->
                updateViewState {
                    it.copy(
                        isShowStartMessage = !value,
                    )
                }
            }
    }
}

fun MainScreenViewModel.selectCurrentCategory(category: CategoryItemDto) {
    updateViewState {
        it.copy(
            currentCategory = category,
        )
    }
}

fun MainScreenViewModel.archivedSelectedChats(archivedChats: ArchiveChatSessionsUseCase) {
    viewModelScope.launch(IODispatcher) {
        val selectedChats = viewState.value.selectedChats
        archivedChats.archive(selectedChats)
        updateViewState {
            it.copy(
                selectedChats = emptyList(),
                screenMode = ScreenMode.DefaultMode
            )
        }
    }
}

fun MainScreenViewModel.updateArchivedCharacterNames(getArchivedCharacterNamesFlow: GetArchivedCharacterNamesFlowUseCase) {
    viewModelScope.launch(IODispatcher) {
        getArchivedCharacterNamesFlow
            .get()
            .distinctUntilChanged()
            .collect { list ->
                updateViewState {
                    it.copy(archivedCharacterNames = list)
                }
            }
    }
}

fun MainScreenViewModel.updateArchiveItemIsVisible(getArchivedCharacterNamesFlow: GetArchivedCharacterNamesFlowUseCase) {
//    viewModelScope.launch(IODispatcher) {
//        getArchivedCharacterNamesFlow
//            .get()
//            .distinctUntilChanged { old, new ->
//                old.isEmpty() == new.isEmpty()
//            }
//            .collect { list ->
//                updateViewState {
//                    it.copy(isShowArchiveSection = list.isNotEmpty())
//                }
//            }
//    }
}

fun MainScreenViewModel.updateOnboardingState(getShowPromoFlagsFlow: GetShowPromoFlagsFlowUseCase) {
    viewModelScope.launch(IODispatcher) {
        getShowPromoFlagsFlow
            .get()
            .distinctUntilChanged()
            .collect {
                updateViewState { viewState ->
                    viewState.copy(
                        onboardingViewState = viewState.onboardingViewState.copy(
                            isShowTelegramReminder = it?.telegramReminder ?: false,
                            isShowOpenApiTransitionAnnouncement = it?.openApiTransitionAnnouncement ?: true
                        )
                    )
                }
            }
    }
}

fun MainScreenViewModel.closeTelegramReminder(disableTelegramReminderPromoFlag: DisableTelegramReminderPromoFlagUseCase) {
    updateViewState { viewState ->
        viewState.copy(
            onboardingViewState = viewState.onboardingViewState.copy(
                isShowTelegramReminder = false
            )
        )
    }
    viewModelScope.launch(IODispatcher) {
        disableTelegramReminderPromoFlag.disable()
    }
}

fun MainScreenViewModel.closeOpenApiTransitionAnnouncement(disableAnnouncement: DisableOpenApiTransitionAnnouncementPromoFlagUseCase) {
    updateViewState { viewState ->
        viewState.copy(
            onboardingViewState = viewState.onboardingViewState.copy(
                isShowOpenApiTransitionAnnouncement = false
            )
        )
    }
    viewModelScope.launch(IODispatcher) {
        disableAnnouncement.disable()
    }
}

fun MainScreenViewModel.openOpenApiTransitionBottomSheet() {
    updateViewState {
        it.copy(isShowOpenApiTransitionBottomSheet = true)
    }
}

fun MainScreenViewModel.dismissOpenApiTransitionBottomSheet() {
    updateViewState {
        it.copy(isShowOpenApiTransitionBottomSheet = false)
    }
}

fun MainScreenViewModel.enableSearchMode() {
    updateViewState {
        it.copy(screenMode = ScreenMode.SearchMode)
    }
}

fun MainScreenViewModel.selectScreenPage(page: Int) {
    val currentScreen = when (page) {
        0 -> ChatsScreens.UserChatsScreen
        1 -> ChatsScreens.AllCharactersChatsScreen
        else -> return
    }
    updateViewState {
        it.copy(currentScreen = currentScreen)
    }
}

fun MainScreenViewModel.changeSearchText(query: String) {

    updateViewState {
        it.copy(searchQuery = query)
    }
}

fun MainScreenViewModel.updateSearchState(searchLoadingState: SearchLoadingState) {
    updateViewState {
        it.copy(searchLoadingState = searchLoadingState)
    }
}

fun MainScreenViewModel.updateConnectionState(chatSyncRpcClient: ChatSyncRpcClient) {
    viewModelScope.launch {
        chatSyncRpcClient
            .connectionState
            .collect { state ->
                updateViewState {
                    it.copy(connectionState = state)
                }
            }
    }
}
