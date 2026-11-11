package com.ensodai.avalon.data.db.models

import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class ChatSessionPreview(
    val sessionId: Uuid,
    val characterId: Uuid,
    val characterName: String,
    val characterAvatarUrl: String?,
    val lastMessageContent: String?,
    val lastMessageType: AvalonMessageContentType?,
    val lastMessageTime: Instant?,
    val updatedAt: Instant?,
    val isArchived: Boolean,
    val draftMessage: String?,
    val unreadCount: Int
)