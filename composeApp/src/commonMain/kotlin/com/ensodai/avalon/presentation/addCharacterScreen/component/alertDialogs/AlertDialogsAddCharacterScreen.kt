package com.ensodai.avalon.presentation.addCharacterScreen.component.alertDialogs

import androidx.compose.runtime.Composable
import com.ensodai.avalon.presentation.commonComponents.alertDialogs.AlertDialogPermissionImage
import com.ensodai.avalon.presentation.commonComponents.alertDialogs.BaseErrorAlertDialog

@Composable
fun AlertDialogsAddCharacterScreen(
    isShowErrorDialog: Boolean,
    isShowPermissionDialog: Boolean,
    errorMessage: String?,
    onCloseAlertDialog: () -> Unit,
    onClosePermissionAlertDialog: () -> Unit
) {

    AlertDialogPermissionImage(
        isShowPermissionDialog = isShowPermissionDialog,
        onCloseButtonClick = { onClosePermissionAlertDialog() }
    )

    if (isShowErrorDialog) {
        BaseErrorAlertDialog(
            text = errorMessage,
            onDismiss = { onCloseAlertDialog() },
            onConfirm = { onCloseAlertDialog() }
        )
    }
}
