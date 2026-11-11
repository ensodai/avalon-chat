package app.service.chat

import com.ensodai.shared.avalon.chat.wss.ServerMessage
import com.ensodai.shared.avalon.chat.models.Rarity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

interface TypingLease {
    suspend fun release()
}

@Single
class TypingIndicatorService(
    private val messageNotifier: MessageNotifier
) {
    private val activeTypingStates = mutableMapOf<Uuid, TypingState>()
    private val heartbeatJobs = mutableMapOf<Uuid, Job>()
    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val HEARTBEAT_INTERVAL_MS = 1500L
    }

    private data class TypingState(
        val chatId: Uuid,
        val rarity: Rarity,
        var refCount: Int
    )

    suspend fun acquire(
        userId: Uuid,
        chatSessionId: Uuid,
        rarity: Rarity
    ): TypingLease {
        mutex.withLock {
            val currentState = activeTypingStates[userId]
            if (currentState != null) {
                currentState.refCount++
                return TypingLeaseImpl(userId, this)
            }

            val newState = TypingState(chatSessionId, rarity, 1)
            activeTypingStates[userId] = newState

            // Запускаем heartbeat-цикл: шлем ивент каждые 1.5 сек
            val job = scope.launch {
                while (isActive) {
                    messageNotifier.notifyUserWithTyping(
                        userId = userId,
                        chatId = chatSessionId,
                        isTyping = true,
                        rarity = rarity
                    )
                    delay(HEARTBEAT_INTERVAL_MS)
                }
            }
            heartbeatJobs[userId] = job

            return TypingLeaseImpl(userId, this)
        }
    }

    suspend fun release(userId: Uuid) {
        mutex.withLock {
            val state = activeTypingStates[userId] ?: return
            state.refCount--

            if (state.refCount <= 0) {
                // Останавливаем heartbeat
                heartbeatJobs.remove(userId)?.cancel()
                activeTypingStates.remove(userId)

                // Отправляем финальный ивент об окончании тайпинга
                withContext(NonCancellable) {
                    messageNotifier.notifyUserWithTyping(
                        userId = userId,
                        chatId = state.chatId,
                        isTyping = false,
                        rarity = state.rarity
                    )
                }
            }
        }
    }

    private class TypingLeaseImpl(
        private val userId: Uuid,
        private val service: TypingIndicatorService
    ) : TypingLease {
        override suspend fun release() {
            service.release(userId)
        }
    }
}
