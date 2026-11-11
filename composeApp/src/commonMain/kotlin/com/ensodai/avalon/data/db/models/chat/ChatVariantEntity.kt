package com.ensodai.avalon.data.db.models.chat

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.ensodai.avalon.data.db.models.SyncAction
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Entity(
    tableName = "chat_variant_entity",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["chat_session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chat_session_id")]
)
data class ChatVariantEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Uuid,
    @ColumnInfo(name = "sync_state")
    val syncAction: SyncAction,
    @ColumnInfo(name = "chat_session_id")
    val chatSessionId: Uuid,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "unread_count")
    val unreadCount: Int = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant
)
