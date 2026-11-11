package com.ensodai.avalon.domain.sessionWorker

import com.ensodai.avalon.data.repository.local.DatastoreManager
import com.ensodai.avalon.domain.auth.AuthSessionManager
import com.ensodai.avalon.di.ApplicationScope
import com.ensodai.avalon.di.RpcConnectionManager
import com.ensodai.avalon.domain.paymentManager.PaymentManager
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import com.ensodai.avalon.domain.chatEvents.ChatEventHandler
import com.ensodai.avalon.domain.chatEvents.RpcConnectionState
import com.ensodai.avalon.utils.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.concurrent.Volatile

@Single
class SessionWorker(
    private val authSessionManager: AuthSessionManager,
    private val chatSyncRpcClient: ChatSyncRpcClient,
    private val eventHandler: ChatEventHandler,
    private val syncManager: SyncManager,
    private val datastoreManager: DatastoreManager,
    private val pendingChangesSyncManager: PendingChangesSyncManager,
    private val messageQueueRestorer: MessageQueueRestorer,
    private val paymentManager: PaymentManager,
    private val rpcConnectionManager: RpcConnectionManager,
    @ApplicationScope private val scope: CoroutineScope,
    private val logger: AppLogger
) {

    @Volatile
    private var isAuthorized = false
    private var reconnectJob: Job? = null

    fun onAppForeground() {
        if (!isAuthorized) return
        logger.d("SessionWorker", "📱 App entered FOREGROUND. Checking RPC connection...")
        rpcConnectionManager.onAppForeground()
    }

    fun onNetworkAvailable() {
        if (!isAuthorized) return
        
        logger.d("SessionWorker", "📶 Internet Available! Notifying RPC Connection Manager...")
        rpcConnectionManager.onNetworkAvailable()

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(500)
            // Инициируем только синхронизацию, коннекты восстановятся сами
            pendingChangesSyncManager.scheduleSync()
        }
    }

    fun start() {
        scope.launch {
            authSessionManager
                .isAuthorized
                .collectLatest { authState ->
                    if (authState == true) {
                        isAuthorized = true
                        startAuthorizedWork()
                    } else if (authState == false) {
                        isAuthorized = false
                        stopAuthorizedWork()
                    }
                }
        }
    }

    private suspend fun startAuthorizedWork() {
        logger.d("SessionWorker", "✅ User authorized. Starting services...")

        // 0. Запускаем удержание постоянного сокета
        rpcConnectionManager.start()

        // 1. ChatEventHandler инициализируется через Koin, подписка идет в init {}

        // 2. Мы только что загрузились. Память чиста.
        // Достаем из базы недоотправленные ивенты и кладем в очередь сокета.
        messageQueueRestorer.restoreQueue()

        // 3. Инициализируем покупки, восстановление и предзагружаем тарифы (временно отключено до лучших времен)
        // paymentManager.initialize()

        // 4. Слушаем состояние коннекта для PULL/PUSH синхронизации
        chatSyncRpcClient
            .isChannelReady
            .collectLatest { isReady ->
                if (isReady) {
                    logger.d("SessionWorker", "📡 RPC Channel Ready -> Triggering Sync")
                    syncManager.performCatchUpSync()
                    pendingChangesSyncManager.scheduleSync()
                } else {
                    logger.d(
                        "SessionWorker",
                        "📡 RPC Channel paused. State=${chatSyncRpcClient.connectionState.value}"
                    )
                }
            }
    }

    private fun stopAuthorizedWork() {
        logger.d("SessionWorker", "🛑 User unauthorized. Stopping services...")
        rpcConnectionManager.disconnect()
    }
}

