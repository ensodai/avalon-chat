package com.ensodai.avalon.domain.useCases.commonUseCase.auth

import com.ensodai.shared.avalon.auth.models.GoogleAuthRequest
import com.ensodai.shared.avalon.rpc.services.AuthRpcService
import com.ensodai.avalon.data.provider.DeviceInfoProvider
import com.ensodai.avalon.domain.auth.AuthSessionManager
import com.ensodai.avalon.utils.getSystemTimezoneOffsetMinutes
import org.koin.core.annotation.Single

import com.ensodai.avalon.data.provider.PushTokenProvider

@Single
class AuthenticationUserFromChatUseCase(
    private val authRpcService: AuthRpcService,
    private val authSessionManager: AuthSessionManager,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val pushTokenProvider: PushTokenProvider
) {

    suspend fun auth(tokenId: String, chatId: Int? = null): Result<Unit> = runCatching {
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
}
