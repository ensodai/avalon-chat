package com.ensodai.avalon.domain.models.message

import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.MessageStatus
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class MessageVariantUi(
    val containerId: Uuid,
    val contentType: AvalonMessageContentType,
    val text: String,
    val role: MessageRole,
    val status: MessageStatus,
    val createdAt: Instant
)