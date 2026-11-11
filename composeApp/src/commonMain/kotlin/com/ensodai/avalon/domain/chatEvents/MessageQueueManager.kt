package com.ensodai.avalon.domain.chatEvents

import com.ensodai.avalon.utils.L
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

@OptIn(ExperimentalAtomicApi::class)
class MessageQueueManager(
    private val messageQueueOptimizer: MessageQueueOptimizer,
    private val scope: CoroutineScope,
    private val sendAction: suspend (LocalChatAction) -> Unit,
) {
    private val queue = ArrayDeque<LocalChatAction>()
    private val mutex = Mutex() // Защищает данные очереди
    private val jobMutex = Mutex() // Защищает запуск джобы

    private val isNetworkAvailable = AtomicBoolean(false)
    private var processingJob: Job? = null

    // 🔥 КАНАЛ-ТРИГГЕР (Conflated = хранит только последний сигнал, старые не нужны)
    private val retrySignal = Channel<Unit>(Channel.CONFLATED)

    private var retryAttempt = 0

    suspend fun setNetworkAvailable(available: Boolean) {
        val wasAvailable = isNetworkAvailable.exchange(available)

        retrySignal.trySend(Unit)

        if (available && !wasAvailable) {
            ensureProcessingStarted()
        }
    }

    suspend fun enqueueAction(newAction: LocalChatAction) {
        mutex.withLock {
            messageQueueOptimizer.optimizeAndAdd(queue, newAction)
        }
        if (isNetworkAvailable.load()) {
            retrySignal.trySend(Unit)
            ensureProcessingStarted()
        }
    }

    private suspend fun ensureProcessingStarted() {
        jobMutex.withLock {
            if (processingJob?.isActive == true) return
            processingJob = scope.launch { processQueueLoop() }
        }
    }

    private suspend fun processQueueLoop() {
        while (scope.isActive) {
            if (!isNetworkAvailable.load()) break

            val action = mutex.withLock { queue.firstOrNull() }

            if (action == null) break

            try {
                sendAction(action)
                mutex.withLock { queue.removeAt(0) }
                retryAttempt = 0 // Сброс при успехе

                val pendingCount = mutex.withLock { queue.size }
                
                val delayMs = when {
                    pendingCount > 10 -> 30L
                    else -> 50L
                }
                delay(delayMs)

            } catch (e: Exception) {
                if (e is CancellationException) throw e

                if (!isNetworkAvailable.load()) {
                    L.w("RPC_Queue", "Сеть недоступна, ждем сигнала от менеджера...")
                    retrySignal.receive()
                } else {
                    retryAttempt++
                    val backoffDelay = (1000L * (1 shl (retryAttempt - 1).coerceAtMost(4))).coerceAtMost(10000L)
                    L.w("RPC_Queue", "Сеть доступна, но отправка не удалась. Пауза $backoffDelay мс перед ретраем (попытка $retryAttempt)... Ошибка: ${e.message}")
                    withTimeoutOrNull(backoffDelay) {
                        retrySignal.receive()
                    }
                }
            }
        }
    }

    suspend fun purgeMessagesForDeletedContexts(
        deletedSessionIds: List<Uuid>,
        deletedVariantIds: List<Uuid>
    ) {
        val sessionSet = deletedSessionIds.toSet()
        val variantSet = deletedVariantIds.toSet()

        if (sessionSet.isEmpty() && variantSet.isEmpty()) return

        mutex.withLock {
            val wasRemoved = queue.removeAll { action ->
                val isInDeletedSession = sessionSet.contains(action.chatSessionId)
                val isInDeletedVariant = variantSet.contains(action.chatVariantId)

                isInDeletedSession || isInDeletedVariant
            }

            if (wasRemoved) {
                L.d("MessageQueueManager", "🧹 Cleaned queue based on deleted contexts")
            }
        }
    }

    suspend fun stop() {
        jobMutex.withLock {
            processingJob?.cancel()
            processingJob = null
        }
        isNetworkAvailable.store(false)
        mutex.withLock { queue.clear() }
    }
}
