package app.data.db.tables.chat

import app.core.BaseUuidEntity
import app.core.BaseUuidEntityClass
import app.core.BaseUuidTable
import app.data.db.tables.chatSessions.ChatVariant
import app.data.db.tables.chatSessions.ChatVariants
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import kotlin.time.Instant
import kotlin.uuid.Uuid

object MessageSlots : BaseUuidTable("message_slots") {
    val chatVariantId = reference("chat_variant_id", ChatVariants, onDelete = ReferenceOption.CASCADE)
    val activeMessageId = reference("active_message_id", MessageContainers, onDelete = ReferenceOption.SET_NULL).nullable()
    val isArchived = bool("is_archived").default(false)

    init {
        // Позволяет молниеносно делать: select * where variant_id = X order by created_at desc limit 50
        index("idx_slots_history_loading", isUnique = false, chatVariantId, createdAt)
        index("idx_slots_active_msg", isUnique = false, activeMessageId)
        // 2. Для быстрой синхронизации изменений (Delta Sync)
        // Позволяет молниеносно найти: WHERE variant_id = X AND updated_at > LastSyncTime
        index("idx_slots_sync", isUnique = false, chatVariantId, updatedAt)
        // 3. Для поиска удаленных слотов (Soft Delete Sync)
        // Используется в MessageSlotSyncService: WHERE deleted_at > since
        index("idx_slots_deleted_sync", isUnique = false, deletedAt)
        // Индекс для быстрого поиска активных сообщений: "Дай мне последние 50, где is_archived = false"
        index("idx_slots_active_context", isUnique = false, chatVariantId, isArchived, createdAt)
    }
}

class MessageSlot(id: EntityID<Uuid>) : BaseUuidEntity(id, MessageSlots) {
    companion object : BaseUuidEntityClass<MessageSlot>(MessageSlots)

    var chatVariant by ChatVariant referencedOn MessageSlots.chatVariantId
    var activeMessageContainer by MessageContainer optionalReferencedOn MessageSlots.activeMessageId
    val allMessages by MessageContainer referrersOn MessageContainers.slotId
}

// DTO №1: "ПОЛНЫЙ".
data class MessageSlotInternalDto(
    val id: Uuid,
    val chatVariantId: String,
    val activeMessageId: String?,
    val messages: List<MessageContainerInternalDto>,
    val createdAt: Instant,
    val updatedAt: Instant
)

// DTO №2: "ПРОСТОЙ".
data class MessageSlotSimpleInternalDto(
    val id: Uuid,
    val chatVariantId: Uuid,
    val activeMessageId: Uuid?,
    val createdAt: Instant,
    val updatedAt: Instant
)

fun MessageSlot.toFullInternalDto(): MessageSlotInternalDto {
    return MessageSlotInternalDto(
        id = this.id.value,
        chatVariantId = this.chatVariant.id.value.toString(),
        activeMessageId = this.activeMessageContainer?.id?.value?.toString(),
        messages = this.allMessages.map { it.toInternalDto() },
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun MessageSlot.toSimpleInternalDto(): MessageSlotSimpleInternalDto {
    return MessageSlotSimpleInternalDto(
        id = this.id.value,
        chatVariantId = this.chatVariant.id.value,
        activeMessageId = this.activeMessageContainer?.id?.value,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
