package app.data.db.tables.images

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import app.data.db.tables.aiCharacter.AICharacters
import app.utils.toServerUrl
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.uuid.Uuid

object CharacterAvatars : BaseUuidTable("character_avatars") {
    val characterPublicId = reference("character_public_id", AICharacters, onDelete = ReferenceOption.CASCADE)
    val imagePath = varchar("image_path", 255)
    val thumbHash = varchar("thumb_hash", 255).nullable()

    init {
        index("idx_avatars_lookup", isUnique = false, characterPublicId)
    }
}

class CharacterAvatar(id: EntityID<Uuid>) : BaseUuidEntity(id, CharacterAvatars) {
    companion object : BaseUuidEntityClass<CharacterAvatar>(CharacterAvatars)

    var characterPublicId: EntityID<Uuid> by CharacterAvatars.characterPublicId
    var imagePath: String by CharacterAvatars.imagePath
    var thumbHash: String? by CharacterAvatars.thumbHash
}

data class CharacterAvatarInternalDto(
    val id: Uuid,
    val characterId: Uuid,
    val imagePath: String,
    val thumbHash: String?
)

fun CharacterAvatar.toInternalDto(): CharacterAvatarInternalDto {
    return CharacterAvatarInternalDto(
        id = this.id.value,
        characterId = this.characterPublicId.value,
        imagePath = this.imagePath,
        thumbHash = this.thumbHash
    )
}

fun CharacterAvatar.toResponse(): com.ensodai.shared.avalon.character.models.CharacterAvatarResponse {
    return com.ensodai.shared.avalon.character.models.CharacterAvatarResponse(
        id = this.id.value,
        imageUrl = this.imagePath.toServerUrl(),
        thumbHash = this.thumbHash
    )
}
