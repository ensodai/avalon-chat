package com.ensodai.avalon.di

import kotlinx.browser.window

actual var platformBaseUrl: String = ""
actual var platformSyncWebSocketBaseUrl: String = buildString {
    append(if (window.location.protocol == "https:") "wss://" else "ws://")
    append(window.location.host)
}
actual val platformSyncWebSocketPath: String = "/sync-v2"

actual var platformLiveCallWebSocketBaseUrl: String = buildString {
    append(if (window.location.protocol == "https:") "wss://" else "ws://")
    append(window.location.host)
}
actual val platformLiveCallWebSocketPath: String = "/call/live"
