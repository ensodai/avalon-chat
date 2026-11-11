package com.ensodai.avalon.presentation.liveCallScreen

import androidx.compose.runtime.Composable

/**
 * Запрос разрешения на запись аудио.
 * Возвращает лямбду для запуска запроса.
 */
@Composable
expect fun rememberMicrophonePermissionLauncher(
    onGranted: () -> Unit,
    onDenied: () -> Unit
): () -> Unit
