package com.ensodai.shared.avalon.chat.services

import com.ensodai.shared.avalon.chat.models.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

object MessageContentCodec {
    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "type"
    }

    inline fun <reified T : MessagePayload> decodeAs(contentString: String): T? {
        return try {
            when (T::class) {
                TextPayload::class -> TextPayload(text = contentString) as T
                ImagePayload::class -> json.decodeFromString<ImagePayload>(contentString) as T
                SystemEventPayload::class -> json.decodeFromString<SystemEventPayload>(contentString) as T
                StickerGiftPayload::class -> json.decodeFromString<StickerGiftPayload>(contentString) as T
                AudioPayload::class -> json.decodeFromString<AudioPayload>(contentString) as T
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun decode(type: AvalonMessageContentType, contentString: String): MessagePayload {
        return try {
            when (type) {
                AvalonMessageContentType.TEXT -> TextPayload(text = contentString)
                AvalonMessageContentType.IMAGE -> json.decodeFromString<ImagePayload>(contentString)
                AvalonMessageContentType.AUDIO -> json.decodeFromString<AudioPayload>(contentString)
                AvalonMessageContentType.SYSTEM_EVENT -> {
                    SystemEventPayload(
                        eventType = SystemEventType.valueOf(contentString),
                        additionalData = null
                    )
                }
                AvalonMessageContentType.STICKER_GIFT,
                AvalonMessageContentType.GIFT -> json.decodeFromString<StickerGiftPayload>(contentString)
                AvalonMessageContentType.GENERATED_IMAGE_DESCRIPTION,
                AvalonMessageContentType.MESSAGE_SYSTEM_INSTRUCTION -> TextPayload(text = contentString)
            }
        } catch (e: Exception) {
            TextPayload(text = "[Ошибка чтения контента: ${e.message}]")
        }
    }

    fun encode(type: AvalonMessageContentType, payload: MessagePayload): String {
        return when (type) {
            AvalonMessageContentType.TEXT -> (payload as TextPayload).text
            AvalonMessageContentType.IMAGE -> json.encodeToString(payload as ImagePayload)
            AvalonMessageContentType.AUDIO -> json.encodeToString(payload as AudioPayload)
            AvalonMessageContentType.SYSTEM_EVENT -> (payload as SystemEventPayload).eventType.name
            AvalonMessageContentType.STICKER_GIFT,
            AvalonMessageContentType.GIFT -> json.encodeToString(payload as StickerGiftPayload)
            AvalonMessageContentType.GENERATED_IMAGE_DESCRIPTION,
            AvalonMessageContentType.MESSAGE_SYSTEM_INSTRUCTION -> (payload as TextPayload).text
        }
    }
}
