package app.utils

import app.plugins.SESSION_ID_CLAIM
import app.plugins.USER_ID_CLAIM
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

fun <T> Optional<T>.orNullOpt(): T? = this.orElse(null)

@OptIn(ExperimentalTime::class)
fun getNewCooldownTime(): Long {
    return Clock.System.now().plus(API_KEY_COOLDOWN_HOURS.hours).toEpochMilliseconds()
}

fun getUTCTimeNow(): Instant {
    return Clock.System.now()
}

val logger: Logger = LoggerFactory.getLogger("MyLogger")

fun RoutingContext.getUserId(): Uuid? {
    val principal = call.principal<JWTPrincipal>()
    val userIdString = principal?.payload?.getClaim(USER_ID_CLAIM)?.asString()

    return try {
        userIdString?.let { Uuid.parse(it) }
    } catch (e: IllegalArgumentException) {
        call.application.environment.log.warn("Invalid UUID format in userId claim: $userIdString", e)
        null
    }
}

fun ApplicationCall.getUserIdFromPrincipal(): Uuid? {
    val principal = principal<JWTPrincipal>()
    val userIdString = principal?.payload?.getClaim(USER_ID_CLAIM)?.asString()

    return try {
        userIdString?.let { Uuid.parse(it) }
    } catch (e: IllegalArgumentException) {
        application.environment.log.warn("Invalid UUID format in userId claim: $userIdString", e)
        null
    }
}

fun ApplicationCall.getSessionIdFromPrincipal(): Uuid? {
    val principal = principal<JWTPrincipal>() ?: return null
    val sessionIdString = principal.payload.getClaim(SESSION_ID_CLAIM).asString() ?: return null
    return try {
        Uuid.parse(sessionIdString)
    } catch (e: Exception) {
        null
    }
}

fun RoutingContext.getSessionIdFromPrincipal(): Uuid? {
    val principal = call.principal<JWTPrincipal>() ?: return null
    val sessionIdString = principal.payload.getClaim(SESSION_ID_CLAIM).asString() ?: return null
    return try {
        Uuid.parse(sessionIdString)
    } catch (e: Exception) {
        null
    }
}

/**
 * Преобразует абсолютный путь к файлу на сервере (например, /var/uploads/uuid/image.jpg)
 * в публичный URL относительно DEFAULT_SERVER_BASE_URL.
 */
fun String.toServerUrl(baseUrl: String = DEFAULT_SERVER_BASE_URL): String {
    // Ищем индекс части "/uploads/" в абсолютном пути
    val uploadsIndex = this.indexOf("/uploads/")

    // Если "/uploads/" не найдено, возвращаем исходную строку (на всякий случай)
    if (uploadsIndex == -1) {
        return this
    }

    // Берем подстроку, начиная с "/uploads/" (включая сам "/uploads/")
    val relativePath = this.substring(uploadsIndex)

    // Соединяем базовый URL и относительный путь
    return "$baseUrl$relativePath"
}

fun String.toUuidOrNull(): Uuid? {
    return try {
        this.trim('"').let { Uuid.parse(it) }
    } catch (e: IllegalArgumentException) {
        null
    }
}

fun String.toUuid(): Uuid = Uuid.parse(this)

@Deprecated("Use toUuidOrNull() instead", ReplaceWith("toUuidOrNull()"))
fun String.toUUIDorNull(): Uuid? = toUuidOrNull()

@Deprecated("Use toUuid() instead", ReplaceWith("toUuid()"))
fun String.toUUID(): Uuid = toUuid()

data class AppVersionInfo(
    val name: String?,
    val code: Int?
) {
    companion object {
        fun fromRequest(call: ApplicationCall): AppVersionInfo {
            return AppVersionInfo(
                name = call.request.headers["Version-Name"],
                code = call.request.headers["Version-Code"]?.toIntOrNull()
            )
        }
    }
}

/**
* Выполняет блокирующий вызов к базе данных внутри транзакции,
* безопасно переключая контекст корутины на Dispatchers.IO.
*
* Это идиоматичная и рекомендованная замена для устаревшей `newSuspendedTransaction`.
* Функция гарантирует, что основной пул потоков Ktor не будет заблокирован
* операциями с базой данных.
*
* @param T Тип возвращаемого значения из блока транзакции.
* @param block Лямбда-выражение с кодом (DSL или DAO) для выполнения внутри транзакции.
* @return Результат выполнения блока [block].
*/
suspend fun <T> dbQuery(
    operation: String = "default_operation",
    isolation: Int = Connection.TRANSACTION_READ_COMMITTED,
    maxAttempts: Int = 1,
    block: suspend Transaction.() -> T
): T {
    return withContext(Dispatchers.IO) {
        val start = System.nanoTime()
        try {
            logger.trace("DB: $operation (start)")
            suspendTransaction(transactionIsolation = isolation) {
                this.maxAttempts = maxAttempts
                block()
            }
        } finally {
            val duration = (System.nanoTime() - start) / 1_000_000
            logger.trace("DB: $operation (completed in ${duration}ms)")
        }
    }
}

private fun isDeadlockError(e: SQLException): Boolean {
    return e.sqlState?.startsWith("40P01") == true // PostgreSQL deadlock
}


// Подумать над этим
//suspend fun <T> paymentTransaction(
//    operation: String,
//    userId: UUID,
//    paymentId: String, // Уникальный ID платежа (из Google Play)
//    block: Transaction.() -> T
//): T = withContext(Dispatchers.IO) {
//    val start = System.nanoTime()
//    val logPrefix = "PAYMENT[$paymentId]-$operation"
//
//    try {
//        transaction(IsolationLevel.TRANSACTION_SERIALIZABLE.levelId) {
//            // 1. Проверяем, не обработан ли платёж ранее (идемпотентность)
//            val existingStatus = UserPayments.select {
//                UserPayments.paymentId eq paymentId
//            }.limit(1).singleOrNull()
//
//            if (existingStatus != null) {
//                logger.warn("$logPrefix: Duplicate payment request. Status=${existingStatus[UserPayments.status]}")
//                throw PaymentException("Duplicate payment: $paymentId")
//            }
//
//            // 2. Начинаем логирование операции
//            logger.info("$logPrefix: Starting transaction")
//
//            // 3. Выполняем бизнес‑логику (передаётся в block)
//            val result = block()
//
//            // 4. Фиксируем успешный платёж
//            UserPayments.insert {
//                it[userId] = userId
//                it[paymentId] = paymentId
//                it[status] = PaymentStatus.COMPLETED
//                it[completedAt] = Clock.System.now()
//            }
//
//            logger.info("$logPrefix: Transaction completed successfully")
//            result
//        }
//    } catch (e: SQLException) {
//        logger.error("$logPrefix: Database error", e)
//        throw PaymentException("Database failure: $paymentId", e)
//    } catch (e: PaymentException) {
//        // Уже обработанная ошибка — перебрасываем
//        throw e
//    } catch (e: Exception) {
//        // Неожиданная ошибка — логируем и перебрасываем как PaymentException
//        logger.error("$logPrefix: Unexpected error", e)
//        throw PaymentException("Unexpected failure: $paymentId", e)
//    } finally {
//        val duration = (System.nanoTime() - start) / 1_000_000
//        logger.info("$logPrefix: Total duration=$duration ms")
//    }
//}

// Контракт: Все временные метки (createdAt, updatedAt, timestamp и т.д.), которые сервер отправляет клиенту,
// должны быть в формате String, представляющем дату и время в ISO 8601 UTC (например, "2025-11-13T10:30:00.123Z").
