package com.ensodai.avalon.presentation.chatScreen.component.bottomBar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.core.pagination.PaginatorState
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.shared.avalon.gifts.models.Gift
import com.ensodai.shared.avalon.gifts.models.GiftPack
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.cancel
import com.ensodai.avalon.generated.resources.save
import com.ensodai.avalon.presentation.chatScreen.action.ChatScreenActions
import com.ensodai.avalon.presentation.chatScreen.viewState.ActionButtonState
import com.ensodai.avalon.presentation.chatScreen.viewState.BottomBarState
import com.ensodai.avalon.presentation.chatScreen.viewState.ChatScreenViewState
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomBarChatScreen(
    modifier: Modifier,
    viewState: ChatScreenViewState,
    actions: ChatScreenActions,
    focusRequester: FocusRequester,
    focusManager: FocusManager,
    isGenerateResponseButtonEnable: Boolean,
    onClickFloatingPlusButton: (Boolean) -> Unit,
    myGiftsState: PaginatorState<GiftPack>,
    allGiftsState: PaginatorState<GiftPack>,
    onShowAiWarningClick: () -> Unit
) {
    BottomBarChatScreen(
        modifier = modifier,
        actionButtonState = viewState.actionButtonState,
        isLoadingFiles = viewState.isLoadingFiles,
        isGenerateResponseButtonEnable = isGenerateResponseButtonEnable,
        isEnableSearchMessageMode = viewState.chatScreenModeState.isEnableSearchMessageMode,
        searchMessageCount = viewState.searchMessages.size,
        selectedMessageNumber = viewState.selectedMessageNumber,
        focusRequester = focusRequester,
        focusManager = focusManager,
        myGiftsState = myGiftsState,
        allGiftsState = allGiftsState,
        onSendGift = { actions.messageInputActions.onSendGift(it) },
        onOpenGiftsMagazine = { actions.messageInputActions.onEnableGiftBottomSheetMode() },
        onChangeMessageText = { actions.messageInputActions.onChangeInputUserMessage(it) },
        onSendMessageClick = { actions.messageInputActions.onSendMessageClick(it) },
        onForceGenerateResponseClick = { actions.messageInputActions.onForceGenerateResponse() },
        onShowPermissionDialog = { actions.dialogActions.onShowPermissionDialog() },
        attachedImages = viewState.attachedImages,
        onStopGenerateResponse = { actions.messageInputActions.onStopGenerateMessage() },
        onAttachImages = { actions.messageInputActions.onAttachImages(it) },
        onDetachImages = { actions.messageInputActions.onDetachImages(it) },
        isEnableEditMessageMode = viewState.chatScreenModeState.isEnableEditMessageMode,
        onSaveEditedMessage = { actions.messageActions.onSaveEditedMessage() },
        onCancelEditMessage = {
            actions.messageActions.onDisableEditMessageMode()
            actions.messageActions.onDisableSelectionMessageMode()
        },
        bottomBarState = viewState.bottomBarState,
        onToggleRemarkMenu = { actions.messageInputActions.onToggleBottomSheetMenu() },
        onSetInstructionMode = { actions.messageInputActions.onSetInstructionMode(it) },
        onSetSystemInstruction = { actions.messageInputActions.onSetSystemInstruction(it) },
        onClearSystemInstruction = { actions.messageInputActions.onClearSystemInstruction() },
        onToggleFloatingPlusButton = onClickFloatingPlusButton,
        showAiWarning = viewState.showAiWarning,
        onShowAiWarningClick = onShowAiWarningClick
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalAnimationApi::class)
@Composable
private fun BottomBarChatScreen(
    modifier: Modifier,
    actionButtonState: ActionButtonState,
    attachedImages: List<CharacterImageDTO>,
    isLoadingFiles: Boolean,
    isEnableSearchMessageMode: Boolean,
    focusRequester: FocusRequester,
    focusManager: FocusManager,
    myGiftsState: PaginatorState<GiftPack>,
    allGiftsState: PaginatorState<GiftPack>,
    onOpenGiftsMagazine: () -> Unit,
    onSendMessageClick: (String) -> Unit,
    onForceGenerateResponseClick: () -> Unit,
    onShowPermissionDialog: () -> Unit,
    onChangeMessageText: (String) -> Unit,
    isGenerateResponseButtonEnable: Boolean,
    onStopGenerateResponse: () -> Unit,
    searchMessageCount: Int,
    selectedMessageNumber: Int,
    onSendGift: (Gift) -> Unit,
    onAttachImages: (List<CharacterImageDTO>) -> Unit,
    onDetachImages: (CharacterImageDTO) -> Unit,
    isEnableEditMessageMode: Boolean,
    onSaveEditedMessage: () -> Unit,
    onCancelEditMessage: () -> Unit,
    bottomBarState: BottomBarState,
    onToggleRemarkMenu: () -> Unit,
    onSetInstructionMode: (Boolean) -> Unit,
    onSetSystemInstruction: (String) -> Unit,
    onClearSystemInstruction: () -> Unit,
    onToggleFloatingPlusButton: (Boolean) -> Unit,
    showAiWarning: Boolean,
    onShowAiWarningClick: () -> Unit
) {

    AnimatedContent(targetState = isEnableEditMessageMode, label = "BottomBarMode") { isEditMode ->
        if (isEditMode) {
            EditMessageButtons(
                onSave = onSaveEditedMessage,
                onCancel = onCancelEditMessage
            )
        } else {
            TextFieldSection(
                modifier = modifier,
                actionButtonState = actionButtonState,
                isEnableSearchMessageMode = isEnableSearchMessageMode,
                isLoadingFiles = isLoadingFiles,
                attachedImages = attachedImages,
                bottomBarState = bottomBarState,
                searchMessageCount = searchMessageCount,
                selectedMessageNumber = selectedMessageNumber,
                focusRequester = focusRequester,
                focusManager = focusManager,
                myGiftsState = myGiftsState,
                allGiftsState = allGiftsState,
                onSendGift = { onSendGift(it) },
                onOpenGiftsMagazine = { onOpenGiftsMagazine() },
                onSendMessageClick = { onSendMessageClick(it) },
                onForceGenerateResponseClick = { onForceGenerateResponseClick() },
                onShowPermissionDialog = { onShowPermissionDialog() },
                onChangeMessageText = { onChangeMessageText(it) },
                isGenerateResponseButtonEnable = isGenerateResponseButtonEnable,
                onStopGenerateResponse = { onStopGenerateResponse() },
                onAttachImages = { onAttachImages(it) },
                onDetachImages = { onDetachImages(it) },
                onToggleBottomBarMenu = onToggleRemarkMenu,
                onSetInstructionMode = onSetInstructionMode,
                onSetMessageSystemInstruction = onSetSystemInstruction,
                onClearSystemInstruction = onClearSystemInstruction,
                onToggleFloatingPlusButton = onToggleFloatingPlusButton,
                showAiWarning = showAiWarning,
                onShowAiWarningClick = onShowAiWarningClick
            )
        }
    }
}

@Composable
private fun EditMessageButtons(
    onSave: () -> Unit,
    onCancel: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding()
            .imePadding()
            .background(Color.Transparent),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TechnoButton(
            text = stringResource(Res.string.cancel),
            onClick = onCancel,
            isPrimary = false,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        TechnoButton(
            text = stringResource(Res.string.save),
            onClick = onSave,
            isPrimary = true,
            modifier = Modifier.weight(1f)
        )
    }

}
