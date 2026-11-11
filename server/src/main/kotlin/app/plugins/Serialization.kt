package app.plugins

import app.data.network.InstantSerializer
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

val ApplicationJson = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
    classDiscriminator = "type"
    encodeDefaults = true
    serializersModule = SerializersModule {
        contextual(InstantSerializer)
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(ApplicationJson)
    }
}
