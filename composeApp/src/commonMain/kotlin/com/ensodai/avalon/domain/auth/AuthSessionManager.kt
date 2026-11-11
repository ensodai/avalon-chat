package com.ensodai.avalon.domain.auth

import com.ensodai.shared.avalon.auth.models.AuthState
import com.ensodai.shared.avalon.auth.models.RefreshTokenRequest
import com.ensodai.shared.avalon.rpc.services.AuthRpcService
import com.ensodai.avalon.data.provider.PushTokenProvider
import com.ensodai.avalon.data.repository.local.DatastoreManager
import com.ensodai.avalon.di.ApplicationScope
import com.ensodai.avalon.di.RpcConnectionManager
import com.ensodai.avalon.domain.chatEvents.RpcConnectionState
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.getSystemTimezoneOffsetMinutes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

/**
 * Реактивный менеджер авторизации.
 * Слушает серверный поток состояний и автоматически управляет жизненным циклом сессии.
 */
@Single
class AuthSessionManager(
    private val authRpcService: AuthRpcService,
    private val rpcConnectionManager: RpcConnectionManager,
    private val datastoreManager: DatastoreManager,
    @ApplicationScope private val scope: CoroutineScope,
    private val logger: AppLogger,
    private val pushTokenProvider: PushTokenProvider
) {
    private val refreshMutex = Mutex()

    private val _isAuthorized = MutableStateFlow<Boolean?>(null)
    val isAuthorized: StateFlow<Boolean?> = _isAuthorized.asStateFlow()

    private val _isSessionAuthorized = MutableStateFlow(false)
    val isSessionAuthorized: StateFlow<Boolean> = _isSessionAuthorized.asStateFlow()

    init {
        scope.launch {
            datastoreManager.isInitialized.first { isInit: Boolean -> isInit }
            if (_isAuthorized.value == null) {
                _isAuthorized.value = !datastoreManager.getAccessToken().isNullOrEmpty()
            }
        }
        startServerObservation()
    }

    private fun startServerObservation() {
        scope.launch {
            rpcConnectionManager.connectionState.collectLatest { state ->
                if (state is RpcConnectionState.Connected) {
                    try {
                        logger.d("AuthSessionManager", "🔄 Connected -> starting observeAuthState...")
                        authRpcService.observeAuthState()
                            .catch { e ->
                                if (e is CancellationException) throw e
                                logger.w("AuthSessionManager", "🔄 Auth stream error: ${e.message}")
                                _isSessionAuthorized.value = false
                                rpcConnectionManager.notifyStreamFailure(e)
                            }
                            .collect { authState ->
                                handleServerAuthState(authState)
                            }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        logger.w("AuthSessionManager", "🔄 Observation failed: ${e.message}")
                        _isSessionAuthorized.value = false
                        rpcConnectionManager.notifyStreamFailure(e)
                    }
                } else {
                    _isSessionAuthorized.value = false
                }
            }
        }
    }

    private suspend fun handleServerAuthState(state: AuthState) {
        logger.d("AuthSessionManager", "📩 New Server AuthState: ${state::class.simpleName}")

        when (state) {
            is AuthState.Authorized -> {
                _isAuthorized.value = true
                _isSessionAuthorized.value = true
            }

            is AuthState.TokenExpired -> {
                _isSessionAuthorized.value = false
                logger.i("AuthSessionManager", "🔑 Token expired on server. Triggering refresh...")
                refreshSession()
            }

            is AuthState.Guest -> {
                _isSessionAuthorized.value = false
                datastoreManager.isInitialized.first { isInit: Boolean -> isInit }
                if (datastoreManager.getAccessToken().isNullOrEmpty()) {
                    _isAuthorized.value = false
                }
            }
        }
    }

    /**
     * Выполняет обновление токена и уведомляет сервер о новом токенe.
     */
    private suspend fun refreshSession() {
        var refreshSuccess = false
        refreshMutex.withLock {
            val refreshToken = datastoreManager.getRefreshToken() ?: return@withLock
            val fcmToken = pushTokenProvider.getPushToken()

            runCatching {
                logger.d("AuthSessionManager", "⏳ Calling refreshToken RPC...")
                val response = authRpcService.refreshToken(
                    RefreshTokenRequest(
                        refreshToken = refreshToken,
                        timezoneOffset = getSystemTimezoneOffsetMinutes(),
                        fcmToken = fcmToken
                    )
                )

                datastoreManager.saveAuthData(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                    userId = response.userId.toString()
                )

                logger.d("AuthSessionManager", "✅ Token refreshed. Sending authenticate RPC...")
                authRpcService.authenticate(response.accessToken)

                _isAuthorized.value = true
                _isSessionAuthorized.value = true
                refreshSuccess = true
            }.onFailure { e ->
                logger.e("AuthSessionManager", "❌ Refresh failed: ${e.message}")
                if (isNetworkError(e)) {
                    logger.w("AuthSessionManager", "⚠️ Refresh failed due to network error. Keeping session locally.")
                } else {
                    logger.e("AuthSessionManager", "❌ Critical refresh error. Logging out locally.")
                    logoutLocally()
                }
            }
        }

        if (refreshSuccess) {
            runCatching {
                logger.d("AuthSessionManager", "🕒 Updating timezone offset on server...")
                authRpcService.updateTimezone(getSystemTimezoneOffsetMinutes())
            }.onFailure { e ->
                logger.w("AuthSessionManager", "⚠️ Timezone update after refresh failed: ${e.message}")
            }
        }
    }

    suspend fun saveSession(accessToken: String, refreshToken: String, userId: String, fcmToken: String? = null) {
        datastoreManager.saveAuthData(accessToken, refreshToken, userId)
        _isAuthorized.value = true
        _isSessionAuthorized.value = true
        logger.d("AuthSessionManager", "🚀 New session saved. Authenticating active connection...")

        runCatching {
            authRpcService.authenticate(accessToken)
            logger.d("AuthSessionManager", "🕒 Updating timezone offset on server...")
            authRpcService.updateTimezone(getSystemTimezoneOffsetMinutes())
            fcmToken?.let {
                logger.d("AuthSessionManager", "📲 Updating push token on server...")
                authRpcService.updatePushToken(it)
            }
        }.onFailure { e ->
            logger.w("AuthSessionManager", "⚠️ Post-authentication updates failed: ${e.message}")
            rpcConnectionManager.forceReconnect()
        }
    }

    suspend fun logoutLocally() {
        datastoreManager.clearUserSession()
        _isAuthorized.value = false
        _isSessionAuthorized.value = false
        rpcConnectionManager.disconnect()
    }

    fun getAccessToken(): String? = datastoreManager.getAccessToken()

    private fun isNetworkError(e: Throwable): Boolean {
        val message = e.message ?: ""
        val authFailureMessages = listOf(
            "Session revoked",
            "Invalid refresh token",
            "Security Warning",
            "token"
        )
        if (authFailureMessages.any { message.contains(it, ignoreCase = true) }) {
            return false
        }

        val className = e::class.simpleName ?: ""
        val networkClassNames = listOf(
            "IOException",
            "ConnectException",
            "SocketException",
            "SocketTimeoutException",
            "UnknownHostException",
            "ClosedChannelException",
            "ClosedReceiveChannelException",
            "ClosedSendChannelException",
            "UnresolvedAddressException",
            "WebSocketException",
            "HttpRequestTimeoutException"
        )
        if (networkClassNames.any { className.contains(it, ignoreCase = true) }) {
            return true
        }

        val networkMessages = listOf(
            "channel closed",
            "connection",
            "timeout",
            "host",
            "network",
            "socket",
            "offline",
            "closed channel",
            "broken pipe",
            "transport"
        )
        return networkMessages.any { message.contains(it, ignoreCase = true) }
    }
}
