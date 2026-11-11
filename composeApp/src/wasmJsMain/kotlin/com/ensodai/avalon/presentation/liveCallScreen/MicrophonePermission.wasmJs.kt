package com.ensodai.avalon.presentation.liveCallScreen

import androidx.compose.runtime.Composable

@Composable
actual fun rememberMicrophonePermissionLauncher(
    onGranted: () -> Unit,
    onDenied: () -> Unit
): () -> Unit {
    return onGranted
}
