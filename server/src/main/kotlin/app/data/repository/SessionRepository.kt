package app.data.repository

import app.data.db.tables.AuthSession
import app.data.db.tables.AuthSessions
import app.data.db.tables.users.AppUser
import app.plugins.generateAccessToken
import app.utils.REFRESH_TOKEN_LIVE_DAYS
import app.utils.dbQuery
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

sealed class RotationResult {
    // Всё ок, держи токены
    data class Success(
        val accessToken: String,
        val refreshToken: String,
        val userId: Uuid
    ) : RotationResult()

    // Токен не найден вообще
    object NotFound : RotationResult()

    // Сессия была отозвана (бан)
    object Revoked : RotationResult()

    // КРИТИЧЕСКОЕ: Кто-то пытается использовать очень старый токен (кража)
    object TheftDetected : RotationResult()
}

@Single
class SessionRepository {
    private val GRACE_PERIOD_SECONDS = 45L
    private val DEVICE_ID_MAX_LENGTH = 256
    private val DEVICE_NAME_MAX_LENGTH = 256
    private val USER_AGENT_MAX_LENGTH = 256
    private val IP_MAX_LENGTH = 64
    /**
     * Проверяет, что сессия существует, принадлежит указанному пользователю и не отозвана.
     * Использует эффективный запрос `SELECT COUNT(*)` с условиями.
     *
     * @param sessionId Uuid сессии, извлеченный из JWT.
     * @param userId Uuid пользователя, извлеченный из JWT.
     * @return `true`, если сессия валидна, иначе `false`.
     */
    suspend fun isSessionValid(sessionId: Uuid, userId: Uuid): Boolean {
        return dbQuery {

            val query = AuthSession.find {
                (AuthSessions.id eq sessionId) and
                        (AuthSessions.userId eq userId) and
                        (AuthSessions.revoked eq false)
            }

            !query.empty()
        }
    }

    suspend fun createNewSession(
        userId: Uuid,
        deviceId: String?,
        deviceName: String?,
        userAgent: String?,
        ip: String?,
        fcmToken: String? = null
    ): AuthSession {
        return dbQuery {
            val appUserEntity = AppUser.findById(userId)
                ?: throw IllegalStateException("User not found, cannot create session")

            // Генерируем уникальный и криптостойкий refresh токен
            val newRefreshToken = Uuid.random().toString() + "-" + Uuid.random().toString()

            AuthSession.new {
                this.appUser = appUserEntity
                this.refreshToken = newRefreshToken
                this.revoked = false

                val currentTime = Clock.System.now()
                this.createdAt = currentTime
                this.lastUsedAt = currentTime
                this.expiresAt = currentTime.plus(REFRESH_TOKEN_LIVE_DAYS.days)

                this.deviceId = deviceId?.take(DEVICE_ID_MAX_LENGTH)
                this.deviceName = deviceName?.take(DEVICE_NAME_MAX_LENGTH)
                this.userAgent = userAgent?.take(USER_AGENT_MAX_LENGTH)
                this.ip = ip?.take(IP_MAX_LENGTH)
                this.fcmToken = fcmToken?.take(512)
            }
        }
    }

    suspend fun findByRefreshToken(tokenValue: String): AuthSession? {
        return dbQuery {
            // Ищем сессию, у которой поле refreshToken совпадает с искомым
            AuthSession.find { AuthSessions.refreshToken eq tokenValue }.singleOrNull()
        }
    }

