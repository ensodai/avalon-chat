package app.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.logger() {
    install(CallLogging) {
        level = Level.INFO // Логировать на уровне INFO
        filter { call -> call.request.path().startsWith("/") }
        format { call ->
            val status = call.response.status()?.value ?: "No status"
            val method = call.request.httpMethod.value
            val path = call.request.path()
            val userAgent = call.request.headers["User-Agent"] ?: "Unknown"
            "$method $path $status - $userAgent"
        }
    }
}