package com.ensodai.avalon.utils

import kotlinx.browser.window

@JsFun("(value) => decodeURIComponent(value)")
private external fun decodeURIComponent(value: String): String
actual fun getGoogleAuthCode(): String? =
    window.location.search
        .trimStart('?')
        .split('&')
        .firstOrNull { it.startsWith("code=") }
        ?.removePrefix("code=")
        ?.let { decodeURIComponent(it) }

@OptIn(ExperimentalWasmJsInterop::class)
actual fun clearUrlCode() {
    window.history.replaceState(null, "", window.location.pathname)
}

actual fun getWebRedirectUri(): String =
    "${window.location.origin}/"
