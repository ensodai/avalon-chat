package com.ensodai.avalon.pwa

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface AppInstaller {
    val canBeInstalled: StateFlow<Boolean>
    val showIosInstructions: MutableStateFlow<Boolean>
    val isInstallDismissed: StateFlow<Boolean>

    fun installApp()
    fun dismissInstall()
}

val LocalAppInstaller = staticCompositionLocalOf<AppInstaller?> { null }
