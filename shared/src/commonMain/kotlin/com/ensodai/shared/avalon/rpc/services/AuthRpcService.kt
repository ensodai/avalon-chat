package com.ensodai.shared.avalon.rpc.services

import com.ensodai.shared.avalon.auth.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
interface AuthRpcService {
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun register(request: RegisterRequest): AuthResponse
    suspend fun googleSignIn(request: GoogleAuthRequest): AuthResponse
    suspend fun webGoogleSignIn(request: WebGoogleAuthRequest): AuthResponse
    suspend fun refreshToken(request: RefreshTokenRequest): AuthResponse
    suspend fun logout(request: LogoutRequest)
    suspend fun deleteAccount()

    fun observeAuthState(): Flow<AuthState>
    suspend fun authenticate(token: String): AuthResponse?
    suspend fun updateTimezone(timezoneOffset: Int)
    suspend fun updatePushToken(fcmToken: String)
}
