package com.ensodai.avalon.presentation.liveCallScreen

import androidx.compose.runtime.Composable

@Composable
actual fun rememberMicrophonePermissionLauncher(
    onGranted: () -> Unit,
    onDenied: () -> Unit
): () -> Unit {
    // iOS handles permissions natively when audio session is created
    return onGranted
}
