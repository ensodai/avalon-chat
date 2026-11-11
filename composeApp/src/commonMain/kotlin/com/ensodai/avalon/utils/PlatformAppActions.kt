package com.ensodai.avalon.utils

import androidx.compose.runtime.Composable

interface PlatformAppActions {
    fun openAppSettings()
    fun closeApp()
    fun reloadApp()
}

@Composable
expect fun rememberPlatformAppActions(): PlatformAppActions
