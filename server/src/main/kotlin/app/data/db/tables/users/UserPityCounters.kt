package app.data.db.tables.users

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.time.Instant
import kotlin.uuid.Uuid

object UserPityCounters : BaseUuidTable("user_pity_counters") {
    val userId = reference("user_id", AppUsers, onDelete = ReferenceOption.CASCADE)
    val profileName = varchar("profile_name", 50)
    val pityCount = integer("pity_count").default(0)

    init {
        uniqueIndex("unique_user_profile_idx", userId, profileName)
        index("user_pity_counters_created_at_idx", isUnique = false, createdAt)
        index("user_pity_counters_updated_at_idx", isUnique = false, updatedAt)
    }
}

class UserPityCounter(id: EntityID<Uuid>) : BaseUuidEntity(id, UserPityCounters) {
    companion object : BaseUuidEntityClass<UserPityCounter>(UserPityCounters)

    var userId: EntityID<Uuid> by UserPityCounters.userId
    var profileName: String by UserPityCounters.profileName
    var pityCount: Int by UserPityCounters.pityCount

}

data class UserPityCounterInternalDto(
    val id: Uuid,
    val userId: Uuid,
    val profileName: String,
    val pityCount: Int,
    val lastUpdatedAt: Instant
)

fun UserPityCounter.toInternalDto(): UserPityCounterInternalDto {
    return UserPityCounterInternalDto(
        id = this.id.value,
        userId = this.userId.value,
        profileName = this.profileName,
        pityCount = this.pityCount,
        lastUpdatedAt = this.updatedAt
    )
}
