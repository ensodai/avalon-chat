package app.utils

import app.data.db.tables.users.AppUsers
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob
import kotlin.time.Clock
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.update

private val log = KotlinLogging.logger {}

/**
 * # UserActivityTracker (Сервис оптимизации активности пользователей)
 *
 * Этот сервис предназначен для высокопроизводительного отслеживания времени последнего визита
 * пользователей в приложение (last_seen_at) с целью оптимизации нагрузки на базу данных PostgreSQL.
 *
 * ## Проблема:
 * При высокой активности пользователя в реальном времени (например, отправка десятков сообщений
 * или частые переходы по экранам) выполнять SQL-запрос `UPDATE app_users SET last_seen_at = NOW()`
 * на каждый HTTP/RPC запрос крайне расточительно — это создаёт огромный оверхед на базу данных.
 *
 * ## Решение (Гибридный кэш):
 * 1. Мы храним в оперативной памяти бэкенда (`ConcurrentHashMap`) кэш меток времени последней активности
 *    пользователей: `[userId -> timestamp_ms]`.
 * 2. При любом RPC-запросе через [BaseRpcService] вызывается метод [updateLastSeen].
 * 3. Если пользователя нет в кэше или с момента последнего обновления в памяти прошло **более 15 минут** (900,000 мс):
 *    - Мы обновляем метку времени в локальной HashMap.
 *    - Запускаем асинхронную корутину на пуле [Dispatchers.IO] для выполнения Exposed-транзакции в БД.
 * 4. Если 15 минут еще не прошло: запрос полностью игнорируется для базы данных (срабатывает защита от спама БД).
 *
 * ## Интеграция с базой данных:
 * Обновление `last_seen_at` в Exposed-таблице [AppUsers] выполняется асинхронно:
 * ```kotlin
 * dbQuery {
 *     AppUsers.update({ AppUsers.id eq userId }) {
 *         it[lastSeenAt] = Clock.System.now()
 *     }
 * }
 * ```
 */
object UserActivityTracker {

    // Кэш последней активности в оперативной памяти бэкенда
    private val lastSeenCache = ConcurrentHashMap<Uuid, Long>()

    // Интервал между принудительными обновлениями базы данных (15 минут в миллисекундах)
    private const val UPDATE_INTERVAL_MS = 15 * 60 * 1000L

    // Собственный CoroutineScope для выполнения фоновых неблокирующих транзакций обновления БД
    private val trackingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Регистрирует активность пользователя.
     * Проверяет кэш в памяти и при необходимости асинхронно обновляет базу данных.
     *
     * @param userId Уникальный идентификатор пользователя (Uuid)
     */
    fun updateLastSeen(userId: Uuid) {
        val currentTime = System.currentTimeMillis()
        val lastSeen = lastSeenCache[userId]

        if (lastSeen == null || (currentTime - lastSeen) >= UPDATE_INTERVAL_MS) {
            // Обновляем локальный кэш в оперативной памяти
            lastSeenCache[userId] = currentTime

            // Асинхронно запускаем обновление в базе данных, чтобы не блокировать текущий RPC-запрос
            trackingScope.launch {
                try {
                    dbQuery {
                        AppUsers.update({ AppUsers.id eq userId }) {
                            it[lastSeenAt] = Clock.System.now()
                        }
                    }
                    log.debug { "[UserActivityTracker] Асинхронное обновление last_seen_at для пользователя: $userId" }
                } catch (e: Throwable) {
                    log.error(e) { "[UserActivityTracker] Ошибка при обновлении last_seen_at для пользователя: $userId" }
                }
            }
        }
    }

    /**
     * Позволяет вручную сбросить кэш пользователя (например, при логауте).
     */
    fun evict(userId: Uuid) {
        lastSeenCache.remove(userId)
        log.debug { "[UserActivityTracker] Кэш активности для пользователя $userId успешно сброшен" }
    }
}
