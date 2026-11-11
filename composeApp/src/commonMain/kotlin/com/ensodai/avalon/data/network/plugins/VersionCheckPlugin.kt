package com.ensodai.avalon.data.network.plugins

import com.ensodai.avalon.domain.global.GlobalEvent
import com.ensodai.avalon.domain.global.GlobalEventBus
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Ktor Plugin to check for forced updates (HTTP 426).
 */
val VersionCheckPlugin = createClientPlugin("VersionCheckPlugin", ::VersionCheckConfig) {
    val eventBus = pluginConfig.eventBus
    val appScope = pluginConfig.appScope
    val json = pluginConfig.json

    onResponse { response ->
        if (response.status == HttpStatusCode.UpgradeRequired) {
            val errorBodyString = response.bodyAsText()

            val targetVersion = try {
                if (errorBodyString.isNotEmpty()) {
                    val jsonElement = json.parseToJsonElement(errorBodyString)
                    jsonElement.jsonObject["actualAppVersion"]?.jsonPrimitive?.content ?: "Unknown"
                } else {
                    "Unknown"
                }
            } catch (e: Exception) {
                "Unknown"
            }

            appScope.launch {
                eventBus.emit(GlobalEvent.ForceUpdateRequired(targetVersion = targetVersion))
            }
        }
    }
}

class VersionCheckConfig {
    lateinit var eventBus: GlobalEventBus
    lateinit var appScope: CoroutineScope
    lateinit var json: Json
}
