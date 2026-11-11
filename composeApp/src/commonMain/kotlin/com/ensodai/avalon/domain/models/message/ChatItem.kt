package com.ensodai.avalon.domain.models.message

import androidx.compose.runtime.Immutable
import com.ensodai.avalon.domain.models.ChatMessageModel
import com.ensodai.avalon.domain.models.chat.WelcomeInfo
import com.ensodai.shared.avalon.chat.models.MessagePayload

@Immutable
sealed interface ChatItem {
    val id: String
}

// Обычное сообщение
@Immutable
data class MessageItem(
    val slot: ChatMessageModel
) : ChatItem {
    // Используем slotId как стабильный идентификатор
    override val id: String = slot.slotId.toString()
}

// Карточка (Статичный ID)
@Immutable
data class WelcomeCardItem(
    val welcomeInfo: WelcomeInfo
) : ChatItem {
    override val id: String = "static_id_welcome_card"
}