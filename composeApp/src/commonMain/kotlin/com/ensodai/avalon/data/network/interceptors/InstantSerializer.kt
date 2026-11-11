package com.ensodai.avalon.data.network.interceptors

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

/**
 * Кастомный сериализатор для kotlinx.serialization, который умеет
 * преобразовывать строки формата ISO 8601 в объекты Instant и обратно.
 */
object InstantSerializer : KSerializer<Instant> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        val rawString = decoder.decodeString()

        // МЕНЯЕМ ЗДЕСЬ: добавляем проверку на старые "короткие" даты
        val formattedString = if (rawString.length == 17 && rawString.endsWith("Z")) {
            rawString.replace("Z", ":00Z")
        } else {
            rawString
        }

        return Instant.parse(formattedString)
    }
}