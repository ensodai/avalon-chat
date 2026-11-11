package app.domain.models

import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import kotlin.uuid.Uuid

/**
 * Представляет одну часть сообщения (текст, картинка, стикер).
 */
data class DomainMessageContent(
    val type: AvalonMessageContentType, // Будет "TEXT", "IMAGE", "STICKER_GIFT"
    val content: String // Сам текст или путь/URL к картинке
)

/**
 * Представляет полное сообщение (контейнер) для отображения в UI.
 * Состоит из роли и списка частей контента.
 */
data class DomainMessage(
    val id: Uuid,
    val role: MessageRole,
    val contents: List<DomainMessageContent>,
    val userReaction: String? = null,
    val characterReaction: String? = null
)

data class MessageHistoryResponse(
    val messages: List<DomainMessage>,
    val totalCount: Long // Общее количество сообщений до лимита
)