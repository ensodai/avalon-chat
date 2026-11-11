package app.domain.syncmanager

import app.data.db.tables.users.AppUsers
import app.data.db.tables.users.SubscriptionStatus
import app.data.db.tables.users.Subscriptions
import com.ensodai.shared.avalon.auth.models.AppUserResponse
import app.service.paymentService.TariffService
import app.utils.toServerUrl
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.leftJoin
import org.jetbrains.exposed.v1.jdbc.select
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

@Single
class UserProfileSyncService(private val tariffService: TariffService) {

    fun fetchUser(userId: Uuid, since: Instant): AppUserResponse? {
        val userRow = AppUsers
            .leftJoin(
                otherTable = Subscriptions,
                onColumn = { AppUsers.id },
                otherColumn = { Subscriptions.user },
                additionalConstraint = { Subscriptions.status eq SubscriptionStatus.ACTIVE }
            )
            .select(
                AppUsers.id,
                AppUsers.name,
                AppUsers.nickName,
                AppUsers.imagePath,
                AppUsers.currentDailyBalance,
                AppUsers.defaultPersona,
                AppUsers.premiumStatus,
                AppUsers.reserveBalance,
                AppUsers.isOutreachEnabled,
                AppUsers.dailyOutreachLimit,
                AppUsers.isFlowModeEnabled,

                Subscriptions.provider,
                Subscriptions.expiresAt
            )
            .where { (AppUsers.id eq userId) and (AppUsers.updatedAt greater since) }
            .limit(1)
            .singleOrNull()
            ?: return null // Если null, значит юзера нет или он не менялся (updatedAt)

        val premiumStatus = userRow[AppUsers.premiumStatus]
        val tariff = tariffService.getConfig(premiumStatus)

        val provider = userRow.getOrNull(Subscriptions.provider)
        val nextBillingDate = userRow.getOrNull(Subscriptions.expiresAt)

        return AppUserResponse(
            id = userRow[AppUsers.id].value,
            name = userRow[AppUsers.name],
            nickName = userRow[AppUsers.nickName],
            avatarUrl = userRow[AppUsers.imagePath]?.toServerUrl(),
            currentQuotaLimit = userRow[AppUsers.currentDailyBalance],
            maxQuotaLimit = tariff.maxDailyEnergy,
            reserveBalance = userRow[AppUsers.reserveBalance],
            maxReserveBalance = tariff.maxReserveBalance,
            premiumStatus = premiumStatus,
            defaultPersonaId = userRow[AppUsers.defaultPersona]?.value,
            paymentProvider = provider,
            nextBillingDate = nextBillingDate,
            isOutreachEnabled = userRow[AppUsers.isOutreachEnabled],
            dailyOutreachLimit = userRow[AppUsers.dailyOutreachLimit],
            isFlowModeEnabled = userRow[AppUsers.isFlowModeEnabled]
        )
    }
}