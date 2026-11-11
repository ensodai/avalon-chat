package app.data.db.tables

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import app.data.db.tables.users.AppUser
import app.data.db.tables.users.AppUsers
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Instant
import kotlin.uuid.Uuid

object AuthSessions : BaseUuidTable("auth_sessions") {

    val userId = reference("user_id", AppUsers, onDelete = ReferenceOption.CASCADE).index()
    val refreshToken = varchar("refresh_token", 255).uniqueIndex()
    val revoked = bool("revoked").default(false)

    // === Данные для жизненного цикла токена ===
    val expiresAt = timestamp("expires_at")
        .index()
    val lastUsedAt = timestamp("last_used_at")
        .nullable()

    // === Аудит и безопасность (данные клиента) ===
    val deviceId = varchar("device_id", 256).nullable()
    val deviceName = varchar("device_name", 256).nullable()
    val userAgent = varchar("user_agent", 255).nullable()
    val ip = varchar("ip", 64).nullable()
    val previousRefreshToken = varchar("previous_refresh_token", 255).nullable().index()
    val lastRotatedAt = timestamp("last_rotated_at").nullable()
    val fcmToken = varchar("fcm_token", 512).nullable()
}

class AuthSession(id: EntityID<Uuid>) : BaseUuidEntity(id, AuthSessions) {
    companion object : BaseUuidEntityClass<AuthSession>(AuthSessions)

    var userId by AuthSessions.userId
    var appUser by AppUser referencedOn AuthSessions.userId
    var refreshToken by AuthSessions.refreshToken
    var revoked by AuthSessions.revoked

    var expiresAt: Instant by AuthSessions.expiresAt
    var lastUsedAt: Instant? by AuthSessions.lastUsedAt

    var deviceId by AuthSessions.deviceId
    var deviceName by AuthSessions.deviceName
    var userAgent by AuthSessions.userAgent
    var ip by AuthSessions.ip
    var previousRefreshToken by AuthSessions.previousRefreshToken
    var lastRotatedAt: Instant? by AuthSessions.lastRotatedAt
    var fcmToken by AuthSessions.fcmToken
}
