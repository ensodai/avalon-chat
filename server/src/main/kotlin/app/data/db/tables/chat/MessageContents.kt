package app.data.db.tables.chat

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.time.Instant
import kotlin.uuid.Uuid

object MessageContents : BaseUuidTable("message_contents") {
    val containerId = reference("container_id", MessageContainers, onDelete = ReferenceOption.CASCADE)
    val type = enumerationByName("type", 50, AvalonMessageContentType::class)
    val content = text("content")
    val orderIndex = integer("order_index").default(0)


    init {
        index("idx_attachments_lookup_ordered", isUnique = false, containerId, orderIndex)
        index("idx_message_contents_images_lookup", isUnique = false, containerId, type)
    }
}

class MessageContent(id: EntityID<Uuid>) : BaseUuidEntity(id, MessageContents) {
    companion object : BaseUuidEntityClass<MessageContent>(MessageContents)

    var messageContainer by MessageContainer referencedOn MessageContents.containerId
    var type by MessageContents.type
    var content by MessageContents.content
    var orderIndex by MessageContents.orderIndex
}

data class MessageContentInternalDto(
    val id: Uuid,
    val type: AvalonMessageContentType,
    val content: String,
    val orderIndex: Int,
    val createdAt: Instant
)

fun MessageContent.toInternalDto(): MessageContentInternalDto {
    return MessageContentInternalDto(
        id = this.id.value,
        type = this.type,
        content = this.content,
        orderIndex = this.orderIndex,
        createdAt = this.createdAt
    )
}
