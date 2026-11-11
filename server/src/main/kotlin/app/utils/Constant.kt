package app.utils

const val MAX_QUOTA_MESSAGE_PER_HOUR = 10
const val API_KEY_COOLDOWN_HOURS = 1
const val ACCESS_TOKEN_LIVE = 15 * 60 * 1000 // 15 минут
const val REFRESH_TOKEN_LIVE = 30L * 24 * 60 * 60 * 1000 // 30 дней
const val REFRESH_TOKEN_LIVE_DAYS = 30L // 30 дней

const val MINUTES_1 = 1 * 60 * 1000
const val MINUTES_2 = 2 * 60 * 1000

// ==========================================
// 🌐 Network & Endpoint Defaults
// ==========================================
/**
 * Базовый URL сервера по умолчанию для формирования абсолютных ссылок на загруженные медиа (uploads)
 */
val DEFAULT_SERVER_BASE_URL: String 
    get() = EnvProvider.getServerBaseUrl() ?: "http://localhost:8080"

/**
 * Домен веб-клиента для CORS
 */
const val DEFAULT_WEB_CORS_DOMAIN = "localhost"
