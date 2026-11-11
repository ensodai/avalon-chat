package app.data.db.tables.images

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import app.data.db.tables.chatSessions.ChatVariants
import app.utils.toServerUrl
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.uuid.Uuid

object ChatVariantImages : BaseUuidTable("chat_variant_images") {
    val chatVariantsId = reference("chat_variants_id", ChatVariants, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    val imagePath = varchar("image_path", 255)
    val thumbHash = varchar("thumb_hash", 255).nullable()
}
class ChatVariantImage(id: EntityID<Uuid>) : BaseUuidEntity(id, ChatVariantImages) {
    companion object : BaseUuidEntityClass<ChatVariantImage>(ChatVariantImages)

    var chatVariantsId: EntityID<Uuid> by ChatVariantImages.chatVariantsId
    var imagePath: String by ChatVariantImages.imagePath
    var thumbHash: String? by ChatVariantImages.thumbHash
}

data class ChatVariantImageInternalDto(
    val id: Uuid,
    val chatVariantId: Uuid,
    val imagePath: String,
    val thumbHash: String?
)

fun ChatVariantImage.toInternalDto(): ChatVariantImageInternalDto {
    return ChatVariantImageInternalDto(
        id = this.id.value,
        chatVariantId = this.chatVariantsId.value,
        imagePath = this.imagePath,
        thumbHash = this.thumbHash
    )
}

fun ChatVariantImage.toResponse(): com.ensodai.shared.avalon.chat.models.ChatVariantImageResponse {
    return com.ensodai.shared.avalon.chat.models.ChatVariantImageResponse(
        id = this.id.value,
        imageUrl = this.imagePath.toServerUrl(),
        thumbHash = this.thumbHash
    )
}
