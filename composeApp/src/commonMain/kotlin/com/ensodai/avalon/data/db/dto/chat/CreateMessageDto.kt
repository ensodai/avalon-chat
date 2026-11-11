package com.ensodai.avalon.data.db.dto.chat

import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.shared.avalon.chat.models.MessageStatus
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class CreateSlotDto(
    val sessionId: Uuid,
    val variantId: Uuid,
    val slotId: Uuid,
    val activeContainerId: Uuid?,
    val syncAction: SyncAction,
    val createdAt: Instant,
    val updatedAt: Instant,
    val containers: List<CreateContainerDto>,
    val characterId: Uuid
)

data class CreateContainerDto(
    val id: Uuid,
    val role: MessageRole,
    val rarity: Rarity,
    val status: MessageStatus,
    val syncAction: SyncAction,
    val userReaction: String?,
    val characterReaction: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val contents: List<CreateContentDto>
)

data class CreateContentDto(
    val id: Uuid,
    val type: AvalonMessageContentType,
    val content: String,
    val orderIndex: Int
)