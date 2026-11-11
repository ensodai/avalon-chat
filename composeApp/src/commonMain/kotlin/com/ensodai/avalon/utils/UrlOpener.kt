package com.ensodai.avalon.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler

interface UrlOpener {
    fun open(url: String)
}

@Composable
fun rememberUrlOpener(): UrlOpener {
    val uriHandler = LocalUriHandler.current
    return remember(uriHandler) {
        object : UrlOpener {
            override fun open(url: String) {
                runCatching {
                    uriHandler.openUri(url)
                }.onFailure { error ->
                    L.e("UrlOpener", "Failed to open URL: $url: ${error.stackTraceToString()}")
                }
            }
        }
    }
}
