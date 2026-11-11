@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package app.apiv4.infrastructure

import com.ensodai.shared.avalon.auth.models.AuthState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid
import kotlin.time.Duration.Companion.seconds

class RpcSessionContext(
    private val scope: CoroutineScope,
    initialState: AuthState = AuthState.Guest,
    initialExpiresAtMs: Long? = null
) {
    private val _state = MutableStateFlow<AuthState>(initialState)
    val state = _state.asStateFlow()

    private val _status = MutableStateFlow(RpcSessionStatus.IDLE)
    val status = _status.asStateFlow()

    private var lastKnownUserId: Uuid? = null
    private var lastKnownSessionId: Uuid? = null

    private var graceJob: Job? = null
    private var expirationJob: Job? = null

    init {
        when (initialState) {
            is AuthState.Authorized -> {
                lastKnownUserId = initialState.userId
                lastKnownSessionId = initialState.sessionId
                _status.value = RpcSessionStatus.AUTHORIZED
                scheduleExpiration(initialExpiresAtMs)
            }
            AuthState.Guest -> {
                _status.value = RpcSessionStatus.IDLE
            }
            AuthState.TokenExpired -> {
                _status.value = RpcSessionStatus.EXPIRED_TIMEOUT
            }
        }
    }

    suspend fun userId(): Uuid {
        return combine(state, status) { s, st ->
            when {
                s is AuthState.Authorized -> s.userId
                st == RpcSessionStatus.IN_GRACE_PERIOD -> lastKnownUserId
                else -> null
            }
        }.filterNotNull().first()
    }

    suspend fun sessionId(): Uuid {
        return combine(state, status) { s, st ->
            when {
                s is AuthState.Authorized -> s.sessionId
                st == RpcSessionStatus.IN_GRACE_PERIOD -> lastKnownSessionId
                else -> null
            }
        }.filterNotNull().first()
    }

    fun observeUserId(): Flow<Uuid?> {
        return combine(state, status) { s, st ->
            when {
                s is AuthState.Authorized -> s.userId
                st == RpcSessionStatus.IN_GRACE_PERIOD -> lastKnownUserId
                st == RpcSessionStatus.EXPIRED_TIMEOUT -> lastKnownUserId
                else -> null
            }
        }.distinctUntilChanged()
    }


    fun updateState(newState: AuthState, expiresAtMs: Long? = null) {
        val oldState = _state.value
        _state.value = newState

        when (newState) {
            is AuthState.Authorized -> {
                lastKnownUserId = newState.userId
                lastKnownSessionId = newState.sessionId

                graceJob?.cancel()
                graceJob = null
                _status.value = RpcSessionStatus.AUTHORIZED
                scheduleExpiration(expiresAtMs)
            }
            AuthState.Guest -> {
                cancelAllJobs()
                lastKnownUserId = null
                lastKnownSessionId = null
                _status.value = RpcSessionStatus.REVOKED
            }
            AuthState.TokenExpired -> {
                if (oldState is AuthState.Authorized && _status.value != RpcSessionStatus.IN_GRACE_PERIOD) {
                    startGracePeriod()
                } else if (oldState is AuthState.Guest) {
                    _status.value = RpcSessionStatus.EXPIRED_TIMEOUT
                }
            }
        }
    }

    private fun scheduleExpiration(expiresAtMs: Long?) {
        expirationJob?.cancel()
        if (expiresAtMs == null) return

        val delayTime = expiresAtMs - System.currentTimeMillis()
        if (delayTime <= 0) {
            updateState(AuthState.TokenExpired)
            return
        }

        expirationJob = scope.launch {
            delay(delayTime.milliseconds)
            updateState(AuthState.TokenExpired)
        }
    }

    private fun cancelAllJobs() {
        graceJob?.cancel()
        expirationJob?.cancel()
        graceJob = null
        expirationJob = null
    }

    private fun startGracePeriod() {
        graceJob?.cancel()
        expirationJob?.cancel() // Больше не нужно следить за основным временем
        _status.value = RpcSessionStatus.IN_GRACE_PERIOD
        
        graceJob = scope.launch {
            delay(10.seconds)
            if (_status.value == RpcSessionStatus.IN_GRACE_PERIOD) {
                _status.value = RpcSessionStatus.EXPIRED_TIMEOUT
            }
        }
    }

    fun currentState(): AuthState = _state.value
    fun currentStatus(): RpcSessionStatus = _status.value
}

enum class RpcSessionStatus {
    IDLE,
    AUTHORIZED,
    IN_GRACE_PERIOD,
    EXPIRED_TIMEOUT,
    REVOKED
}

