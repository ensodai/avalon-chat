package com.ensodai.avalon.domain.sessionWorker

import com.ensodai.avalon.di.ApplicationScope
import com.ensodai.avalon.domain.repository.local.MessageQueueRestorerRepository
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import com.ensodai.avalon.utils.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class MessageQueueRestorer(
    private val messageQueueRestorerRepository: MessageQueueRestorerRepository,
    private val chatSyncRpcClient: ChatSyncRpcClient,
    @ApplicationScope private val scope: CoroutineScope,
    private val logger: AppLogger
) {

    /**
     * Вызывается при появлении сети (или старте приложения).
     * Вычитывает из базы все действия, которые "застряли",
     * и кидает их в RPC-очередь.
     */
    fun restoreQueue() {
        scope.launch {
            try {

                val deletedCount = messageQueueRestorerRepository.clearLocalOnlyDeletes()

                if (deletedCount > 0) {
                    logger.i("MessageQueueRestorer", "🧹 Cleared $deletedCount local only deletes")
                }

                val pendingActions = messageQueueRestorerRepository.getAllPendingEvents()

                if (pendingActions.isEmpty()) {
                    logger.d("MessageQueueRestorer", "Queue is clean. No pending actions.")
                    return@launch
                }

                logger.i(
                    "MessageQueueRestorer",
                    "🚀 Restoring ${pendingActions.size} pending actions..."
                )

                pendingActions.forEach { action ->
                    logger.d("MessageQueueRestorer", "Restoring action: ${action::class.simpleName}")
                    chatSyncRpcClient.enqueueActionDirectly(action)
                }

            } catch (e: Exception) {
                logger.e(
                    "MessageQueueRestorer",
                    "Failed to restore action queue: ${e.stackTraceToString()}"
                )
            }
        }
    }
}