    suspend fun rotateRefreshToken(incomingToken: String, fcmToken: String? = null): RotationResult {
        return dbQuery {
            val now = Clock.System.now()

            // --- ШАГ 1: Поиск по АКТУАЛЬНОМУ токену (Штатный режим) ---
            // Это сработает для ПЕРВОГО запроса в гонке (победителя)
            val activeSession = AuthSession.find { AuthSessions.refreshToken eq incomingToken }
                .singleOrNull()

            if (activeSession != null) {
                if (activeSession.revoked) return@dbQuery RotationResult.Revoked

                // Проверяем срок действия
                if (activeSession.expiresAt < now) {
                    return@dbQuery RotationResult.Revoked
                }

                // Обновляем fcmToken, если прислан новый
                if (fcmToken != null) {
                    activeSession.fcmToken = fcmToken.take(512)
                }

                // === РОТАЦИЯ ===
                // 1. Сохраняем текущий токен в "архив"
                activeSession.previousRefreshToken = activeSession.refreshToken
                activeSession.lastRotatedAt = now

                // 2. Генерируем новый
                val newRefreshToken = Uuid.random().toString() + "-" + Uuid.random().toString()
                activeSession.refreshToken = newRefreshToken

                // 3. Обновляем метки времени
                activeSession.lastUsedAt = now
                activeSession.expiresAt = now.plus(REFRESH_TOKEN_LIVE_DAYS.days)

                // 4. Генерируем Access Token
                val newAccessToken = generateAccessToken(
                    userId = activeSession.userId.value.toString(),
                    sessionId = activeSession.id.value.toString()
                )

                return@dbQuery RotationResult.Success(
                    accessToken = newAccessToken,
                    refreshToken = newRefreshToken,
                    userId = activeSession.userId.value
                )
            }

            // --- ШАГ 2: Поиск по ПРЕДЫДУЩЕМУ токену (Режим Grace Period) ---
            // Сюда попадут все "проигравшие" в гонке запросы (Retrofit, ImageLoader)
            val rotatedSession = AuthSession.find { AuthSessions.previousRefreshToken eq incomingToken }
                .singleOrNull()

            if (rotatedSession != null) {
                if (rotatedSession.revoked) return@dbQuery RotationResult.Revoked

                // Обновляем fcmToken, если прислан новый
                if (fcmToken != null) {
                    rotatedSession.fcmToken = fcmToken.take(512)
                }

                // Считаем, сколько времени прошло с момента смены токена
                val lastRotatedAt = rotatedSession.lastRotatedAt ?: now
                val secondsSinceRotation = (now - lastRotatedAt).inWholeSeconds

                if (secondsSinceRotation < GRACE_PERIOD_SECONDS) {
                    // ✅ УСПЕХ (Grace Period)
                    // Мы понимаем, что это просто дубль запроса из-за лагов сети.
                    // Возвращаем АКТУАЛЬНЫЙ токен (который уже лежит в базе).
                    // МЫ НЕ ГЕНЕРИРУЕМ НОВЫЙ, чтобы не плодить цепочку.

                    val currentAccessToken = generateAccessToken(
                        userId = rotatedSession.userId.value.toString(),
                        sessionId = rotatedSession.id.value.toString()
                    )

                    return@dbQuery RotationResult.Success(
                        accessToken = currentAccessToken,
                        refreshToken = rotatedSession.refreshToken,
                        userId = rotatedSession.userId.value
                    )
                } else {

                    // Старый токен используется спустя минуту/час/год.
                    // Значит, токен украли и пытаются использовать.
                    // ДЛЯ БЕЗОПАСНОСТИ УБИВАЕМ ВСЮ СЕССИЮ.

                    rotatedSession.revoked = true
                    return@dbQuery RotationResult.TheftDetected
                }
            }

            // --- ШАГ 3: Токен вообще левый ---
            return@dbQuery RotationResult.NotFound
        }
    }

    suspend fun revokeSessionByRefreshToken(tokenValue: String): Boolean {
        return dbQuery {
            // Используем DSL `update` для атомарной операции.
            // Он найдет запись с нужным токеном и выставит `revoked = true`.
            val updatedRows = AuthSessions.update({ AuthSessions.refreshToken eq tokenValue }) {
                it[revoked] = true
            }
            // `update` возвращает количество измененных строк. Если > 0, значит, мы нашли и обновили токен.
            updatedRows > 0
        }
    }
}
