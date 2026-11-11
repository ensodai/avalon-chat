package app.plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headers
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.corsPlugin() {
    install(CORS) {
        // Локальная разработка
        allowHost("localhost:8080", schemes = listOf("http"))
        allowHost("localhost:8081", schemes = listOf("http"))
        allowHost("localhost:8082", schemes = listOf("http"))
        allowHost("localhost:8083", schemes = listOf("http"))
        allowHost("127.0.0.1:8080", schemes = listOf("http"))
        allowHost("127.0.0.1:8081", schemes = listOf("http"))
        allowHost("127.0.0.1:8082", schemes = listOf("http"))
        allowHost("127.0.0.1:8083", schemes = listOf("http"))
        
        // Разрешаем любые хосты/IP для удобства локальной разработки (включая динамические IP эмулятора)
        anyHost()

        // Методы
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)

        // Заголовки
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.CacheControl)
        allowHeader("X-Requested-With")
        allowHeader("X-Forwarded-For")
        allowHeader("Version-Name")
        allowHeader("Version-Code")
        allowHeader("X-Platform")

        // Важные настройки
        allowCredentials = true
        allowNonSimpleContentTypes = true
        maxAgeInSeconds = 3600

    }
}
