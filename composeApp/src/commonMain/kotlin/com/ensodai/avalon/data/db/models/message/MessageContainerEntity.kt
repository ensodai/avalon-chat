package com.ensodai.avalon.data.db.models.message

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.shared.avalon.chat.models.MessageStatus
import com.ensodai.shared.avalon.chat.models.MessageRole
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Entity(
    tableName = "message_containers",
    foreignKeys = [
        ForeignKey(
            entity = MessageSlotEntity::class,
            parentColumns = ["id"],
            childColumns = ["slot_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("slot_id"),
        // Индекс по статусу нужен для MessageQueueRestorer (найти все PENDING)
        // Но так как status теперь сохраняется через TypeConverter как строка,
        // индекс будет работать корректно.
        Index("status"),
        Index("sync_action") // Для поиска неотправленных через REST
    ]
)
data class MessageContainerEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Uuid,
    @ColumnInfo(name = "slot_id")
    val slotId: Uuid,
    @ColumnInfo(name = "role")
    val role: MessageRole,
    @ColumnInfo(name = "rarity")
    val rarity: Rarity,
    @ColumnInfo(name = "status")
    val status: MessageStatus,
    // При создании = CREATE. После ACK от сервера = NONE.
    @ColumnInfo(name = "sync_action")
    val syncAction: SyncAction,
    @ColumnInfo(name = "user_reaction")
    val userReaction: String? = null,
    @ColumnInfo(name = "character_reaction")
    val characterReaction: String? = null,
    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,
    @ColumnInfo(name = "show_outreach_feedback")
    val showOutreachFeedback: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant
)
