package com.ensodai.avalon.presentation.chatScreen.alertDialogs

import androidx.compose.runtime.Composable
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.delete_all_message_history
import com.ensodai.avalon.generated.resources.delete_chat_and_all_messages
import com.ensodai.avalon.generated.resources.save_changes
import com.ensodai.avalon.presentation.chatScreen.action.ChatScreenActions
import com.ensodai.avalon.presentation.chatScreen.viewState.ChatScreenViewState
import com.ensodai.avalon.presentation.commonComponents.alertDialogs.AlertDialogComplaint
import com.ensodai.avalon.presentation.commonComponents.alertDialogs.AlertDialogDeleteItem
import com.ensodai.avalon.presentation.commonComponents.alertDialogs.AlertDialogPermissionImage
import com.ensodai.avalon.presentation.navigation.Navigation
import org.jetbrains.compose.resources.stringResource

@Composable
fun AlertDialogsChatScreen(
    viewState: ChatScreenViewState,
    actions: ChatScreenActions,
    navigation: Navigation
) {

    AlertDialogDeleteItem(
        title = stringResource(Res.string.delete_all_message_history),
        onDismiss = { actions.dialogActions.onCloseAlertDialog() },
        onConfirm = { actions.chatManagementActions.onDeleteAllChatMessages() },
        isShowDialog = viewState.isShowAlertDialogClearHistory
    )

    AlertDialogDeleteItem(
        title = stringResource(Res.string.delete_chat_and_all_messages),
        onDismiss = { actions.dialogActions.onCloseAlertDialog() },
        onConfirm = {
            actions.chatManagementActions.onDeleteChatConfigWithMessages()
            navigation.navigateBack()
        },
        isShowDialog = viewState.isShowAlertDialogDeleteChatConfigWithMessages
    )

//    AlertDialogDeleteItem(
//        title = stringResource(Res.string.save_changes),
//        onDismiss = {
//            actions.messageActions.onDisableEditMessageMode()
//            actions.messageActions.onDisableSelectionMessageMode()
//            actions.dialogActions.onCloseAlertDialog()
//        },
//        onConfirm = { actions.messageActions.onSaveEditedMessage() },
//        isShowDialog = viewState.isShowAlertDialogSaveEditedMessage,
//        reversedButtonColor = true
//    )

    AlertDialogPermissionImage(
        onCloseButtonClick = { actions.dialogActions.onCloseAlertDialog() },
        isShowPermissionDialog = viewState.isShowPermissionDialog
    )

    AlertDialogComplaint(
        isShow = viewState.reportCharacterState.isShowReportDialog,
        text = viewState.reportCharacterState.reportMessage,
        onChangeText = { actions.dialogActions.onChangeReportMessage(it) },
        onDismiss = { actions.dialogActions.onCloseAlertDialog() },
        onConfirm = { actions.dialogActions.onCloseAlertDialog() }
    )
}