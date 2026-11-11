package com.ensodai.avalon.presentation.chatScreen.component.topBar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.presentation.chatScreen.action.ChatScreenActions
import com.ensodai.avalon.presentation.chatScreen.viewState.ChatScreenViewState
import com.ensodai.avalon.presentation.chatScreen.viewState.characterHasAvatars
import com.ensodai.avalon.presentation.commonComponents.AnimateCloseButtonAndCounter
import com.ensodai.avalon.presentation.commonComponents.BaseItemScaleAnimation
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import com.ensodai.avalon.presentation.core.ChatStatus
import com.ensodai.avalon.presentation.navigation.Navigation
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Suppress("DEPRECATION")
@Composable
fun TopBarChatScreen(
    viewState: ChatScreenViewState,
    actions: ChatScreenActions,
    modifier: Modifier,
    navigation: Navigation,
    isRefreshButtonVisible: Boolean,
    focusRequester: FocusRequester,
    focusManager: FocusManager,
    onOpenAllChatVariantsScreen: () -> Unit,
    characterId: Uuid?
) {
    val clipboardManager = LocalClipboardManager.current

    TopBarChatScreen(
        modifier = modifier,
        chatName = viewState.characterName,
        avatarSeed = viewState.characterId.toString() + viewState.characterName,
        chatImage = viewState.avatarUrl,
        countSelectMessage = viewState.selectedMessages.size,
        currentContextTokens = viewState.currentContextTokens,
        inputSearchMessage = viewState.inputSearchMessage,
        onlineStatus = viewState.onlineStatus,
        isEnableCounter = viewState.isEnableCounter,
        chatStatus = viewState.chatStatus,
        isEnableChatInfoRow = viewState.isEnableChatInfoRow,
        isEnableBackButton = viewState.isEnableBackButton,
        isEnableSearchMessageMode = viewState.chatScreenModeState.isEnableSearchMessageMode,
        isEnableSelectedMessagesMode = viewState.chatScreenModeState.isEnableSelectedMessagesMode,
        isEnableEditMessageMode = viewState.chatScreenModeState.isEnableEditMessageMode,
        isEnableRefreshButton = isRefreshButtonVisible && !viewState.chatScreenModeState.isEnableEditMessageMode,
        isEnableCopyButton = viewState.isEnableCopyButton,
        isEnableEditButton = viewState.isEnableEditButton,
        isEnableDeleteButton = viewState.isEnableDeleteButton,
        isEnableDropdownMenuButton = viewState.isEnableDropdownMenuButton,
        focusRequester = focusRequester,
        onSelectedLlmConfig = { actions.messageVariantActions.onSelectActiveLlmConfig(it) },
        selectedLlmConfig = viewState.selectedConfig,
        availableLlmConfigs = viewState.availableLlmConfigs,
        onSearchMessageClick = { actions.searchActions.onSearchMessage() },
        onBackButtonClick = {
            if (viewState.chatScreenModeState.isEnableSearchMessageMode) {
                actions.searchActions.onDisableSearchMessageMode()
            } else {
                navigation.navigateBack()
            }
        },
        onDisableSelectedMode = { actions.messageActions.onDisableSelectionMessageMode() },
        onDisableEditMode = { actions.messageActions.onDisableEditMessageMode() },
        onEnableEditMessageMode = { actions.messageActions.onEnableEditMessageMode() },
        onSaveEditedMessage = { actions.messageActions.onSaveEditedMessage() },
        onDeleteSelectedMessage = { actions.messageActions.onDeleteSelectedMessage() },
        onCopyMessage = {
            if (viewState.selectedMessageText != null) {
                clipboardManager
                    .setText(AnnotatedString(viewState.selectedMessageText))
                actions.messageActions.onDisableSelectionMessageMode()
            }
        },
        onRefreshGeneratedMessage = { actions.messageInputActions.onRefreshGeneratedMessage() },
        onEnableSearchMessageMode = { actions.searchActions.onEnableSearchMessageMode() },
        onChangeInputSearchMessage = { actions.searchActions.onChangeInputSearchMessage(it) },
        onOpenChatProfileScreen = {
            if (viewState.characterId != null) {
                focusManager.clearFocus()
                navigation.navigateToCharacterProfileScreen(
                    characterId = viewState.characterId,
                    fullScreenImage = false,
                    characterHasAvatars = viewState.characterHasAvatars
                )
            }
        },
        onOpenChatProfileScreenFullImage = {
            if (viewState.characterId != null) {
                focusManager.clearFocus()
                navigation.navigateToCharacterProfileScreen(
                    characterId = viewState.characterId,
                    fullScreenImage = true,
                    characterHasAvatars = viewState.characterHasAvatars
                )
            }
        },
        onCreateNewChatVariant = { actions.messageVariantActions.onCreateNewChatVariant() },
        onOpenAllChatVariantsScreen = {
            onOpenAllChatVariantsScreen()
        },
        characterId = characterId,
        onOpenLiveCallScreen = {
            if (characterId != null) {
                focusManager.clearFocus()
                navigation.navigateToLiveCallScreen(characterId = characterId)
            }
        }
    )
}

