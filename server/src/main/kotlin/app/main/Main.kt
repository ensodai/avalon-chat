package app.main

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {

//    embeddedServer(
//        factory = Netty,
//        port = 8080,
//        host = "0.0.0.0",
//        module = Application::module
//    ).start(wait = true)

    val config = HoconApplicationConfig(ConfigFactory.load())
    val port = config.propertyOrNull("ktor.deployment.port")?.getString()?.toIntOrNull() ?: 8080
    val host = config.propertyOrNull("ktor.deployment.host")?.getString()?.takeIf { it.isNotEmpty() } ?: "0.0.0.0"
    embeddedServer(
        factory = Netty,
        environment = applicationEnvironment {
            this.config = config
        },
        configure = {
            connector {
                this.port = port
                this.host = host
            }
        },
//        module = Application::module
    ).start(wait = true)
}

