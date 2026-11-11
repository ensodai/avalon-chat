package app.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.defaultheaders.* 

fun Application.backendSecurityHeadersPlugin() {
    install(DefaultHeaders) {
        header("Cross-Origin-Resource-Policy", "cross-origin")
        header("Cross-Origin-Opener-Policy", "same-origin")
        header("Cross-Origin-Embedder-Policy", "require-corp")
        header("Vary", "Origin")
    }
}