package com.ensodai.avalon.presentation.commonComponents.snackbar

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class AppSnackbarController(
    val hostState: SnackbarHostState
) {
    suspend fun showMessage(
        message: String,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration = SnackbarDuration.Short
    ): SnackbarResult {
        return hostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            withDismissAction = withDismissAction,
            duration = duration
        )
    }
}

val LocalAppSnackbarController = compositionLocalOf<AppSnackbarController> {
    error("No AppSnackbarController provided")
}

@Composable
fun rememberAppSnackbarController(
    hostState: SnackbarHostState = remember { SnackbarHostState() }
): AppSnackbarController {
    return remember(hostState) {
        AppSnackbarController(hostState)
    }
}

@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    )
}
