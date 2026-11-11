package app.data.db.tables.images

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import app.data.db.tables.aiCharacter.AICharacters
import app.utils.toServerUrl
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.uuid.Uuid

object WelcomeCardImages : BaseUuidTable("welcome_card_images") {
    val characterPublicId = reference("character_public_id", AICharacters, onDelete = ReferenceOption.CASCADE)
    val imagePath = varchar("image_path", 255)
    val thumbHash = varchar("thumb_hash", 255).nullable()
    val orderIndex = integer("order_index").nullable()

    init {
        // Составной индекс для сортировки
        index("idx_welcome_images_ordered", isUnique = false, characterPublicId, orderIndex)
    }
}

class WelcomeCardImage(id: EntityID<Uuid>) : BaseUuidEntity(id, WelcomeCardImages) {
    companion object : BaseUuidEntityClass<WelcomeCardImage>(WelcomeCardImages)

    var characterPublicId: EntityID<Uuid> by WelcomeCardImages.characterPublicId
    var imagePath: String by WelcomeCardImages.imagePath
    var thumbHash: String? by WelcomeCardImages.thumbHash
    var orderIndex: Int? by WelcomeCardImages.orderIndex
}

data class WelcomeCardImageInternalDto(
    val id: Uuid,
    val characterId: Uuid,
    val imagePath: String,
    val thumbHash: String?,
    val orderIndex: Int?
)

fun WelcomeCardImage.toInternalDto(): WelcomeCardImageInternalDto {
    return WelcomeCardImageInternalDto(
        id = this.id.value,
        characterId = this.characterPublicId.value,
        imagePath = this.imagePath,
        thumbHash = this.thumbHash,
        orderIndex = this.orderIndex
    )
}

fun WelcomeCardImage.toResponse(): com.ensodai.shared.avalon.character.models.WelcomeCardImageResponse {
    return com.ensodai.shared.avalon.character.models.WelcomeCardImageResponse(
        id = this.id.value,
        imageUrl = this.imagePath.toServerUrl(),
        thumbHash = this.thumbHash
    )
}
