package com.ensodai.avalon.data.network.plugins

import com.ensodai.avalon.domain.sessionWorker.ServerTimeManager
import io.ktor.client.plugins.api.createClientPlugin

/**
 * Ktor Plugin to capture server time from response headers.
 */
val ServerTimePlugin = createClientPlugin("ServerTimePlugin", ::ServerTimeConfig) {
    val manager = pluginConfig.serverTimeManager

    onResponse { response ->
        val dateHeader = response.headers["Date"]
        manager.updateServerTime(dateHeader)
    }
}

class ServerTimeConfig {
    lateinit var serverTimeManager: ServerTimeManager
}
