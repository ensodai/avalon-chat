package app.data.db.tables.images

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import app.data.db.tables.users.AppUsers
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.uuid.Uuid

object UnlockedCharacterImages : BaseUuidTable("unlocked_character_images") {
    val userId = reference("user_id", AppUsers, onDelete = ReferenceOption.CASCADE)
    val characterImageId = reference("character_image_id", CharacterImages, onDelete = ReferenceOption.CASCADE)
    val unlockedAt = timestamp("unlocked_at").defaultExpression(CurrentTimestamp)

    init {
        uniqueIndex("unique_unlocked_character_image_idx", userId, characterImageId)
    }
}

class UnlockedCharacterImage(id: EntityID<Uuid>) : BaseUuidEntity(id, UnlockedCharacterImages) {
    companion object : BaseUuidEntityClass<UnlockedCharacterImage>(UnlockedCharacterImages)

    var userId by UnlockedCharacterImages.userId
    var characterImageId by UnlockedCharacterImages.characterImageId
    var unlockedAt by UnlockedCharacterImages.unlockedAt
}
