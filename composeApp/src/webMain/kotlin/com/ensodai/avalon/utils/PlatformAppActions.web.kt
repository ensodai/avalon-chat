package com.ensodai.avalon.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

import kotlinx.browser.window

private object WebPlatformAppActions : PlatformAppActions {
    override fun openAppSettings() = Unit

    override fun closeApp() = Unit

    override fun reloadApp() {
        window.location.reload()
    }
}

@Composable
actual fun rememberPlatformAppActions(): PlatformAppActions {
    return remember { WebPlatformAppActions }
}
