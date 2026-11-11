package com.ensodai.avalon.navigation

/**
 * Дата-класс, описывающий мгновенное состояние жеста.
 * Обновляется с каждым кадром, пока палец на экране.
 *
 * @param offset На сколько пикселей экран сдвинут прямо сейчас.
 * @param phase Текущая фаза жеста.
 */
data class SwipeBackState(
    val offset: Float = 0f,
    val phase: SwipeBackPhase = SwipeBackPhase.Idle
)
