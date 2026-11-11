package app.data.db.tables.chat

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.Rarity
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.time.Instant
import kotlin.uuid.Uuid

import com.ensodai.shared.avalon.chat.models.SystemEventType

object MessageContainers : BaseUuidTable("message_containers") {
    val slotId = reference("slot_id", MessageSlots, onDelete = ReferenceOption.CASCADE)
    val role = enumerationByName("role", 10, MessageRole::class)
    val messageTokens = integer("message_tokens").nullable()
    val rarity = enumerationByName("rarity", 20, Rarity::class).default(Rarity.COMMON)
    // Храним сам смайлик (например: "❤️", "🔥", "👍") или null, если реакции нет.
    val userReaction = varchar("user_reaction", 16).nullable()
    val characterReaction = varchar("character_reaction", 16).nullable()

    init {
        index("idx_messages_slot_lookup", isUnique = false, slotId)
    }
}

class MessageContainer(id: EntityID<Uuid>) : BaseUuidEntity(id, MessageContainers) {
    companion object : BaseUuidEntityClass<MessageContainer>(MessageContainers)

    var slot by MessageSlot referencedOn MessageContainers.slotId
    var role by MessageContainers.role
    var messageTokens by MessageContainers.messageTokens
    var rarity by MessageContainers.rarity
    val contents by MessageContent referrersOn MessageContents.containerId
}

data class MessageContainerInternalDto(
    val id: Uuid,
    val slotId: Uuid,
    val role: MessageRole,
    val rarity: Rarity,
    val contents: List<MessageContentInternalDto>,
    val createdAt: Instant
)

fun MessageContainer.toInternalDto(): MessageContainerInternalDto {
    return MessageContainerInternalDto(
        id = this.id.value,
        slotId = this.slot.id.value,
        role = this.role,
        rarity = this.rarity,
        contents = this.contents.map { it.toInternalDto() },
        createdAt = this.createdAt
    )
}
