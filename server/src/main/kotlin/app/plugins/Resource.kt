package app.plugins

import io.ktor.server.application.*
import io.ktor.server.resources.*

fun Application.configureResource() {
    install(Resources)
}
