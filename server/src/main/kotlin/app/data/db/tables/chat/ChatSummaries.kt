package app.data.db.tables.chat

import app.core.BaseUuidTable
import app.data.db.tables.chatSessions.ChatVariants
import org.jetbrains.exposed.v1.core.ReferenceOption

object ChatSummaries : BaseUuidTable("chat_summaries") {
    val chatVariantId = reference("chat_variant_id", ChatVariants, onDelete = ReferenceOption.CASCADE)
    val content = text("content")
    val lastIncludedSlotId = reference("last_included_slot_id", MessageSlots, onDelete = ReferenceOption.SET_NULL).nullable()

    init {
        index("idx_chat_summaries_last", isUnique = false, chatVariantId, createdAt)
    }
}
