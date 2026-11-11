package app.plugins

import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun Application.configureWebSockets() {
    install(WebSockets) {
        pingPeriod = 15.toDuration(DurationUnit.SECONDS)
        timeout = 30.toDuration(DurationUnit.SECONDS)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(ApplicationJson)
    }
}
