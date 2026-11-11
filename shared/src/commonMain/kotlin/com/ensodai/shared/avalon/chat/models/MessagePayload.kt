package com.ensodai.shared.avalon.chat.models

import com.ensodai.shared.avalon.gifts.models.GiftActionType
import com.ensodai.shared.avalon.gifts.models.GiftEffectType
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
enum class SystemEventType {
    QUOTA_EXCEEDED,                 // Закончилась серверная квота/энергия
    CUSTOM_MODEL_MISSING,           // Не указана модель в настройках юзера
    CUSTOM_KEY_INVALID,             // 401: Неверный или удаленный API ключ
    CUSTOM_INSUFFICIENT_CREDITS,    // 402: На балансе OpenRouter/провайдера кончились средства
    CUSTOM_RATE_LIMIT,              // 429: Превышен лимит запросов к модели (RPM/TPM)
    CUSTOM_MODEL_NOT_FOUND,         // 404: Модель не найдена (опечатка в названии модели)
    CUSTOM_CONTEXT_LENGTH_EXCEEDED, // 400: История чата и промпт превысили контекстное окно модели
    CUSTOM_GUARDRAILS_RESTRICTED,   // 400: Блокировка настройками приватности / Guardrails OpenRouter
    CUSTOM_PROVIDER_UNAVAILABLE,    // 502/503: Апстрим-провайдер модели временно недоступен или перегружен
    MESSAGE_BLOCKED,                // Цензура/фильтр безопасности текста
    IMAGE_PERSON_BLOCKED,           // Попытка сгенерировать реального человека
    IMAGE_SAFETY_BLOCKED,           // Блокировка фильтром безопасности изображения
    SERVER_ERROR,                   // Внутренняя ошибка бэкенда
    USER_BANNED                     // Юзер заблокирован
}

@Serializable
sealed interface MessagePayload

@Serializable
data class TextPayload(
    val text: String
) : MessagePayload

@Serializable
data class ImagePayload(
    val id: Uuid,
    val url: String,
    val width: Int,
    val height: Int,
    val blurhash: String? = null,
    val sizeBytes: Long? = null
) : MessagePayload

@Serializable
data class SystemEventPayload(
    val eventType: SystemEventType,
    val additionalData: String? = null // Например, причина бана или размер превышенной квоты
) : MessagePayload

@Serializable
data class StickerGiftPayload(
    val giftId: Uuid,
    val packId: Uuid,
    val giftName: String,
    val url: String,
    val actionType: GiftActionType,
    val effectType: GiftEffectType = GiftEffectType.NONE
) : MessagePayload

@Serializable
data class AudioPayload(
    val id: Uuid,
    val url: String,
    val durationMs: Long? = null,
    val sizeBytes: Long? = null,
    val mimeType: String? = null
) : MessagePayload
