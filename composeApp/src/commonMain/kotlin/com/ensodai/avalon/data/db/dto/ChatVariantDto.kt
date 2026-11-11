package com.ensodai.avalon.data.db.dto

import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class ChatVariantDto(
    val id: Uuid,
    val name: String,
    val image: ChatVariantImageDto?,
    val sessionId: Uuid,
    val unreadCount: Int = 0,
    val createdAt: Instant,
    val updatedAt: Instant,
)
