package com.ensodai.avalon.utils

import com.ensodai.avalon.config.AppBuildConfig
import kotlinx.browser.window

actual fun isAppInDebugMode(): Boolean {
    if (!AppBuildConfig.IS_DEBUG) return false
    val hostname = window.location.hostname
    return hostname == "localhost" || hostname == "127.0.0.1" || hostname.startsWith("192.168.")
}
