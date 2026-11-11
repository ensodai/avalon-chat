package com.ensodai.avalon.di

import com.ensodai.shared.avalon.rpc.services.AuthRpcService
import com.ensodai.avalon.data.repository.local.DatastoreManager
import com.ensodai.avalon.domain.chatEvents.RpcConnectionState
import com.ensodai.avalon.utils.L
import io.ktor.client.HttpClient
import io.ktor.http.path
import io.ktor.http.takeFrom
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.rpc.RpcClient
import kotlinx.rpc.krpc.client.KrpcClient
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.ktor.client.rpcConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import kotlinx.serialization.json.Json

/**
 * Единый реактивный менеджер и стейт-машина постоянного RPC/WebSocket соединения.
 * Обеспечивает:
 * 1. Потокобезопасное получение и создание активного WebSocket соединения.
 * 2. Автоматическую аутентификацию при наличии токена и поддержку гостевого режима.
 * 3. Мгновенное (0 мс) переподключение при разворачивании приложения и появлении сети.
 * 4. Защиту от зомби-сокетов и каскадов отмен.
 */
open class RpcConnectionManager(
    @KtorHttpClient private val httpClient: HttpClient,
    private val datastoreManager: DatastoreManager,
    @ApplicationScope private val scope: CoroutineScope,
    private val json: Json
) {
    private var rpcClient: RpcClient? = null
    private val mutex = Mutex()

    protected val _connectionState = MutableStateFlow<RpcConnectionState>(RpcConnectionState.Idle)
    open val connectionState: StateFlow<RpcConnectionState> = _connectionState.asStateFlow()

    val reconnectSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        scope.launch {
            runCatching { getActiveClient() }
        }
    }

    /**
     * Возвращает активный RPC-клиент или мгновенно создает новое подключение.
     */
    suspend fun getActiveClient(): RpcClient {
        // Fast-path без блокировки мьютекса: если сокет уже подключен и активен
        val current = rpcClient
        if (current != null &&
            (current as? CoroutineScope)?.isActive != false &&
            _connectionState.value is RpcConnectionState.Connected
        ) {
            return current
        }

        return mutex.withLock {
            val existing = rpcClient
            val isAlive = existing != null &&
                (existing as? CoroutineScope)?.isActive != false &&
                _connectionState.value is RpcConnectionState.Connected

            if (isAlive) {
                return@withLock existing!!
            }

            if (existing != null) {
                closeClient(existing)
                rpcClient = null
            }

            _connectionState.value = RpcConnectionState.Connecting
            val client: RpcClient
            try {
                client = createRpcClient()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                L.e(TAG, "🔴 Failed to open WebSocket connection: ${e.message}")
                _connectionState.value = RpcConnectionState.Error(e.message ?: "Connection error")
                throw e
            }

            val token = datastoreManager.getAccessToken()
            if (!token.isNullOrEmpty()) {
                _connectionState.value = RpcConnectionState.Authenticating
                L.d(TAG, "Token found locally. Authenticating inside channel...")
                try {
                    authenticateClient(client, token)
                    L.d(TAG, "Channel successfully authenticated!")
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    L.e(TAG, "Failed to authenticate inside channel: ${e.message}")
                    closeClient(client)
                    _connectionState.value = RpcConnectionState.Error(e.message ?: "Auth error")
                    throw e
                }
            } else {
                L.d(TAG, "Connected as guest (anonymous RPC channel).")
            }

            // Автоматический сброс rpcClient при закрытии корутинного скоупа сокета
            if (client is CoroutineScope) {
                client.coroutineContext.job.invokeOnCompletion { cause ->
                    scope.launch {
                        mutex.withLock {
                            if (rpcClient === client) {
                                L.w(TAG, "Active RPC client closed (${cause?.message ?: "normal"}). Resetting rpcClient.")
                                rpcClient = null
                                _connectionState.value = RpcConnectionState.Idle
                            }
                        }
                    }
                }
            }

            val old = rpcClient
            rpcClient = client
            if (old != null && old !== client) {
                closeClient(old)
            }

            _connectionState.value = RpcConnectionState.Connected
            L.i(TAG, "🟢 RPC Connection ESTABLISHED and READY! (hasToken=${!token.isNullOrEmpty()})")
            client
        }
    }

    /**
     * Запуск прогрева сокета.
     */
    fun start() {
        L.d(TAG, "🚀 Start called -> warming up connection...")
        scope.launch {
            runCatching { getActiveClient() }
        }
    }

    /**
     * Остановка сокета при выходе пользователя.
     */
    fun disconnect() {
        L.d(TAG, "🛑 Disconnect called -> clearing connection...")
        invalidate()
    }

    /**
     * Мгновенный реконнект (0 мс).
     */
    fun triggerInstantReconnect() {
        L.d(TAG, "⚡ Triggering instant 0ms reconnect!")
        invalidate()
        reconnectSignal.tryEmit(Unit)
        scope.launch {
            runCatching { getActiveClient() }
        }
    }

    /**
     * Принудительный перезапуск сокета.
     */
    fun forceReconnect() {
        L.d(TAG, "🔄 Forcing full socket reconnect...")
        triggerInstantReconnect()
    }

    /**
     * Вызывается при разворачивании приложения (onAppForeground).
     */
    fun onAppForeground() {
        L.d(TAG, "📱 App entered FOREGROUND -> forcing instant reconnect...")
        triggerInstantReconnect()
    }

    /**
     * Вызывается при появлении интернет-соединения (onNetworkAvailable).
     */
    fun onNetworkAvailable() {
        L.d(TAG, "📶 Network AVAILABLE -> forcing instant reconnect...")
        triggerInstantReconnect()
    }

    /**
     * Уведомление от стримов/RPC о сбое соединения (для отлова зомби-сокетов).
     */
    fun notifyStreamFailure(e: Throwable) {
        L.w(TAG, "⚠️ Stream failure reported: ${e.message}. Invalidating dead connection and triggering reconnect...")
        triggerInstantReconnect()
    }

    /**
     * Инвалидация текущего клиента при ошибках.
     */
    fun invalidate() {
        val old = rpcClient
        rpcClient = null
        _connectionState.value = RpcConnectionState.Idle
        closeClient(old)
    }

    internal open suspend fun createRpcClient(): RpcClient {
        val rpcUrl = "${platformSyncWebSocketBaseUrl.trimEnd('/')}/api/v4/rpc"
        L.d(TAG, "Connecting RPC socket to $rpcUrl")
        return httpClient.rpc {
            url {
                takeFrom(platformSyncWebSocketBaseUrl)
                path("api/v4/rpc")
            }
            rpcConfig {
                serialization {
                    json(json)
                }
            }
        }
    }

    internal open suspend fun authenticateClient(client: RpcClient, token: String) {
        val authService = client.withService<AuthRpcService>()
        authService.authenticate(token)
    }

    private fun closeClient(client: RpcClient?) {
        if (client == null) return
        try {
            if (client is AutoCloseable) client.close()
            (client as? KrpcClient)?.close()
        } catch (e: Exception) {
            L.w(TAG, "Failed to close RPC client: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "RpcConnectionManager"
    }
}
