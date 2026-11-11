package app.domain.chat.billing

import com.ensodai.shared.avalon.chat.models.SystemEventType

/**
 * Тип действия, которое требует биллинга/проверки перед выполнением.
 * Используется в [BillingRouter] для определения правил (например, нужна ли Image-модель).
 */
enum class GenerationActionType {
    /** Обычный текстовый ответ от ИИ */
    TEXT_REPLY,
    
    /** Генерация картинки (через подарок или другой триггер) */
    IMAGE_GIFT,
    
    /** Генерация голосового сообщения (TTS) */
    VOICE_MESSAGE
}

/**
 * Результат маршрутизации и биллинга.
 * Определяет, может ли действие быть выполнено, и за чей счет.
 */
sealed interface RouteResult {
    
    /**
     * Базовый интерфейс для успешной маршрутизации.
     * Означает, что фейс-контроль пройден и можно приступать к генерации.
     */
    sealed interface Success : RouteResult

    /**
     * Пользователь использует свой кастомный ключ (Bring Your Own Key).
     * Энергия сервера не списывается.
     *
     * @param apiKey API-ключ пользователя (например, OpenRouter)
     * @param baseUrl Базовый URL провайдера
     */
    data class FreeCustomKey(
        val apiKey: String,
        val baseUrl: String
    ) : Success

    /**
     * Пользователь использует ключи сервера.
     * За это действие была успешно списана энергия (или она бесплатна для сервера).
     *
     * @param cost Сколько энергии было списано
     */
    data class ServerEnergy(
        val cost: Int
    ) : Success

    /**
     * Ошибка маршрутизации. Действие должно быть прервано.
     * Содержит [SystemEventType], чтобы отправить клиенту понятное системное сообщение.
     */
    sealed class Error(val eventType: SystemEventType) : RouteResult {
        /** Закончилась энергия на сервере */
        object QuotaExceeded : Error(SystemEventType.QUOTA_EXCEEDED)
        
        /** Выбран кастомный провайдер, но не указаны модели для картинок */
        object NoImageModelConfigured : Error(SystemEventType.CUSTOM_MODEL_MISSING)
        
        /** Выбран кастомный провайдер, но ключ не указан */
        object CustomKeyMissing : Error(SystemEventType.CUSTOM_KEY_INVALID)
    }
}
