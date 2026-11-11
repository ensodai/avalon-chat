package app.apiv4.chat

import com.ensodai.shared.avalon.chat.wss.ServerMessage
import com.ensodai.shared.avalon.chat.wss.RpcStreamEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onSubscription
import java.util.concurrent.ConcurrentHashMap
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

/**
 * Единая шина событий чата для API V4.
 * Заменяет собой MessageNotifier для новой архитектуры.
 */
@Single
class ChatEventBus {
    private val onlineUsers = ConcurrentHashMap.newKeySet<Uuid>()

    fun registerOnlineUser(userId: Uuid) {
        if (onlineUsers.add(userId)) {
            app.utils.logger.WssLogger.info("[ChatEventBus] Пользователь вошел в сеть: userId=$userId. Всего онлайн: ${onlineUsers.size}")
        }
    }

    fun deregisterOnlineUser(userId: Uuid) {
        if (onlineUsers.remove(userId)) {
            app.utils.logger.WssLogger.info("[ChatEventBus] Пользователь вышел из сети: userId=$userId. Всего онлайн: ${onlineUsers.size}")
        }
    }

    fun isUserOnline(userId: Uuid): Boolean {
        return onlineUsers.contains(userId)
    }

    fun getOnlineUsers(): Set<Uuid> {
        return onlineUsers
    }

    private val _events = MutableSharedFlow<UserEvent>(extraBufferCapacity = 100)
    val events: SharedFlow<UserEvent> = _events.asSharedFlow()

    private val _typingEvents = MutableSharedFlow<UserTypingEvent>(extraBufferCapacity = 100)
    val typingEvents: SharedFlow<UserTypingEvent> = _typingEvents.asSharedFlow()

    /**
     * Отправить событие конкретному пользователю.
     */
    suspend fun emit(userId: Uuid, message: ServerMessage) {
        val subs = _events.subscriptionCount.value
        if (subs == 0) {
            app.utils.logger.WssLogger.fail("[ChatEventBus] ВНИМАНИЕ: emit без подписчиков! userId=$userId, type=${message::class.simpleName}")
        } else {
            app.utils.logger.WssLogger.info("[ChatEventBus] emit: userId=$userId, type=${message::class.simpleName}, subscribers=$subs")
        }
        _events.emit(UserEvent(userId, message))
    }

    /**
     * Отправить событие печати конкретному пользователю.
     */
    suspend fun emitTyping(userId: Uuid, event: com.ensodai.shared.avalon.chat.models.TypingState) {
        val subs = _typingEvents.subscriptionCount.value
        if (subs == 0) {
            app.utils.logger.WssLogger.fail("[ChatEventBus] ВНИМАНИЕ: emitTyping без подписчиков! userId=$userId, chatId=${event.chatId}")
        } else {
            app.utils.logger.WssLogger.info("[ChatEventBus] emitTyping: userId=$userId, chatId=${event.chatId}, isTyping=${event.isTyping}, subscribers=$subs")
        }
        _typingEvents.emit(UserTypingEvent(userId, event))
    }

    /**
     * Получить поток событий для конкретного пользователя.
     */
    fun observe(userId: Uuid): Flow<ServerMessage> = events
        .filter { it.userId == userId && !it.isReadySignal }
        .map { it.message!! }

    fun observeV2(userId: Uuid): Flow<RpcStreamEvent<ServerMessage>> = events
        .filter { it.userId == userId && !it.isReadySignal }
        .map { 
            RpcStreamEvent(payload = it.message!!)
        }
        .onStart {
            emit(RpcStreamEvent(isReadySignal = true))
        }

    /**
     * Получить поток событий печати для конкретного пользователя.
     */
    fun observeTyping(userId: Uuid): Flow<com.ensodai.shared.avalon.chat.models.TypingState> = typingEvents
        .filter { it.userId == userId && !it.isReadySignal }
        .map { it.event!! }

    fun observeTypingV2(userId: Uuid): Flow<RpcStreamEvent<com.ensodai.shared.avalon.chat.models.TypingState>> = typingEvents
        .filter { it.userId == userId && !it.isReadySignal }
        .map { 
            RpcStreamEvent(payload = it.event!!)
        }
        .onStart {
            emit(RpcStreamEvent(isReadySignal = true))
        }
}

data class UserEvent(
    val userId: Uuid,
    val message: ServerMessage? = null,
    val isReadySignal: Boolean = false
)

data class UserTypingEvent(
    val userId: Uuid,
    val event: com.ensodai.shared.avalon.chat.models.TypingState? = null,
    val isReadySignal: Boolean = false
)
