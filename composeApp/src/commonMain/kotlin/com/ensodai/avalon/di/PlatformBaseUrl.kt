package com.ensodai.avalon.di

expect var platformBaseUrl: String

expect var platformSyncWebSocketBaseUrl: String

expect val platformSyncWebSocketPath: String

expect var platformLiveCallWebSocketBaseUrl: String

expect val platformLiveCallWebSocketPath: String

fun updateServerBaseUrl(newHttpUrl: String) {
    val cleanUrl = newHttpUrl.trim().trimEnd('/')
    if (cleanUrl.isBlank()) return
    platformBaseUrl = cleanUrl
    val wsScheme = if (cleanUrl.startsWith("https://")) "wss://" else "ws://"
    val hostAndPort = cleanUrl.removePrefix("https://").removePrefix("http://")
    platformSyncWebSocketBaseUrl = "$wsScheme$hostAndPort/"
    platformLiveCallWebSocketBaseUrl = "$wsScheme$hostAndPort/"
}
