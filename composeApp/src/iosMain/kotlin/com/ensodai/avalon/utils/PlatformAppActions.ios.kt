package com.ensodai.avalon.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private object IosPlatformAppActions : PlatformAppActions {
    override fun openAppSettings() = Unit

    override fun closeApp() = Unit

    override fun reloadApp() = Unit
}

@Composable
actual fun rememberPlatformAppActions(): PlatformAppActions {
    return remember { IosPlatformAppActions }
}
