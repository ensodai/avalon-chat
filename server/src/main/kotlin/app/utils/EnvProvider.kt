package app.utils

import io.github.cdimascio.dotenv.dotenv
import java.io.File
import java.util.*

object EnvProvider {

    private val dotenv = dotenv {
        ignoreIfMissing = true
    }

    private fun get(key: String): String? {
        return dotenv[key] ?: System.getenv(key)
    }

    fun getGeminiApiKey(): String {
        return get("SPECIAL_GEMINI_KEY") ?: error("SPECIAL_GEMINI_KEY is missing")
    }

    fun getVertexServiceAccountJSON(): String {
        // Читаем переменную с новым именем (или fallback, если вдруг переименуешь)
        val rawBase64 = get("GOOGLE_VERTEX_JSON_B64")
            ?: get("GOOGLE_VERTEX_JSON") // на всякий случай fallback
            ?: error("GOOGLE_VERTEX_JSON_B64 is missing")

        return try {
            // Очищаем от лишнего и декодируем base64
            val cleanedBase64 = rawBase64.trim().removeSurrounding("'").removeSurrounding("\"")
            String(Base64.getDecoder().decode(cleanedBase64), Charsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            // Если локально запустил с обычным JSON вместо base64
            if (rawBase64.trim().startsWith("{")) {
                rawBase64
            } else {
                error("GOOGLE_VERTEX_JSON_B64 is invalid")
            }
        }
    }

    fun getFirebaseServiceAccountJSON(): String {
        val rawBase64 = get("FIREBASE_SERVICE_ACCOUNT_JSON_B64")
            ?: get("FIREBASE_SERVICE_ACCOUNT_JSON")
            ?: error("FIREBASE_SERVICE_ACCOUNT_JSON_B64 is missing")

        return try {
            val cleanedBase64 = rawBase64.trim().removeSurrounding("'").removeSurrounding("\"")
            String(Base64.getDecoder().decode(cleanedBase64), Charsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            if (rawBase64.trim().startsWith("{")) {
                rawBase64
            } else {
                error("FIREBASE_SERVICE_ACCOUNT_JSON_B64 is invalid")
            }
        }
    }

    fun getUSProxyHost(): String {
        return get("US_PROXY_HOST")?.trim() ?: ""
    }

    fun getUSProxyPort(): Int {
        return get("US_PROXY_PORT")?.trim()?.toIntOrNull() ?: 3128
    }

    fun getUSProxyUser(): String {
        return get("US_PROXY_USER")?.trim() ?: ""
    }

    fun getUSProxyPass(): String {
        return get("US_PROXY_PASS")?.trim() ?: ""
    }

    // --- DB CONFIG ---
    fun getDbHost(): String? = get("DB_HOST")
    fun getDbPort(): String? = get("DB_PORT")
    fun getDbName(): String? = get("DB_NAME")
    fun getDbUser(): String? = get("DB_USER")
    fun getDbPassword(): String? = get("DB_PASSWORD")

    // --- TELEGRAM ---
    fun getTelegramBotToken(): String {
        return get("TELEGRAM_BOT_TOKEN") ?: ""
    }

    fun getTelegramAdminId(): Long {
        return get("TELEGRAM_ADMIN_ID")?.toLongOrNull() ?: 0L
    }

    fun getGoogleClientId(): String {
        return get("GOOGLE_CLIENT_ID") ?: ""
    }

    fun getGoogleAndroidClientId(): String {
        return get("GOOGLE_ANDROID_CLIENT_ID") ?: ""
    }

    fun getGoogleWebClientSecret(): String {
        return get("GOOGLE_WEB_CLIENT_SECRET") ?: ""
    }

    private var cachedJwtSecret: String? = null

    fun getJwtSecret(): String {
        cachedJwtSecret?.let { return it }

        val fromEnv = get("JWT_SECRET")?.trim()
        if (!fromEnv.isNullOrEmpty()) {
            cachedJwtSecret = fromEnv
            return fromEnv
        }

        // Автогенерация криптостойкого UUID-ключа для упрощения self-hosted настройки
        val generatedSecret = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "")

        try {
            val envFile = when {
                File(".env").exists() -> File(".env")
                File("server/.env").exists() -> File("server/.env")
                else -> File(".env")
            }
            if (!envFile.exists() || !envFile.readText().contains("JWT_SECRET=")) {
                envFile.appendText("\n# Auto-generated JWT Secret for Self-Hosted\nJWT_SECRET=$generatedSecret\n")
            }
        } catch (_: Exception) {
            // Игнорируем ошибки файловой системы при read-only среде
        }

        cachedJwtSecret = generatedSecret
        return generatedSecret
    }

    fun getServerBaseUrl(): String? = get("SERVER_BASE_URL") ?: get("APP_SERVER_BASE_URL")

    @Volatile
    var isPublicRegistrationAllowed: Boolean = get("ALLOW_PUBLIC_REGISTRATION")?.toBooleanStrictOrNull() ?: true
}