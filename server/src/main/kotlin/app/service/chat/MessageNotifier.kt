package app.service.chat

import app.apiv4.chat.ChatEventBus

import com.ensodai.shared.avalon.chat.wss.ServerMessage
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

/**
 * Рассылает уведомления пользователям через новую RPC-шину.
 */
@Single
class MessageNotifier(
    private val chatEventBus: ChatEventBus
) {
    suspend fun notifyUserWithMessage(
        userId: Uuid,
        message: ServerMessage,
        excludeSessionId: Uuid? = null
    ) {
        // Пушим в новую RPC-шину для V4 клиентов
        chatEventBus.emit(userId, message)
    }

    suspend fun notifyUserWithError(userId: Uuid, error: String) {
        notifyUserWithMessage(userId, ServerMessage.Error(error), null)
    }

    suspend fun notifyUserWithTyping(
        userId: Uuid,
        chatId: Uuid,
        isTyping: Boolean,
        rarity: com.ensodai.shared.avalon.chat.models.Rarity
    ) {
        chatEventBus.emitTyping(
            userId = userId,
            event = com.ensodai.shared.avalon.chat.models.TypingState(
                chatId = chatId,
                isTyping = isTyping,
                rarity = rarity
            )
        )
    }
}
