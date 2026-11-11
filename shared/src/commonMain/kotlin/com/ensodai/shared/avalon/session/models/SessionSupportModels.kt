@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package com.ensodai.shared.avalon.session.models

import com.ensodai.shared.avalon.chat.models.ChatUpdateItem
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class UpdateChatSessionsResponse(
    val updatedResult: Map<@Contextual Uuid, @Contextual Instant>
)

@Serializable
data class ChatSessionsBatchRequest(
    val items: List<ChatUpdateItem>
)

@Serializable
data class ChatSessionsDeleteBatchRequest(
    val sessionIds: List<@Contextual Uuid>
)

@Serializable
data class ReportRequest(
    @Contextual
    val characterId: Uuid,
    val message: String
)

@Serializable
data class ReportResponse(
    val message: String
)
