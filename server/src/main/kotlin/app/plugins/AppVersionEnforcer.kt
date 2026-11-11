package app.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

class AppVersionConfig {
    var minVersionCode: Long = 79
    var actualAppVersion: String = "4.9.5"
    var pathPrefix: String = "/api/"
}

@Serializable
data class AppVersionErrorResponse(
    val error: String,
    val minVersion: Long,
    val yourVersion: Long,
    val updateUrl: String,
    val actualAppVersion: String
)

fun Application.appVersionEnforcer() {
    install(AppVersionEnforcer) {
        minVersionCode = 79
        actualAppVersion = "4.9.5"
    }
}

val AppVersionEnforcer = createApplicationPlugin(
    name = "AppVersionEnforcer",
    createConfiguration = ::AppVersionConfig
) {

    val minVersion = pluginConfig.minVersionCode
    val actualAppVersion = pluginConfig.actualAppVersion
    val targetPathPrefix = pluginConfig.pathPrefix

    onCall { call ->
        val path = call.request.path()

        if (!path.startsWith(targetPathPrefix)) {
            return@onCall
        }

        if (call.request.httpMethod == HttpMethod.Options) {
            return@onCall
        }

        val versionCodeHeader = call.request.header("Version-Code")
        val clientVersion = versionCodeHeader?.toLongOrNull()

        if (clientVersion == null || clientVersion < minVersion) {
            call.respond(
                status = HttpStatusCode.UpgradeRequired, // 426
                message = AppVersionErrorResponse(
                    error = "App version is outdated",
                    minVersion = minVersion,
                    yourVersion = clientVersion ?: 0L,
                    updateUrl = "https://play.google.com/store/apps/details?id=...",
                    actualAppVersion = actualAppVersion
                )
            )
            return@onCall // Важно прервать выполнение
        }
    }
}
