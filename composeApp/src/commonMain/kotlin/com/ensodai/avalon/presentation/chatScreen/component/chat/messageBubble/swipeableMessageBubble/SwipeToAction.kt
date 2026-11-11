package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.swipeableMessageBubble

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.fastFirstOrNull
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.abs

@Composable
fun SwipeToAction(
    modifier: Modifier = Modifier,
    state: SwipeToActionState,
    isEnableSwipe: Boolean,
    isEnableSwipeToAction: Boolean,
    stayAtMaxOffset: Boolean = true,
    onSwiped: () -> Unit,
    onMidSwipe: () -> Unit,
    content: @Composable () -> Unit
) {
    // Логика вибрации (оставил твою)
    LaunchedEffect(state, isEnableSwipeToAction) {
        snapshotFlow { state.offset }
            .map { it <= state.config.midSwipeThreshold }
            .distinctUntilChanged()
            .collect { crossed ->
                if (isEnableSwipeToAction && crossed) {
                    onMidSwipe()
                }
            }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .swipeToAction(
                isEnableSwipe = isEnableSwipe,
                isEnableSwipeToAction = isEnableSwipeToAction,
                state = state,
                stayAtMaxOffset = stayAtMaxOffset,
                onSwiped = onSwiped
            )
    ) {
        // Контент движется через GPU слой (плавно)
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = state.offset
                    clip = false
                }
        ) {
            content()
        }
    }
}
// БАЗА
//@Composable
//fun SwipeToAction(
//    state: SwipeToActionState,
//    modifier: Modifier = Modifier,
//    isEnableSwipe: Boolean,
//    isEnableSwipeToAction: Boolean,
//    onSwiped: () -> Unit,
//    onMidSwipe: () -> Unit,
//    content: @Composable () -> Unit
//) {
//    val midSwipeTriggered = remember { mutableStateOf(false) }
//
//    LaunchedEffect(state.offset, isEnableSwipeToAction) {
//        val hasCrossedThreshold = state.offset <= state.config.midSwipeThreshold
//        // Триггер сработает только если действие разрешено
//        if (isEnableSwipeToAction && hasCrossedThreshold && !midSwipeTriggered.value) {
//            onMidSwipe()
//            midSwipeTriggered.value = true
//        } else if (!hasCrossedThreshold) {
//            midSwipeTriggered.value = false
//        }
//    }
//
//    Box(
//        modifier = modifier
//            .fillMaxWidth()
//            .swipeToAction(
//                isEnableSwipe = isEnableSwipe,
//                isEnableSwipeToAction = isEnableSwipeToAction,
//                state = state,
//                onSwiped = onSwiped
//            )
//    ) {
//        Box(
//            modifier = modifier.offset { IntOffset(state.offset.roundToInt(), 0) }
//        ) {
//            content()
//        }
//    }
//}


@Composable
fun rememberSwipeToActionState(
    config: SwipeToActionConfig = SwipeToActionConfig(),
    onSwipeStateChanged: (isSwiping: Boolean) -> Unit,
): SwipeToActionState {
    val coroutineScope = rememberCoroutineScope()
    val state = remember(coroutineScope, config) {
        SwipeToActionState(coroutineScope, config)
    }

    // Отслеживаем статус свайпа для внешнего мира (блокировка кнопок и т.д.)
    // Используем snapshotFlow чтобы не спамить рекомпозициями
    LaunchedEffect(state) {
        snapshotFlow { state.offset != 0f }
            .distinctUntilChanged()
            .collect { isSwiping ->
                onSwipeStateChanged(isSwiping)
            }
    }

    return state
}


// БАЗА
//@Composable
//fun rememberSwipeToActionState(
//    config: SwipeToActionConfig = SwipeToActionConfig(), // Принимаем конфиг
//    onSwipeStateChanged: (isSwiping: Boolean) -> Unit,
//): SwipeToActionState {
//    val coroutineScope = rememberCoroutineScope()
//
//    // 1. Создаем State Holder
//    val state = remember(coroutineScope, config) {
//        SwipeToActionState(coroutineScope, config)
//    }
//    LaunchedEffect(state, onSwipeStateChanged) {
//        snapshotFlow { state.offset != 0f }
//            .distinctUntilChanged()
//            .collect { isSwiping ->
//                onSwipeStateChanged(isSwiping)
//            }
//    }
//    // 2. Организуем синхронизацию здесь, внутри фабрики
//
//    // Синхронизация: Анимация -> Состояние
//    // Когда значение анимации меняется, мы обновляем наш главный offset
//    LaunchedEffect(state.animatable.value) {
//        state.updateOffset(state.animatable.value)
//    }
//
//    // Синхронизация: Состояние -> Анимация
//    // Если offset изменился извне (например, через state.reset()),
//    // мы должны немедленно "щелкнуть" анимацию в то же положение.
//    LaunchedEffect(state.offset) {
//        if (state.animatable.value != state.offset) {
//            state.animatable.snapTo(state.offset)
//        }
//    }
//
//    return state
//}

