package app.data.db.tables.images

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import app.data.db.tables.aiCharacter.AICharacters
import app.utils.toServerUrl
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.uuid.Uuid

object CharacterImages : BaseUuidTable("character_images") {
    val characterPublicId = reference("character_public_id", AICharacters, onDelete = ReferenceOption.CASCADE)
    val imagePath = varchar("image_path", 255)
    val thumbHash = varchar("thumb_hash", 255).nullable()
    val orderIndex = integer("order_index").nullable()
    val isPrivate = bool("is_private").default(false)
    val energyCost = integer("energy_cost").default(0)
    val requiredLevel = integer("required_level").nullable()

    init {
        index("idx_character_images_ordered", isUnique = false, characterPublicId, orderIndex)
    }
}

class CharacterImage(id: EntityID<Uuid>) : BaseUuidEntity(id, CharacterImages) {
    companion object : BaseUuidEntityClass<CharacterImage>(CharacterImages)

    var characterPublicId by CharacterImages.characterPublicId
    var imagePath by CharacterImages.imagePath
    var thumbHash: String? by CharacterImages.thumbHash
    var orderIndex by CharacterImages.orderIndex
    var isPrivate by CharacterImages.isPrivate
    var energyCost by CharacterImages.energyCost
    var requiredLevel by CharacterImages.requiredLevel
}

data class CharacterImageInternalDto(
    val id: Uuid,
    val characterId: Uuid,
    val imagePath: String,
    val thumbHash: String?,
    val orderIndex: Int?,
    val isPrivate: Boolean,
    val energyCost: Int,
    val requiredLevel: Int?
)

fun CharacterImage.toInternalDto(): CharacterImageInternalDto {
    return CharacterImageInternalDto(
        id = this.id.value,
        characterId = this.characterPublicId.value,
        imagePath = this.imagePath,
        thumbHash = this.thumbHash,
        orderIndex = this.orderIndex,
        isPrivate = this.isPrivate,
        energyCost = this.energyCost,
        requiredLevel = this.requiredLevel
    )
}

fun CharacterImage.toResponse(isUnlocked: Boolean = true): com.ensodai.shared.avalon.character.models.CharacterImageResponse {
    return com.ensodai.shared.avalon.character.models.CharacterImageResponse(
        id = this.id.value,
        imageUrl = if (isUnlocked) this.imagePath.toServerUrl() else "",
        thumbHash = this.thumbHash,
        isPrivate = this.isPrivate,
        energyCost = this.energyCost,
        requiredLevel = this.requiredLevel,
        isUnlocked = isUnlocked
    )
}
