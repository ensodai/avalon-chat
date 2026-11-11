package com.ensodai.avalon.domain.chatEvents

import com.ensodai.avalon.data.network.interceptors.InstantSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

val ApplicationJson = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
    classDiscriminator = "type"
    serializersModule = SerializersModule {
//        contextual(UUIDSerializer)
        contextual(InstantSerializer)
    }
}