suspend fun AwaitPointerEventScope.awaitHorizontalDragOrCancel(
    pointerId: PointerId
): PointerInputChange? {
    val touchSlop = viewConfiguration.touchSlop
    var horizontalDrag = 0f
    var verticalDrag = 0f
    var drag: PointerInputChange?

    do {
        val event = awaitPointerEvent()
        drag = event.changes.fastFirstOrNull { it.id == pointerId }

        if (drag == null || !drag.pressed) {
            return null // Жест отменен или палец поднят
        }

        // Вычисляем смещение вручную
        val change = drag.position - drag.previousPosition
        horizontalDrag += change.x
        verticalDrag += change.y

    } while (abs(horizontalDrag) < touchSlop && abs(verticalDrag) < touchSlop)

    return if (abs(horizontalDrag) > touchSlop) {
        // Горизонтальный свайп подтвержден
        drag
    } else {
        // Вертикальный свайп "победил", считаем это отменой
        null
    }
}

fun Modifier.swipeToAction(
    isEnableSwipe: Boolean,
    isEnableSwipeToAction: Boolean,
    state: SwipeToActionState,
    stayAtMaxOffset: Boolean,
    onSwiped: () -> Unit
): Modifier {
    if (!isEnableSwipe) return this

    return this.pointerInput(isEnableSwipeToAction, stayAtMaxOffset, onSwiped) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            // Важно: останавливаем анимацию возврата, если юзер снова коснулся
            state.stopAnimation()

            val initialHorizontalDrag = awaitHorizontalDragOrCancel(down.id)
            val isTryingToSwipeLeft = initialHorizontalDrag != null &&
                    initialHorizontalDrag.position.x < initialHorizontalDrag.previousPosition.x
            val isAlreadySwiped = state.offset < 0f

            if (initialHorizontalDrag != null && (isTryingToSwipeLeft || isAlreadySwiped)) {
                initialHorizontalDrag.consume()

                drag(initialHorizontalDrag.id) { change ->
                    val delta = change.position.x - change.previousPosition.x
                    // Вызываем твою математику
                    state.dispatchDragDelta(delta)
                    change.consume()
                }

                state.settle(
                    onSwiped = onSwiped,
                    isActionEnabled = isEnableSwipeToAction,
                    stayAtMaxOffset = stayAtMaxOffset
                )
            } else {
                // Если просто тапнули по открытому - закрываем
                if (state.offset != 0f) {
                    state.settle(onSwiped = {}, isActionEnabled = false, stayAtMaxOffset = false)
                }
            }
        }
    }
}

// БАЗА
//fun Modifier.swipeToAction(
//    isEnableSwipe: Boolean,
//    isEnableSwipeToAction: Boolean,
//    state: SwipeToActionState,
//    onSwiped: () -> Unit
//): Modifier {
//    if (!isEnableSwipe) {
//        return this
//    }
//
//    return this.pointerInput(Unit) {
//        if (!isEnableSwipe) return@pointerInput
//        awaitEachGesture {
//
//            // Убираем ошибочную логику и возвращаем вашу.
//            val down = awaitFirstDown(requireUnconsumed = false)
//            val initialHorizontalDrag = awaitHorizontalDragOrCancel(down.id)
//
//            val isAlreadySwiped = state.offset < 0f
//            val isTryingToSwipeLeft = initialHorizontalDrag != null &&
//                    initialHorizontalDrag.position.x < initialHorizontalDrag.previousPosition.x
//
//            // Условие теперь точно такое же, как в вашем рабочем коде
//            if (initialHorizontalDrag != null && (isTryingToSwipeLeft || isAlreadySwiped)) {
//                initialHorizontalDrag.consume()
//
//                // Перетаскивание: просто передаем дельту в state
//                drag(initialHorizontalDrag.id) { change ->
//                    state.dispatchDragDelta(change.position.x - change.previousPosition.x)
//                    change.consume()
//                }
//
//                // Конец жеста: просим state разобраться
//                state.settle(
//                    onSwiped = onSwiped,
//                    isActionEnabled = isEnableSwipeToAction
//                )
//            }
//        }
//    }
//}
