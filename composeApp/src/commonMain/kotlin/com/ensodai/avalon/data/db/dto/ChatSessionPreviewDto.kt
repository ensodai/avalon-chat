package com.ensodai.avalon.data.db.dto

import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class ChatSessionPreviewDto(
    val id: Uuid,
    val characterId: Uuid,
    val activeChatVariantId: Uuid?,
    val isArchived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastMessage: ChatPreviewMessageDto?
)