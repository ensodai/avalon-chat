package app.auth

import app.data.repository.SessionRepository
import app.plugins.VerifiedAccessToken
import app.plugins.verifyAccessToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid
import org.koin.core.annotation.Single

data class GlobalUserSessionKey(
    val userId: Uuid,
    val sessionId: Uuid
)

enum class GlobalUserSessionStatus {
    UNAUTHENTICATED,
    ACTIVE,
    EXPIRED,
    REVOKED
}

data class GlobalUserSessionState(
    val key: GlobalUserSessionKey,
    val status: GlobalUserSessionStatus,
    val expiresAtMs: Long? = null,
    val updatedAtMs: Long = System.currentTimeMillis()
)

sealed interface GlobalUserSessionAuthResult {
    data class Active(
        val verifiedToken: VerifiedAccessToken,
        val key: GlobalUserSessionKey,
        val state: StateFlow<GlobalUserSessionState>
    ) : GlobalUserSessionAuthResult

    data class Expired(
        val verifiedToken: VerifiedAccessToken,
        val key: GlobalUserSessionKey,
        val state: StateFlow<GlobalUserSessionState>
    ) : GlobalUserSessionAuthResult

    data object InvalidToken : GlobalUserSessionAuthResult
}



@Single
class GlobalUserSessionCoordinator(
    private val sessionRepository: SessionRepository,
    private val applicationScope: CoroutineScope
) {

    private data class SessionEntry(
        val state: MutableStateFlow<GlobalUserSessionState>,
        @Volatile var expirationJob: Job? = null
    )

    private val sessions = ConcurrentHashMap<GlobalUserSessionKey, SessionEntry>()

    suspend fun authenticate(accessToken: String): GlobalUserSessionAuthResult {
        val verified = verifyAccessToken(
            token = accessToken,
            sessionRepository = sessionRepository
        ) ?: return GlobalUserSessionAuthResult.InvalidToken

        val key = GlobalUserSessionKey(
            userId = verified.userId,
            sessionId = verified.sessionId
        )
        val entry = getOrCreateEntry(key)
        val nowMs = System.currentTimeMillis()

        return if (verified.expiresAtMs <= nowMs) {
            cancelExpiration(entry)
            entry.state.value = GlobalUserSessionState(
                key = key,
                status = GlobalUserSessionStatus.EXPIRED,
                expiresAtMs = verified.expiresAtMs,
                updatedAtMs = nowMs
            )
            GlobalUserSessionAuthResult.Expired(
                verifiedToken = verified,
                key = key,
                state = entry.state.asStateFlow()
            )
        } else {
            entry.state.value = GlobalUserSessionState(
                key = key,
                status = GlobalUserSessionStatus.ACTIVE,
                expiresAtMs = verified.expiresAtMs,
                updatedAtMs = nowMs
            )
            scheduleExpiration(
                key = key,
                entry = entry,
                expiresAtMs = verified.expiresAtMs
            )
            GlobalUserSessionAuthResult.Active(
                verifiedToken = verified,
                key = key,
                state = entry.state.asStateFlow()
            )
        }
    }

    fun observe(key: GlobalUserSessionKey): StateFlow<GlobalUserSessionState> {
        return getOrCreateEntry(key).state.asStateFlow()
    }

    fun getCurrentState(key: GlobalUserSessionKey): GlobalUserSessionState {
        return getOrCreateEntry(key).state.value
    }

    fun markRevoked(key: GlobalUserSessionKey) {
        val entry = getOrCreateEntry(key)
        cancelExpiration(entry)
        entry.state.value = GlobalUserSessionState(
            key = key,
            status = GlobalUserSessionStatus.REVOKED,
            expiresAtMs = entry.state.value.expiresAtMs,
            updatedAtMs = System.currentTimeMillis()
        )
    }

    fun markExpired(key: GlobalUserSessionKey) {
        val entry = getOrCreateEntry(key)
        cancelExpiration(entry)
        entry.state.value = GlobalUserSessionState(
            key = key,
            status = GlobalUserSessionStatus.EXPIRED,
            expiresAtMs = entry.state.value.expiresAtMs,
            updatedAtMs = System.currentTimeMillis()
        )
    }

    private fun getOrCreateEntry(key: GlobalUserSessionKey): SessionEntry {
        return sessions.computeIfAbsent(key) {
            SessionEntry(
                state = MutableStateFlow(
                    GlobalUserSessionState(
                        key = key,
                        status = GlobalUserSessionStatus.UNAUTHENTICATED
                    )
                )
            )
        }
    }

    private fun scheduleExpiration(
        key: GlobalUserSessionKey,
        entry: SessionEntry,
        expiresAtMs: Long
    ) {
        cancelExpiration(entry)

        val ttlMs = expiresAtMs - System.currentTimeMillis()
        if (ttlMs <= 0L) {
            entry.state.value = GlobalUserSessionState(
                key = key,
                status = GlobalUserSessionStatus.EXPIRED,
                expiresAtMs = expiresAtMs,
                updatedAtMs = System.currentTimeMillis()
            )
            return
        }

        entry.expirationJob = applicationScope.launch {
            delay(ttlMs.milliseconds)
            entry.state.value = GlobalUserSessionState(
                key = key,
                status = GlobalUserSessionStatus.EXPIRED,
                expiresAtMs = expiresAtMs,
                updatedAtMs = System.currentTimeMillis()
            )
        }
    }

    private fun cancelExpiration(entry: SessionEntry) {
        entry.expirationJob?.cancel()
        entry.expirationJob = null
    }
}
