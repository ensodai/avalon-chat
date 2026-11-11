package com.ensodai.avalon.data.db.dto

import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class ChatPreviewMessageDto(
    val id: Uuid,
    val type: AvalonMessageContentType,
    val content: String,
    val role: MessageRole,
    val createdAt: Instant
)