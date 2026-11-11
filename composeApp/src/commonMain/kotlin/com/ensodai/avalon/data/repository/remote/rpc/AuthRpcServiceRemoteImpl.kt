package com.ensodai.avalon.data.repository.remote.rpc

import com.ensodai.shared.avalon.auth.models.*
import com.ensodai.shared.avalon.rpc.services.AuthRpcService
import com.ensodai.avalon.di.RpcConnectionManager
import com.ensodai.avalon.utils.L
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.rpc.withService

open class AuthRpcServiceRemoteImpl(
    private val manager: RpcConnectionManager,
    private val executor: RpcCallExecutor
) : AuthRpcService {

    open suspend fun getService(): AuthRpcService = manager.getActiveClient().withService<AuthRpcService>()

    override suspend fun login(request: LoginRequest): AuthResponse =
        executor.execute("login", getService = { getService() }) { login(request) }

    override suspend fun register(request: RegisterRequest): AuthResponse =
        executor.execute("register", getService = { getService() }) { register(request) }

    override suspend fun googleSignIn(request: GoogleAuthRequest): AuthResponse =
        executor.execute("googleSignIn", getService = { getService() }) { googleSignIn(request) }

    override suspend fun webGoogleSignIn(request: WebGoogleAuthRequest): AuthResponse =
        executor.execute("webGoogleSignIn", getService = { getService() }) { webGoogleSignIn(request) }

    override suspend fun refreshToken(request: RefreshTokenRequest): AuthResponse =
        executor.execute("refreshToken", getService = { getService() }) { refreshToken(request) }

    override suspend fun logout(request: LogoutRequest) =
        executor.execute("logout", getService = { getService() }) { logout(request) }

    override suspend fun deleteAccount() =
        executor.execute("deleteAccount", getService = { getService() }) { deleteAccount() }

    override suspend fun authenticate(token: String): AuthResponse? =
        executor.execute("authenticate", getService = { getService() }) { authenticate(token) }

    override suspend fun updateTimezone(timezoneOffset: Int) =
        executor.execute("updateTimezone", getService = { getService() }) { updateTimezone(timezoneOffset) }

    override suspend fun updatePushToken(fcmToken: String) =
        executor.execute("updatePushToken", getService = { getService() }) { updatePushToken(fcmToken) }

    override fun observeAuthState(): Flow<AuthState> = flow {
        L.d(TAG, "--> observeAuthState (stream)")
        emitAll(
            getService().observeAuthState()
                .onEach { L.d(TAG, "  ~ observeAuthState emit=$it") }
        )
    }

    companion object {
        private const val TAG = "AuthRpc"
    }
}
