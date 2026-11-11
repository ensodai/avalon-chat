package com.ensodai.avalon.data.db.models.message

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.avalon.data.db.models.chat.ChatVariantEntity
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Entity(
    tableName = "message_slots",
    foreignKeys = [
        ForeignKey(
            entity = ChatVariantEntity::class,
            parentColumns = ["id"],
            childColumns = ["chat_variant_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("chat_variant_id"),
        Index("created_at")
    ]
)
data class MessageSlotEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Uuid,
    @ColumnInfo(name = "sync_action")
    val syncAction: SyncAction,
    @ColumnInfo(name = "chat_variant_id")
    val chatVariantId: Uuid,
    @ColumnInfo(name = "active_container_id")
    val activeContainerId: Uuid?,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant
)
