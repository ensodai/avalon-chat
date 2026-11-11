package com.ensodai.avalon.domain.useCases.commonUseCase.auth

import com.ensodai.shared.avalon.auth.models.GoogleAuthRequest
import com.ensodai.shared.avalon.auth.models.LoginRequest
import com.ensodai.shared.avalon.auth.models.RegisterRequest
import com.ensodai.shared.avalon.auth.models.WebGoogleAuthRequest
import com.ensodai.shared.avalon.rpc.services.AuthRpcService
import com.ensodai.avalon.data.provider.DeviceInfoProvider
import com.ensodai.avalon.data.provider.PushTokenProvider
import com.ensodai.avalon.data.repository.local.DatastoreManager
import com.ensodai.avalon.di.RpcConnectionManager
import com.ensodai.avalon.domain.auth.AuthSessionManager
import com.ensodai.avalon.utils.getSystemTimezoneOffsetMinutes
import org.koin.core.annotation.Single

@Single
class AuthenticationUserUseCase(
    private val authRpcService: AuthRpcService,
    private val authSessionManager: AuthSessionManager,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val pushTokenProvider: PushTokenProvider,
    private val datastoreManager: DatastoreManager,
    private val rpcConnectionManager: RpcConnectionManager
) {

    suspend fun login(
        serverUrl: String,
        username: String,
        password: String
    ): Result<Unit> = runCatching {
        if (serverUrl.isNotBlank()) {
            datastoreManager.saveServerUrl(serverUrl)
            rpcConnectionManager.forceReconnect()
        }

        val deviceId = deviceInfoProvider.getDeviceId()
        val deviceName = deviceInfoProvider.getDeviceName()
        val fcmToken = pushTokenProvider.getPushToken()

        val response = authRpcService.login(
            LoginRequest(
                username = username,
                password = password,
                deviceId = deviceId,
                deviceName = deviceName,
                timezoneOffset = getSystemTimezoneOffsetMinutes(),
                fcmToken = fcmToken
            )
        )

        authSessionManager.saveSession(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            userId = response.userId.toString(),
            fcmToken = fcmToken
        )
    }

    suspend fun register(
        serverUrl: String,
        username: String,
        password: String
    ): Result<Unit> = runCatching {
        if (serverUrl.isNotBlank()) {
            datastoreManager.saveServerUrl(serverUrl)
            rpcConnectionManager.forceReconnect()
        }

        val deviceId = deviceInfoProvider.getDeviceId()
        val deviceName = deviceInfoProvider.getDeviceName()
        val fcmToken = pushTokenProvider.getPushToken()

        val response = authRpcService.register(
            RegisterRequest(
                username = username,
                password = password,
                deviceId = deviceId,
                deviceName = deviceName,
                timezoneOffset = getSystemTimezoneOffsetMinutes(),
                fcmToken = fcmToken
            )
        )

        authSessionManager.saveSession(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            userId = response.userId.toString(),
            fcmToken = fcmToken
        )
    }

    suspend fun auth(tokenId: String): Result<Unit> = runCatching {
        val deviceId = deviceInfoProvider.getDeviceId()
        val deviceName = deviceInfoProvider.getDeviceName()
        val fcmToken = pushTokenProvider.getPushToken()
        
        val response = authRpcService.googleSignIn(
            GoogleAuthRequest(
                idToken = tokenId,
                deviceId = deviceId,
                deviceName = deviceName,
                timezoneOffset = getSystemTimezoneOffsetMinutes(),
                fcmToken = fcmToken
            )
        )
        
        authSessionManager.saveSession(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            userId = response.userId.toString(),
            fcmToken = fcmToken
        )
    }

    suspend fun webAuth(
        code: String,
        redirectUri: String
    ): Result<Unit> = runCatching {
        val deviceId = deviceInfoProvider.getDeviceId()
        val deviceName = deviceInfoProvider.getDeviceName()
        val fcmToken = pushTokenProvider.getPushToken()
        
        val response = authRpcService.webGoogleSignIn(
            WebGoogleAuthRequest(
                code = code,
                redirectUri = redirectUri,
                deviceId = deviceId,
                deviceName = deviceName,
                timezoneOffset = getSystemTimezoneOffsetMinutes(),
                fcmToken = fcmToken
            )
        )
        
        authSessionManager.saveSession(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            userId = response.userId.toString(),
            fcmToken = fcmToken
        )
    }
}
