package app.utils

import io.klogging.Klogger
import io.klogging.logger

val customLogger: Klogger = logger<CustomLogger>()
class CustomLogger {
    private val logger: Klogger = logger<CustomLogger>()

    suspend fun doSomething() {
        logger.info("Работает!")
        logger.debug("Дебаг сообщение")
    }
}