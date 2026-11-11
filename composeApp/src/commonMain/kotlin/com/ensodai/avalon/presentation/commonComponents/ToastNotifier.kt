package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.ensodai.avalon.presentation.commonComponents.snackbar.LocalAppSnackbarController
import kotlinx.coroutines.launch


@Composable
fun toastNotify(): (message: String) -> Unit {
    val snackbarController = LocalAppSnackbarController.current
    val scope = rememberCoroutineScope()

    return remember(snackbarController, scope) {
        { message ->
            scope.launch {
                snackbarController.showMessage(message)
            }
        }
    }
}
