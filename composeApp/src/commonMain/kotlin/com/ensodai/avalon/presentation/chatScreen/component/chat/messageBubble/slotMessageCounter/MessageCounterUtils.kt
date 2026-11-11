package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.slotMessageCounter

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.swipeableMessageBubble.SwipeToActionState

// Утилита для копирования infiniteRepeatableSpec с новым смещением
fun InfiniteRepeatableSpec<Float>.copy(initialStartOffset: StartOffset): InfiniteRepeatableSpec<Float> {
    // Так как в Jetpack Compose нет публичного способа скопировать spec с новым offset,
    // мы пересоздаем его с теми же параметрами.
    return infiniteRepeatable(animation, repeatMode, initialStartOffset)
}

/**
 * Вычисляет ширину контейнера на основе сдвига при свайпе.
 */
@Composable
fun calculateAnimatedWidth(
    offset: Float,
    messageCounterDefaults: MessageCounterDefaults
): Dp {
    val density = LocalDensity.current
    val baseWidthPx = with(density) { messageCounterDefaults.BaseWidth.toPx() }
    val targetWidthPx = baseWidthPx + -offset // offset отрицательный
    return with(density) { targetWidthPx.toDp() }
}

/**
 * Вычисляет альфа-канал для текста счетчика.
 */
fun calculateTextAlpha(swipeToActionState: SwipeToActionState): Float {
    val trigger = swipeToActionState.config.midSwipeThreshold * 0.1f
    val progress = (swipeToActionState.offset / trigger).coerceIn(0f, 1f)
    return 1f - progress
}

/**
 * Вычисляет прогресс анимации для одного круга на основе его конфигурации.
 */
fun calculateSwipeProgress(
    offset: Float,
    midSwipeThreshold: Float,
    config: CircleSwipeConfig
): Float {
    val start = midSwipeThreshold * config.startFactor
    val end = midSwipeThreshold * config.endFactor
    val window = end - start
    //защита от деления на ноль
    if (window == 0f) return 0f
    return ((offset - start) / window).coerceIn(0f, 1f)
}

/**
 * Запоминает состояние "гигашлепка" (достижение порога свайпа).
 */
@Composable
fun rememberGigashlepState(swipeToActionState: SwipeToActionState): Boolean {
    var hasTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(swipeToActionState.offset) {
        val hasCrossedThreshold =
            swipeToActionState.offset <= swipeToActionState.config.midSwipeThreshold
        if (hasCrossedThreshold && !hasTriggered) {
            hasTriggered = true
        }
        if (swipeToActionState.offset > swipeToActionState.config.midSwipeThreshold) {
            hasTriggered = false
        }
    }
    return hasTriggered
}
