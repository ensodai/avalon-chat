package com.ensodai.avalon.presentation.splashScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.domain.auth.AuthSessionManager
import com.ensodai.avalon.domain.global.GlobalEvent
import com.ensodai.avalon.domain.global.GlobalEventBus
import com.ensodai.avalon.domain.useCases.commonUseCase.auth.AuthenticationUserUseCase
import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import com.ensodai.avalon.utils.L
import com.ensodai.avalon.utils.clearUrlCode
import com.ensodai.avalon.utils.getGoogleAuthCode
import com.ensodai.avalon.utils.getWebRedirectUri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

import com.ensodai.shared.avalon.rpc.services.AppVersionRpcService
import com.ensodai.shared.avalon.rpc.services.CheckAppVersionRequest
import com.ensodai.avalon.domain.model.PlatformMetadata

@KoinViewModel
class SplashViewModel(
    private val authSessionManager: AuthSessionManager,
    private val globalEventBus: GlobalEventBus,
    private val authenticationUserUseCase: AuthenticationUserUseCase,
    private val appSettingsRepository: AppSettingsRepository,
    private val appVersionRpcService: AppVersionRpcService,
    private val platformMetadata: PlatformMetadata
) : ViewModel() {

    private val _isAuthorized = MutableStateFlow<Boolean?>(null)
    val isAuthorized: StateFlow<Boolean?> = _isAuthorized.asStateFlow()

    private val _requiredVersion = MutableStateFlow<String?>(null)
    val requiredVersion = _requiredVersion.asStateFlow()

    private val _isThemeLoaded = MutableStateFlow(false)
    val isThemeLoaded: StateFlow<Boolean> = _isThemeLoaded.asStateFlow()

    init {
        viewModelScope.launch {
            appSettingsRepository.getAvalonColorsFlow().first()
            _isThemeLoaded.value = true
        }

        viewModelScope.launch {
            try {
                val checkResult = appVersionRpcService.checkVersion(
                    CheckAppVersionRequest(
                        versionCode = platformMetadata.versionCode,
                        versionName = platformMetadata.versionName,
                        platform = platformMetadata.platform
                    )
                )
                if (checkResult.isUpdateRequired) {
                    globalEventBus.emit(GlobalEvent.ForceUpdateRequired(targetVersion = checkResult.actualAppVersion))
                }
            } catch (e: Exception) {
                L.e("SplashViewModel", "RPC version check failed: ${e.message}")
            }
        }

        viewModelScope.launch {
            // сначала проверяем Google callback
            val code = getGoogleAuthCode()
            if (code != null) {
                clearUrlCode()
                authenticationUserUseCase.webAuth(
                    code = code,
                    redirectUri = getWebRedirectUri()
                ).onFailure { e ->
                    L.e("SplashViewModel", "Web Google auth failed: ${e.message}")
                    // даже если упало — продолжаем, пользователь увидит LoginScreen
                }
            }

            authSessionManager
                .isAuthorized
                .collect { isAuthorized ->
                    _isAuthorized.value = isAuthorized
                }
        }
        subscribeToGlobalEvents()
    }

    private fun subscribeToGlobalEvents() {
        viewModelScope.launch {
            globalEventBus.events.collect { event ->
                when (event) {
                    is GlobalEvent.ForceUpdateRequired -> {
                        _requiredVersion.value = event.targetVersion
                    }
                    is GlobalEvent.RelationshipLevelUp -> {
                        // Игнорируем на сплеш-экране
                    }
                    is GlobalEvent.GiftEffectTriggered -> {
                        // Игнорируем на сплеш-экране
                    }
                }
            }
        }
    }
}
