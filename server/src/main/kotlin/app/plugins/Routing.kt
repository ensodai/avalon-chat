package app.plugins

import app.apiv4.routes.uploadRoute
import app.apiv4.routes.apiV4Route
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import io.ktor.server.http.content.*
import java.io.File

fun Application.configureRouting() {
    routing {
        // Раздача статических файлов (картинок)
        val uploadsDir = File("/var/uploads")
        val finalUploadsDir = if (uploadsDir.exists()) uploadsDir else File("uploads")
        staticFiles("/uploads", finalUploadsDir)

        uploadRoute()
        apiV4Route()
    }
}

fun Route.testRoute123() {
    get("/hello12345") {
        call.respondText("IT WORKS")
    }
}
