package app.data.db.tables.chatSessions

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import app.data.db.tables.images.ChatVariantImage
import app.data.db.tables.images.ChatVariantImages
import app.data.db.tables.images.toResponse

import app.data.db.tables.chat.MessageSlot
import app.data.db.tables.chat.MessageSlots
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.time.Instant
import kotlin.uuid.Uuid

object ChatVariants : BaseUuidTable("chat_variants") {
    val name = varchar("name", 255)
    val chatSessionId = reference("chat_session_id", ChatSessions, onDelete = ReferenceOption.CASCADE)
    val unreadCount = integer("unread_count").default(0)

    init {
        // Он покрывает и поиск по ID сессии, и сортировку, и синк.
        index("idx_variants_session_lookup", isUnique = false, chatSessionId, updatedAt)
    }
}

class ChatVariant(id: EntityID<Uuid>) : BaseUuidEntity(id, ChatVariants) {
    companion object : BaseUuidEntityClass<ChatVariant>(ChatVariants)

    var chatSession by ChatSession referencedOn ChatVariants.chatSessionId
    var name by ChatVariants.name
    val slots by MessageSlot referrersOn MessageSlots.chatVariantId
    val chatVariantImage by ChatVariantImage optionalBackReferencedOn ChatVariantImages.chatVariantsId
    var unreadCount by ChatVariants.unreadCount
}

data class ChatVariantInternalDto(
    val id: Uuid,
    val chatSessionId: Uuid,
    val name: String,
    val unreadCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant
)

// ВАЖНО: Вызывать, когда `chatSession` и `storyImage` загружены через .with()
fun ChatVariant.toInternalDto(): ChatVariantInternalDto {
    return ChatVariantInternalDto(
        id = this.id.value,
        chatSessionId = this.chatSession.id.value,
        name = this.name,
        unreadCount = this.unreadCount,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun ChatVariant.toResponse(): com.ensodai.shared.avalon.chat.models.ChatVariantResponse {
    return com.ensodai.shared.avalon.chat.models.ChatVariantResponse(
        id = this.id.value,
        name = this.name,
        image = this.chatVariantImage?.toResponse(),
        sessionId = this.chatSession.id.value,
        unreadCount = this.unreadCount,
        updatedAt = this.updatedAt,
        createdAt = this.createdAt
    )
}
