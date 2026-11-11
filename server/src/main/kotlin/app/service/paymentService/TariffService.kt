package app.service.paymentService

import com.ensodai.shared.avalon.gifts.models.PremiumStatus
import com.ensodai.shared.avalon.gifts.models.SubscriptionProvider
import app.data.db.tables.users.AppUsers
import app.data.db.tables.users.SubscriptionTiers
import app.utils.dbQuery
import kotlinx.coroutines.*
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class TariffService {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val cache = AtomicReference<Map<PremiumStatus, TierConfig>>(emptyMap())

    init {
        serviceScope.launch {
            refreshCache()

            while (isActive) {
                delay(10.minutes)
                refreshCache()
            }
        }
    }

    private suspend fun refreshCache() {
        try {
            val newCache = dbQuery {
                SubscriptionTiers.selectAll().associate { row ->
                    val status = row[SubscriptionTiers.code]

                    status to TierConfig(
                        status = status,
                        maxDailyEnergy = row[SubscriptionTiers.dailyEnergy],
                        maxReserveBalance = row[SubscriptionTiers.maxCapacity],
                    )
                }
            }

            cache.set(newCache)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getConfig(status: PremiumStatus): TierConfig {
        val currentCache = cache.get()

        return currentCache[status]
            ?: currentCache[PremiumStatus.FREE]
            ?: fallbackHardcodedConfig(PremiumStatus.FREE)
    }

    fun getConfigByCodeName(codeName: String): TierConfig {
        val status = runCatching {
            PremiumStatus.valueOf(codeName.uppercase())
        }.getOrNull() ?: PremiumStatus.FREE

        return getConfig(status)
    }

    suspend fun getUserTariffInfo(userId: Uuid): UserEnergyQuotaInfo {
        return dbQuery {
            AppUsers
                .selectAll()
                .where { AppUsers.id eq userId }
                .singleOrNull()
                ?.let { row ->
                    val status = row[AppUsers.premiumStatus]
                    val config = getConfig(status)
                    UserEnergyQuotaInfo(
                        premiumStatus = status,
                        currentQuota = row[AppUsers.currentDailyBalance],
                        maxQuota = config.maxDailyEnergy,
                        reserveBalance = row[AppUsers.reserveBalance],
                        maxReserveBalance = config.maxReserveBalance,
                        paymentProvider = null,
                        nextBillingDate = null
                    )
                } ?: fallbackUserTariffInfo()
        }
    }

    private fun fallbackUserTariffInfo() = UserEnergyQuotaInfo(
        premiumStatus = PremiumStatus.FREE,
        currentQuota = 0,
        maxQuota = 0,
        reserveBalance = 0,
        maxReserveBalance = 0,
        paymentProvider = null,
        nextBillingDate = null
    )

    private fun fallbackHardcodedConfig(status: PremiumStatus) = TierConfig(
        status = status,
        maxDailyEnergy = if (status == PremiumStatus.FREE) 0 else 50,
        maxReserveBalance = 0,
    )
}


data class TierConfig(
    val status: PremiumStatus,
    val maxDailyEnergy: Int,
    val maxReserveBalance: Int,
)

data class UserEnergyQuotaInfo(
    val premiumStatus: PremiumStatus,
    val currentQuota: Int,
    val maxQuota: Int,
    val reserveBalance: Int,
    val maxReserveBalance: Int,
    val paymentProvider: SubscriptionProvider?,
    val nextBillingDate: kotlin.time.Instant?,
)
