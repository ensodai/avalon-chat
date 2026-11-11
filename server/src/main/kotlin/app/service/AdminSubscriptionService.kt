package app.service
import com.ensodai.shared.avalon.gifts.models.*

import app.data.db.tables.users.*
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import com.ensodai.shared.avalon.gifts.models.SubscriptionProvider
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class AdminSubscriptionService {

    // ==========================================
    // ПУБЛИЧНЫЕ МЕТОДЫ ДЛЯ БОТА (принимают String)
    // ==========================================

    /**
     * Обертка для бота: находит юзера по строке и выдает права.
     * @param userIdentifier строка: Uuid, Email или Nickname
     */
    fun grantPremiumByString(
        userIdentifier: String,
        targetPlan: PremiumStatus,
        durationMonths: Long = 1200,
        adminComment: String = "System Grant"
    ) {
        val uuid = findUserId(userIdentifier)
            ?: throw IllegalArgumentException("Пользователь с идентификатором '$userIdentifier' не найден.")

        // Вызываем основную логику
        grantPremium(uuid, targetPlan, durationMonths, adminComment)
    }

    /**
     * Обертка для бота: находит юзера по строке и отбирает права.
     */
    fun revokeGrantByString(
        userIdentifier: String,
        adminComment: String = "Revoked by admin"
    ) {
        val uuid = findUserId(userIdentifier)
            ?: throw IllegalArgumentException("Пользователь с идентификатором '$userIdentifier' не найден.")

        // Вызываем основную логику
        revokeGrant(uuid, adminComment)
    }

    // ==========================================
    // ВНУТРЕННЯЯ ЛОГИКА ПОИСКА
    // ==========================================

    private fun findUserId(input: String): Uuid? {
        return transaction {
            // 1. Проверяем, может это UUID?
            val uuidAttempt = try { Uuid.parse(input) } catch (e: Exception) { null }
            if (uuidAttempt != null) {
                // Проверяем, есть ли такой ID в базе
                val exists = AppUsers.select(AppUsers.id)
                    .where { AppUsers.id eq uuidAttempt }
                    .count() > 0

                if (exists) return@transaction uuidAttempt
            }

            // 2. Ищем по Email (игнорируя регистр, если база позволяет, или просто точное совпадение)
            // LowerCase в Exposed делается через .lowerCase(), но для простоты начнем с точного совпадения
            val byEmail = AppUsers.select(AppUsers.id)
                .where { AppUsers.email eq input }
                .singleOrNull()

            if (byEmail != null) return@transaction byEmail[AppUsers.id].value

            // 3. Ищем по Нику
            val byNick = AppUsers.select(AppUsers.id)
                .where { AppUsers.nickName eq input }
                .singleOrNull()

            return@transaction byNick?.get(AppUsers.id)?.value
        }
    }

    // ==========================================
    // БАЗОВЫЕ МЕТОДЫ (работают с UUID)
    // ==========================================

    fun grantPremium(
        userId: Uuid,
        targetPlan: PremiumStatus,
        durationMonths: Long = 1200,
        adminComment: String = "System Grant"
    ) {
        transaction {
            // 1. Находим пользователя и его резерв
            val userRow = AppUsers
                .select(AppUsers.id, AppUsers.reserveBalance)
                .where { AppUsers.id eq userId }
                .singleOrNull()
                ?: throw IllegalArgumentException("User with ID $userId not found")

            // 2. Получаем настройки Тира
            val tierRow = SubscriptionTiers
                .select(SubscriptionTiers.dailyEnergy)
                .where { SubscriptionTiers.code eq targetPlan }
                .singleOrNull()

            val newDailyLimit = tierRow?.get(SubscriptionTiers.dailyEnergy)
                ?: throw IllegalStateException("Tier $targetPlan not found in DB")

            // 3. Создаем запись в Subscriptions
            // Используем timestamp для уникальности токена
            val grantToken = "GRANT_${targetPlan}_${userId}_${System.currentTimeMillis()}"
            val now = Clock.System.now()
            val expiryDate = now.plus(durationMonths.days)

            Subscriptions.insert {
                it[user] = userId
                it[status] = SubscriptionStatus.ACTIVE
                it[provider] = SubscriptionProvider.SYSTEM_GRANT
                it[productId] = "system_grant_${targetPlan.toString().lowercase()}"
                it[plan] = targetPlan
                it[purchaseId] = grantToken
                it[startDate] = now
                it[expiresAt] = expiryDate
                it[lastValidationDate] = now
                it[isAutoRenewing] = false
                it[country] = "SYSTEM"
                it[isTestPurchase] = true
            }

            // 4. Обновляем юзера
            AppUsers.update({ AppUsers.id eq userId }) {
                it[premiumStatus] = targetPlan
                it[currentDailyBalance] = newDailyLimit
            }

            // 5. Пишем историю
            val currentReserve = userRow[AppUsers.reserveBalance]
            EnergyHistory.insert {
                it[user] = userId
                it[operationType] = EnergyOperationType.ADMIN_ADJUSTMENT
                it[amount] = 0
                it[dailyBalanceAfter] = newDailyLimit
                it[reserveBalanceAfter] = currentReserve
                it[metaData] = "Granted $targetPlan via Admin. Reason: $adminComment"
                it[relatedMessageId] = null
            }
        }
    }

    fun revokeGrant(userId: Uuid, adminComment: String = "Revoked by admin") {
        transaction {
            // 1. Отменяем подписку
            Subscriptions.update({
                (Subscriptions.user eq userId) and
                        (Subscriptions.provider eq SubscriptionProvider.SYSTEM_GRANT) and
                        (Subscriptions.status eq SubscriptionStatus.ACTIVE)
            }) {
                it[status] = SubscriptionStatus.CANCELED
                it[expiresAt] = Clock.System.now()
            }

            // 2. Узнаем лимит для FREE
            val freeTierRow = SubscriptionTiers
                .select(SubscriptionTiers.dailyEnergy)
                .where { SubscriptionTiers.code eq PremiumStatus.FREE }
                .singleOrNull()
            val freeLimit = freeTierRow?.get(SubscriptionTiers.dailyEnergy) ?: 10

            // 3. Возвращаем юзера на FREE
            AppUsers.update({ AppUsers.id eq userId }) {
                it[premiumStatus] = PremiumStatus.FREE
                it[currentDailyBalance] = freeLimit
            }

            // 4. Лог
            val currentReserve = AppUsers
                .select(AppUsers.reserveBalance)
                .where { AppUsers.id eq userId }
                .singleOrNull()
                ?.get(AppUsers.reserveBalance) ?: 0

            EnergyHistory.insert {
                it[user] = userId
                it[operationType] = EnergyOperationType.ADMIN_ADJUSTMENT
                it[metaData] = "Revoked premium. Reason: $adminComment"
                it[amount] = 0
                it[dailyBalanceAfter] = freeLimit
                it[reserveBalanceAfter] = currentReserve
            }
        }
    }
}