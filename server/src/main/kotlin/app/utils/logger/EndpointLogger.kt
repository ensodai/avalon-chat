package app.utils.logger

import io.github.oshai.kotlinlogging.KotlinLogging

object EndpointLogger {
    private val base = AppLogger(KotlinLogging.logger("HTTP"), "HTTP")

    fun request(path: String, method: String) =
        base.info("→ $method $path")

    fun response(path: String, status: Int) =
        when {
            status >= 500 -> base.fail("← $status $path")
            status >= 400 -> base.warn("← $status $path")
            else -> base.success("← $status $path")
        }
}
