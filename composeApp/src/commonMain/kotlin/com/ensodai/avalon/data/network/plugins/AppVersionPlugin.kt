package com.ensodai.avalon.data.network.plugins

import com.ensodai.avalon.domain.model.PlatformMetadata
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.header

/**
 * Ktor Plugin to add version headers from [PlatformMetadata].
 */
val AppVersionPlugin = createClientPlugin("AppVersionPlugin", ::AppVersionConfig) {
    val metadata = pluginConfig.metadata

    onRequest { request, _ ->
        request.header("Version-Name", metadata.versionName)
        request.header("Version-Code", metadata.versionCode.toString())
        request.header("X-Platform", metadata.platform)
    }
}

class AppVersionConfig {
    lateinit var metadata: PlatformMetadata
}
