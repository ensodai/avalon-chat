package app.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.seconds

fun Application.rateLimit() {
    install(RateLimit) {
        global {
            rateLimiter(
                limit = 60,
                refillPeriod = 1.seconds
            )

            requestKey { call ->
                call.request.origin.remoteHost  // ключ = IP-адрес
            }
        }
    }
}