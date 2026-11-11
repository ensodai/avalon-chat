package com.ensodai.avalon.pwa

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.await
import kotlin.js.JsAny

class PwaManager : AppInstaller {
    private val _isInstallDismissed = MutableStateFlow(false)
    override val isInstallDismissed: StateFlow<Boolean> = _isInstallDismissed.asStateFlow()

    override fun dismissInstall() {
        _isInstallDismissed.value = true
    }
    // Состояние наличия фонового обновления
    private val _isUpdateAvailable = MutableStateFlow(false)
    val isUpdateAvailable: StateFlow<Boolean> = _isUpdateAvailable.asStateFlow()

    // Состояние возможности установки приложения (PWA Installability)
    private val _canBeInstalled = MutableStateFlow(false)
    override val canBeInstalled: StateFlow<Boolean> = _canBeInstalled.asStateFlow()

    private val scope = MainScope()

    override val showIosInstructions = MutableStateFlow(false)

    val isIos = isIosDevice()
    val isStandalone = isStandaloneMode()

    init {
        if (isIos && !isStandalone) {
            _canBeInstalled.value = true
        }

        // Регистрация PWA и привязка коллбеков к обновлению StateFlow
        initializePwaBridge(
            onUpdateAvailable = {
                _isUpdateAvailable.value = true
            },
            onInstallPromptAvailable = {
                _canBeInstalled.value = true
            }
        )
    }

    /**
     * Подает сигнал Service Worker'у применить обновление.
     * Это приведет к срабатыванию события 'controlling' и перезагрузке страницы.
     */
    fun triggerUpdate() {
        applyPwaUpdate()
    }

    /**
     * Вызывает системный диалог установки PWA или показывает инструкцию (iOS).
     */
    override fun installApp() {
        if (isIos) {
            showIosInstructions.value = true
            return
        }

        scope.launch {
            try {
                // Ожидание завершения Promise, возвращенного из JavaScript
                promptPwaInstall().await<JsAny>()
                // После демонстрации промпта (успешной или нет) скрываем кнопку установки
                _canBeInstalled.value = false
            } catch (e: Exception) {
                println("Ошибка во время выполнения процесса установки PWA: ${e.message}")
            }
        }
    }
}
