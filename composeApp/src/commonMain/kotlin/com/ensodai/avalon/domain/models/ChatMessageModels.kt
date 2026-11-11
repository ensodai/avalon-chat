package com.ensodai.avalon.domain.models

import androidx.compose.runtime.Immutable
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.MessagePayload
import com.ensodai.shared.avalon.chat.models.MessageStatus
import com.ensodai.shared.avalon.chat.models.Rarity
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * 1. Верхний уровень: СООБЩЕНИЕ В ЧАТЕ (Слот).
 * Это то, что видит юзер как "Один пузырь" (или группа пузырей, если есть варианты).
 */
@Immutable
data class ChatMessageModel(
    val slotId: Uuid,
    val createdAt: Instant,
    val containers: List<MessageContainerModel>,
    val activeContainerId: Uuid?
) {
    val activeContainer: MessageContainerModel?
        get() = containers.find { it.id == activeContainerId } ?: containers.firstOrNull()
}

/**
 * 2. Средний уровень: КОНТЕЙНЕР (Вариант сообщения).
 * Хранит статус, роль и реакции.
 */
@Immutable
data class MessageContainerModel(
    val id: Uuid,
    val slotId: Uuid,
    val rarity: Rarity,
    val role: MessageRole,
    val status: MessageStatus,
    val createdAt: Instant,
    val userReaction: String?,
    val characterReaction: String?,
    val isRead: Boolean = false,
    val showOutreachFeedback: Boolean = false,
    val content: List<MessageContentModel>,
    val isStreaming: Boolean = false
)

/**
 * 3. Нижний уровень: КОНТЕНТ (Кусочки).
 */
@Immutable
data class MessageContentModel(
    val id: Uuid,
    val type: AvalonMessageContentType,
    val payload: MessagePayload
)