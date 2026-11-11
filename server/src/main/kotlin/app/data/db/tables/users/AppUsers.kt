package app.data.db.tables.users
import com.ensodai.shared.avalon.gifts.models.*

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import com.ensodai.shared.avalon.gifts.models.*
import com.ensodai.shared.avalon.persona.models.PersonaResponse
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Instant
import kotlin.uuid.Uuid

object AppUsers : BaseUuidTable("app_users") {
    val googleSub = varchar("google_sub", 255).nullable().uniqueIndex()
    val passwordHash = varchar("password_hash", 255).nullable()
    val name = varchar("name", 50)
    val nickName = varchar("nick_name", 20).index("idx_users_nickname")
    val email = varchar("email", 255).nullable()
    val imagePath  = varchar("image_path", 255).nullable()
    val currentDailyBalance = integer("current_daily_balance")
    val reserveBalance = integer("reserve_balance").default(0)
    val lastQuotaReset = long("last_quota_reset").default(System.currentTimeMillis())
    val totalGeneratedMessages = integer("total_generated_messages").default(0)
    val defaultPersona = reference("default_persona", UserPersonas, onDelete = ReferenceOption.SET_NULL).nullable()
    val isBanned = bool("is_banned").default(false)
    val isShadowBanned = bool("is_shadow_banned").default(false)
    val totalGeneratedTokens = integer("total_generated_tokens").default(0)
    val rarityProfileName = varchar("rarity_profile_name", 50).default("CLASSIC_RPG")
    val premiumStatus = enumerationByName<PremiumStatus>("premium_status", 20)
        .default(PremiumStatus.FREE)
    val lastSeenAt = timestamp("last_seen_at").nullable()
    val timezoneOffset = integer("timezone_offset").default(0)
    val isCreatorOutreachBlocked = bool("is_creator_outreach_blocked").default(false)
    val isOutreachEnabled = bool("is_outreach_enabled").default(false)
    val dailyOutreachLimit = integer("daily_outreach_limit").default(3)
    val isFlowModeEnabled = bool("is_flow_mode_enabled").default(false)
    val isEarlySupporter = bool("is_early_supporter").default(false)

    init {
        index(
            customIndexName = "idx_users_active_status",
            isUnique = false,
            columns = arrayOf(deletedAt, isBanned)
        )
    }
}

class AppUser(id: EntityID<Uuid>) : BaseUuidEntity(id, AppUsers) {
    companion object : BaseUuidEntityClass<AppUser>(AppUsers)

    var googleSub by AppUsers.googleSub
    var passwordHash by AppUsers.passwordHash
    var name by AppUsers.name
    var nickName by AppUsers.nickName
    var email by AppUsers.email
    var avatarUrl by AppUsers.imagePath
    var currentQuotaLimit by AppUsers.currentDailyBalance
    var reserveBalance by AppUsers.reserveBalance
//    var maxQuotaLimit by AppUsers.maxQuotaLimit
    var lastQuotaReset by AppUsers.lastQuotaReset
    var totalGeneratedMessages by AppUsers.totalGeneratedMessages
    var defaultPersona by UserPersona optionalReferencedOn AppUsers.defaultPersona
    var isBanned by AppUsers.isBanned
    var isShadowBanned by AppUsers.isShadowBanned
    var totalGeneratedTokens by AppUsers.totalGeneratedTokens
    var rarityProfileName by AppUsers.rarityProfileName
    var lastSeenAt by AppUsers.lastSeenAt
    var timezoneOffset by AppUsers.timezoneOffset
    var isCreatorOutreachBlocked by AppUsers.isCreatorOutreachBlocked
    var isOutreachEnabled by AppUsers.isOutreachEnabled
    var dailyOutreachLimit by AppUsers.dailyOutreachLimit
    var isFlowModeEnabled by AppUsers.isFlowModeEnabled
    var isEarlySupporter by AppUsers.isEarlySupporter
}

data class UserAccountAggregatedData(
    val id: Uuid,
    val name: String,
    val nickName: String,
    val imagePath: String?,
    val currentQuota: Int,
    val maxQuota: Int,
    val reserveBalance: Int,
    val maxReserveBalance: Int,
    val defaultPersonaId: Uuid?,
    val premiumStatus: PremiumStatus,
    val personas: List<PersonaResponse>,
    val paymentProvider: SubscriptionProvider?,
    val nextBillingDate: Instant?,
    val isEarlySupporter: Boolean = false,
)

