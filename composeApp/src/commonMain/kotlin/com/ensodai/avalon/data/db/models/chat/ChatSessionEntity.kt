package com.ensodai.avalon.data.db.models.chat

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.avalon.data.db.models.character.CharacterEntity
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Entity(
    tableName = "chat_session_entity",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["character_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ChatVariantEntity::class,
            parentColumns = ["id"],
            childColumns = ["active_chat_variant_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["character_id"], unique = true), Index("active_chat_variant_id")]
)
data class ChatSessionEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: Uuid,
    @ColumnInfo(name = "character_id")
    val characterId: Uuid,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant?,
    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean,
    @ColumnInfo(name = "sync_state")
    val syncAction: SyncAction,
    @ColumnInfo(name = "active_chat_variant_id")
    val activeChatVariantId: Uuid?,
    @ColumnInfo(name = "last_message_content")
    val lastMessageContent: String?,
    @ColumnInfo(name = "last_message_type")
    val lastMessageType: AvalonMessageContentType?,
    @ColumnInfo(name = "last_message_role")
    val lastMessageRole: MessageRole? = null,
    @ColumnInfo(name = "last_message_time")
    val lastMessageTime: Instant?,
    @ColumnInfo(name = "selected_llm_config_code_name")
    val selectedLlmConfigCodeName: String? = null,
    @ColumnInfo(name = "draft_message")
    val draftMessage: String? = null
)
