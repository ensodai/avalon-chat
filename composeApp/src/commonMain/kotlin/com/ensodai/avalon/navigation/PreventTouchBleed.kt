package com.ensodai.avalon.navigation

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

/**
 * 🛡 Защитный экран.
 * Блокирует проваливание хит-теста на нижние слои навигации (Z-index),
 * но НЕ убивает жесты дочерних компонентов (Пейджеров, Списков),
 * так как не вызывает change.consume().
 */
fun Modifier.preventTouchBleed(): Modifier = this.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            // Просто читаем ивенты, заставляя Compose считать этот слой "твердым".
            awaitPointerEvent(PointerEventPass.Initial)
        }
    }
}
