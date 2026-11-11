package com.ensodai.avalon.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState


/**
 * WasmJS-реализация перехвата кнопки "Назад".
 *
 * Механика:
 * 1. При enabled=true пушим "sentinel" запись в browser history через pushState.
 * 2. Когда пользователь нажимает "Назад", браузер убирает sentinel и стреляет popstate.
 * 3. Мы перехватываем popstate, вызываем onBack() (= controller.pop()) и пушим новый sentinel.
 * 4. Когда enabled=false (последний экран), sentinel не ставится — "Назад" работает стандартно.
 */
@Composable
actual fun AvalonBackHandler(enabled: Boolean, onBack: () -> Unit) {
    val currentOnBack by rememberUpdatedState(onBack)

    DisposableEffect(enabled) {
        if (!enabled) return@DisposableEffect onDispose {}

        // Пушим sentinel-запись — теперь следующее нажатие "Назад" вызовет popstate
        pushHistorySentinel()

        // При popstate: вызываем Compose-навигацию назад + ставим новый sentinel
        setPopstateHandler {
            currentOnBack()
            pushHistorySentinel()
        }

        onDispose {
            clearPopstateHandler()
        }
    }
}
