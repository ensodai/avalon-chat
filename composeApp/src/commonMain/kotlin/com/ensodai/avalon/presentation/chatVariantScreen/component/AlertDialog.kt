package com.ensodai.avalon.presentation.chatVariantScreen.component

import androidx.compose.runtime.Composable
import com.ensodai.avalon.presentation.chatVariantScreen.action.AlertDialogActions
import com.ensodai.avalon.presentation.chatVariantScreen.viewState.AlertDialog
import com.ensodai.avalon.presentation.commonComponents.alertDialogs.AlertDialogPermissionImage

@Composable
fun AlertDialog(state: AlertDialog, actions: AlertDialogActions) {

    when (state) {
        AlertDialog.Closed -> {}
        AlertDialog.PermissionDialog -> {
            AlertDialogPermissionImage(
                onCloseButtonClick = { actions.onCloseDialog() },
                isShowPermissionDialog = true
            )
        }
    }
}