data class AppUserInternalDto(
    val id: Uuid,
    val googleSub: String?,
    val passwordHash: String? = null,
    val name: String?,
    val nickName: String?,
    val email: String?,
    val avatarUrl: String?,
    val currentQuotaLimit: Int,
//    val maxQuotaLimit: Int,
    val lastQuotaReset: Long,
    val totalGeneratedMessages: Int,
    val deletedAt: Instant?,
    val defaultPersona: PersonaInternalDto?,
    val isBanned: Boolean,
    val isShadowBanned: Boolean,
    val totalGeneratedTokens: Int,
    val rarityProfileName: String,
    val lastSeenAt: Instant?,
    val timezoneOffset: Int,
    val isCreatorOutreachBlocked: Boolean,
    val isOutreachEnabled: Boolean,
    val dailyOutreachLimit: Int,
    val isFlowModeEnabled: Boolean
)

fun AppUser.toInternalDto(): AppUserInternalDto {
    return AppUserInternalDto(
        id = this.id.value,
        googleSub = this.googleSub,
        passwordHash = this.passwordHash,
        name = this.name,
        nickName = this.nickName,
        email = this.email,
        avatarUrl = this.avatarUrl,
        currentQuotaLimit = this.currentQuotaLimit,
//        maxQuotaLimit = this.maxQuotaLimit,
        lastQuotaReset = this.lastQuotaReset,
        totalGeneratedMessages = this.totalGeneratedMessages,
        deletedAt = this.deletedAt,
        defaultPersona = this.defaultPersona?.toInternalDto(),
        isBanned = this.isBanned,
        isShadowBanned = this.isShadowBanned,
        totalGeneratedTokens = this.totalGeneratedTokens,
        rarityProfileName = this.rarityProfileName,
        lastSeenAt = this.lastSeenAt,
        timezoneOffset = this.timezoneOffset,
        isCreatorOutreachBlocked = this.isCreatorOutreachBlocked,
        isOutreachEnabled = this.isOutreachEnabled,
        dailyOutreachLimit = this.dailyOutreachLimit,
        isFlowModeEnabled = this.isFlowModeEnabled
    )
}

fun ResultRow.toAppUserInternalDto(): AppUserInternalDto {
    return AppUserInternalDto(
        id = this[AppUsers.id].value,
        googleSub = this[AppUsers.googleSub],
        passwordHash = this[AppUsers.passwordHash],
        name = this[AppUsers.name],
        nickName = this[AppUsers.nickName],
        email = this[AppUsers.email],
        avatarUrl = this[AppUsers.imagePath], // В DTO avatarUrl, в базе imagePath
        currentQuotaLimit = this[AppUsers.currentDailyBalance],
//        maxQuotaLimit = this[AppUsers.maxQuotaLimit],
        lastQuotaReset = this[AppUsers.lastQuotaReset],
        totalGeneratedMessages = this[AppUsers.totalGeneratedMessages],
        deletedAt = this[AppUsers.deletedAt],
        defaultPersona = null,
        isBanned = this[AppUsers.isBanned],
        isShadowBanned = this[AppUsers.isShadowBanned],
        totalGeneratedTokens = this[AppUsers.totalGeneratedTokens],
        rarityProfileName = this[AppUsers.rarityProfileName],
        lastSeenAt = this[AppUsers.lastSeenAt],
        timezoneOffset = this[AppUsers.timezoneOffset],
        isCreatorOutreachBlocked = this[AppUsers.isCreatorOutreachBlocked],
        isOutreachEnabled = this[AppUsers.isOutreachEnabled],
        dailyOutreachLimit = this[AppUsers.dailyOutreachLimit],
        isFlowModeEnabled = this[AppUsers.isFlowModeEnabled]
    )
}

object Moderators : BaseUuidTable("moderators") {
    val user = reference("user_id", AppUsers) // связь с таблицей Users
    val assignedAt = long("assigned_at").default(System.currentTimeMillis())
    val isActive = bool("is_active").default(true)

    init {
        index(customIndexName = "idx_moderators_user", isUnique = true, user)
    }
}

class Moderator(id: EntityID<Uuid>) : BaseUuidEntity(id, Moderators) {
    companion object : BaseUuidEntityClass<Moderator>(Moderators)

    var appUser by AppUser referencedOn Moderators.user
    var assignedAt by Moderators.assignedAt
    var isActive by Moderators.isActive
}

data class ModeratorInternalDto(
    val id: Uuid,
    val userId: Uuid,
    val userName: String?,
    val assignedAt: Long,
    val isActive: Boolean
)

// ВАЖНО: Вызывать только когда `appUser` загружен через .with()
fun Moderator.toInternalDto(): ModeratorInternalDto {
    return ModeratorInternalDto(
        id = this.id.value,
        userId = this.appUser.id.value,
        userName = this.appUser.name,
        assignedAt = this.assignedAt,
        isActive = this.isActive
    )
}
