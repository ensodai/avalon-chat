package app.service

import app.data.db.tables.users.AppUsers
import app.data.db.tables.users.SubscriptionStatus
import app.data.db.tables.users.Subscriptions
import app.service.paymentService.TariffService
import app.utils.dbQuery
import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import kotlinx.coroutines.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid


object QuotaResetService {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val logger = org.slf4j.LoggerFactory.getLogger(QuotaResetService::class.java)

    private val energyService = EnergyService()
    private val tariffService: TariffService by lazy { TariffService() }

    fun start() {
        logger.info("QuotaResetService: Service started. Timezone: UTC")
        scope.launch {
            while (isActive) {
                try {
                    val now = Clock.System.now()
                    val nowJava = java.time.Instant.ofEpochMilli(now.toEpochMilliseconds()).atOffset(ZoneOffset.UTC)
                    val nextReset = nowJava.toLocalDate().plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC)

                    val millisUntilNextReset = ChronoUnit.MILLIS.between(nowJava, nextReset).coerceAtLeast(0)

                    logger.info("QuotaResetService: Waiting ${millisUntilNextReset / 1000}s until reset.")
                    delay(millisUntilNextReset.milliseconds)

                    logger.info("QuotaResetService: waking up to reset quotas...")

                    val count = resetQuotas()

                    logger.info("QuotaResetService: Successfully reset quotas for $count users.")

                    delay(2000)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    logger.error("QuotaResetService: Error", e)
                    delay(60_000)
                }
            }
        }
    }

    private suspend fun resetQuotas(): Int {

        val activeUserIds = dbQuery {
            AppUsers.select(AppUsers.id)
                .where { AppUsers.deletedAt.isNull() }
                .map { it[AppUsers.id].value }
        }

        logger.info("Starting reset for ${activeUserIds.size} users...")

        var successCount = 0
        var downgradedCount = 0

        activeUserIds.forEach { userId ->
            try {
                // Сначала проверяем подписку - если истекла или отсутствует, downgrade до FREE
                val wasDowngraded = checkAndDowngradeExpiredSubscription(userId)
                
                if (wasDowngraded) {
                    downgradedCount++
                    logger.info("QuotaResetService: User $userId downgraded to FREE due to expired subscription")
                }
                
                // Затем выполняем ежедневный сброс энергии
                energyService.processDailyReset(userId)
                successCount++
            } catch (e: Exception) {
                logger.error("Failed reset for $userId", e)
            }
        }

        if (downgradedCount > 0) {
            logger.info("QuotaResetService: Downgraded $downgradedCount users to FREE tier")
        }

        return successCount
    }

    /**
     * Проверяет подписку пользователя и если у него нет действующей активной подписки,
     * но статус в app_users не FREE — переводит в FREE и сбрасывает лимиты энергии.
     * @return true если был выполнен downgrade до FREE
     */
    private suspend fun checkAndDowngradeExpiredSubscription(userId: Uuid): Boolean {
        return dbQuery {
            val userRow = AppUsers.select(AppUsers.premiumStatus)
                .where { AppUsers.id eq userId }
                .singleOrNull() ?: return@dbQuery false

            val currentStatus = userRow[AppUsers.premiumStatus]
            val now = Clock.System.now()

            // 1. Помечаем все активные подписки, срок которых истек, как EXPIRED
            Subscriptions.update({
                (Subscriptions.user eq userId) and
                (Subscriptions.status eq SubscriptionStatus.ACTIVE) and
                (Subscriptions.expiresAt lessEq now)
            }) {
                it[status] = SubscriptionStatus.EXPIRED
                it[lastValidationDate] = now
            }

            // 2. Ищем действующие (не истекшие) активные подписки
            val activeSubs = Subscriptions.selectAll()
                .where {
                    (Subscriptions.user eq userId) and
                    (Subscriptions.status eq SubscriptionStatus.ACTIVE) and
                    (Subscriptions.expiresAt greater now)
                }
                .toList()

            val maxActiveSub = activeSubs.maxByOrNull { it[Subscriptions.plan].ordinal }

            if (maxActiveSub != null) {
                val targetPlan = maxActiveSub[Subscriptions.plan]
                if (currentStatus != targetPlan) {
                    logger.info("QuotaResetService: User $userId status synced ($currentStatus -> $targetPlan)")
                    AppUsers.update({ AppUsers.id eq userId }) {
                        it[premiumStatus] = targetPlan
                    }
                }
                return@dbQuery false
            } else {
                // Если активных валидных подписок нет, а статус пользователя не FREE -> сбрасываем в FREE
                if (currentStatus != PremiumStatus.FREE) {
                    val freeConfig = tariffService.getConfig(PremiumStatus.FREE)

                    AppUsers.update({ AppUsers.id eq userId }) {
                        it[premiumStatus] = PremiumStatus.FREE
                        it[currentDailyBalance] = freeConfig.maxDailyEnergy
                        it[reserveBalance] = freeConfig.maxReserveBalance
                    }

                    logger.info(
                        "QuotaResetService: User $userId downgraded from $currentStatus to FREE. " +
                        "Daily: ${freeConfig.maxDailyEnergy}, Reserve: ${freeConfig.maxReserveBalance}"
                    )
                    return@dbQuery true
                }
                return@dbQuery false
            }
        }
    }

    fun stopQuotaResetService() {
        scope.cancel()
        logger.info("QuotaResetService: Stopped")
    }
}