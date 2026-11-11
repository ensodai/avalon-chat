package app.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.forwardedheaders.*

fun Application.xForwardedHeaders() {
    install(XForwardedHeaders)
}
