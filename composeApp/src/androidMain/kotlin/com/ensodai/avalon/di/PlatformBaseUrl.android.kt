package com.ensodai.avalon.di

import com.ensodai.avalon.config.AppBuildConfig

actual var platformBaseUrl: String = AppBuildConfig.BASE_URL

actual var platformSyncWebSocketBaseUrl: String = AppBuildConfig.SYNC_WS_URL

actual val platformSyncWebSocketPath: String = "/sync-v2"

actual var platformLiveCallWebSocketBaseUrl: String = AppBuildConfig.LIVE_CALL_WS_URL

actual val platformLiveCallWebSocketPath: String = "/call/live"