@Composable
private fun TopBarChatScreen(
    modifier: Modifier,
    chatName: String,
    avatarSeed: String,
    chatImage: String?,
    countSelectMessage: Int,
    currentContextTokens: Int,
    inputSearchMessage: String,
    onlineStatus: Boolean,
    isEnableCounter: Boolean,
    isEnableCopyButton: Boolean,
    isEnableEditButton: Boolean,
    isEnableDeleteButton: Boolean,
    chatStatus: ChatStatus,
    isEnableChatInfoRow: Boolean,
    isEnableBackButton: Boolean,
    isEnableSearchMessageMode: Boolean,
    isEnableDropdownMenuButton: Boolean,
    isEnableSelectedMessagesMode: Boolean,
    isEnableRefreshButton: Boolean,
    isEnableEditMessageMode: Boolean,
    focusRequester: FocusRequester,
    availableLlmConfigs: List<AvailableLlmConfigs>,
    selectedLlmConfig: AvailableLlmConfigs,
    onSelectedLlmConfig: (AvailableLlmConfigs) -> Unit,
    onCopyMessage: () -> Unit,
    onSearchMessageClick: () -> Unit,
    onBackButtonClick: () -> Unit,
    onDisableEditMode: () -> Unit,
    onSaveEditedMessage: () -> Unit,
    onDisableSelectedMode: () -> Unit,
    onDeleteSelectedMessage: () -> Unit,
    onEnableEditMessageMode: () -> Unit,
    onRefreshGeneratedMessage: () -> Unit,
    onEnableSearchMessageMode: () -> Unit,
    onChangeInputSearchMessage: (String) -> Unit,
    onOpenChatProfileScreen: () -> Unit,
    onOpenChatProfileScreenFullImage: () -> Unit,
    onCreateNewChatVariant: () -> Unit,
    onOpenAllChatVariantsScreen: () -> Unit,
    characterId: Uuid?,
    onOpenLiveCallScreen: () -> Unit
) {
    GemIslandTopBar(
        modifier = modifier,
        title = {
            ChatInfoRow(
                chatName = chatName,
                avatarSeed = avatarSeed,
                chatImage = chatImage,
                inputSearchMessage = inputSearchMessage,
                isEnableChatInfoRow = isEnableChatInfoRow,
                chatStatus = chatStatus,
                isEnableSearchMessageMode = isEnableSearchMessageMode,
                focusRequester = focusRequester,
                isEnableEditMessageMode = isEnableEditMessageMode,
                onSearchClick = { onSearchMessageClick() },
                onEnableSearchMessageMode = { onEnableSearchMessageMode() },
                onChangeInputSearchMessage = { onChangeInputSearchMessage(it) },
                onOpenChatProfileScreen = { onOpenChatProfileScreen() },
                onOpenChatProfileScreenFullImage = { onOpenChatProfileScreenFullImage() }
            )
        },
        navigationIcon = if (isEnableSelectedMessagesMode && !isEnableEditMessageMode) {
            {
                AnimateCloseButtonAndCounter(
                    counter = countSelectMessage,
                    isEnable = true,
                    isEnableCounter = isEnableCounter,
                    onCloseClick = {
                        onDisableSelectedMode()
                        onDisableEditMode()
                    }
                )
            }
        } else if (isEnableBackButton) {
            {
                NavigationBackButton(
                    onBackButtonClick = { onBackButtonClick() }
                )
            }
        } else {
            null
        },
        actions = if ((isEnableSelectedMessagesMode && !isEnableEditMessageMode) || isEnableDropdownMenuButton) {
            {
                ActionButtonsRowChatScreen(
                    isEnableCopyButton = isEnableCopyButton && isEnableSelectedMessagesMode && !isEnableEditMessageMode,
                    isEnableSaveEditedMessageButton = false,
                    isEnableRefreshButton = isEnableRefreshButton && isEnableSelectedMessagesMode && !isEnableEditMessageMode,
                    isEnableEditButton = isEnableEditButton && isEnableSelectedMessagesMode && !isEnableEditMessageMode,
                    isEnableDeleteButton = isEnableDeleteButton && isEnableSelectedMessagesMode && !isEnableEditMessageMode,
                    isEnableDropdownMenuButton = isEnableDropdownMenuButton,
                    onCopyMessage = { onCopyMessage() },
                    onRefreshGenerateMessage = { onRefreshGeneratedMessage() },
                    onEnableEditMessageMode = { onEnableEditMessageMode() },
                    onDeleteSelectedMessage = { onDeleteSelectedMessage() },
                    onSaveEditMessage = { onSaveEditedMessage() },
                    onCreateNewChatVariant = { onCreateNewChatVariant() },
                    onOpenAllChatVariantsScreen = { onOpenAllChatVariantsScreen() },
                    onOpenLiveCallScreen = { onOpenLiveCallScreen() },
                    onEnableSearchMessageMode = { onEnableSearchMessageMode() }
                )
            }
        } else {
            null
        },
        actionsCircular = !isEnableSelectedMessagesMode,
        titleHeight = 44.dp,
        titleContentPadding = PaddingValues(start = 1.dp, end = 1.dp, top = 1.dp, bottom = 1.dp),
        isTitleVisible = isEnableChatInfoRow || isEnableEditMessageMode
    )
}
