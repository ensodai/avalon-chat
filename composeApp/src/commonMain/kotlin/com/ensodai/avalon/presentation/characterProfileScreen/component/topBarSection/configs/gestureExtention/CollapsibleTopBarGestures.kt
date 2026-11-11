package com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.gestureExtention

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.ToolbarConfig
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.ToolbarState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

//@Composable
//fun Modifier.collapsibleToolbarGestures(
//    toolbarConfig: ToolbarConfig,
//    toolbarHeightAnimatable: Animatable<Dp, AnimationVector1D>,
//    listState: LazyListState
//): Modifier {
//    val density = LocalDensity.current
//
//    val coroutineScope = rememberCoroutineScope()
//    var targetState = ToolbarState.HALF_EXPANDED
//
//    val decaySpec = rememberSplineBasedDecay<Float>()
//    var flingJob by remember { mutableStateOf<Job?>(null) }
//
//    val velocityTracker = VelocityTracker()
//    val canScrollUp by remember {
//        derivedStateOf {
//            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
//        }
//    }
//
//    return this
//        .pointerInput(Unit) {
//            // сбрасываем флинг анимацию прокрутки при нажатии на экран
//            awaitEachGesture {
//                // Ждем первое нажатие
//                awaitFirstDown()
//
//                flingJob?.cancel()
//            }
//        }
//        .pointerInput(Unit) {
//            detectVerticalDragGestures(
//                onDragStart = {
//                    flingJob?.cancel()
//                    velocityTracker.resetTracking()
//                },
//                onVerticalDrag = { change, dragAmount ->
//                    // Регистрируем позицию для вычисления скорости
//                    velocityTracker.addPosition(change.uptimeMillis, change.position)
//
//                    if (!canScrollUp) {
//                        // Переводим dragAmount (в пикселях) в dp
//                        val dragDp = with(density) { dragAmount.toDp() }
//                        // Определяем "тяжесть" свайпа (коэффициент)
//                        val currentHeight = toolbarHeightAnimatable.value
//                        targetState = getTargetState(
//                            current = currentHeight,
//                            toolbarHeightsConfig = toolbarConfig.toolbarHeightConfig
//                        )
//                        val dragFactor = getDragFactor(
//                            currentValue = currentHeight,
//                            toolbarHeightsConfig = toolbarConfig.toolbarHeightConfig
//                        )
//
//                        // Вычисляем эффективное смещение
//                        val effectiveDrag = dragDp * dragFactor
//                        // Вычисляем новое значение высоты в диапазоне [collapsed, overFullExpanded]
//                        val newHeight = (currentHeight + effectiveDrag)
//                            .coerceIn(
//                                toolbarConfig.toolbarHeightConfig.collapsed,
//                                toolbarConfig.toolbarHeightConfig.overFullExpanded
//                            )
//                        // Обновляем значение без анимации (snapping) в процессе драга
//                        coroutineScope.launch {
//                            toolbarHeightAnimatable.snapTo(newHeight)
//                        }
//                    }
//
//                    if (toolbarHeightAnimatable.value == toolbarConfig.toolbarHeightConfig.collapsed) {
//                        coroutineScope.launch {
//                            listState.scrollBy(-dragAmount)
//                        }
//                    }
//                },
//                onDragEnd = {
//                    val currentHeight = toolbarHeightAnimatable.value
//                    targetState = getTargetState(
//                        current = currentHeight,
//                        toolbarHeightsConfig = toolbarConfig.toolbarHeightConfig
//                    )
//
//                    // Если тулбар полностью свёрнут, начинаем обработку fling-а.
//                    if (toolbarHeightAnimatable.value == toolbarConfig.toolbarHeightConfig.collapsed) {
//                        // Получаем вертикальную скорость текущего жеста.
//                        val velocity = velocityTracker.calculateVelocity().y
//                        // Если скорость достаточно велика для fling-а (больше порога 1f по модулю)
//                        if (abs(velocity) > 1f) {
//                            // Запускаем корутину, в которой будет происходить fling-анимация.
//                            flingJob = coroutineScope.launch {
//                                var lastValue = 0f
//                                // Создаём AnimationState с начальным значением 0 и заданной начальной скоростью.
//                                // Затем запускаем decay-анимацию, которая смоделирует замедление fling-а.
//                                AnimationState(initialValue = 0f, initialVelocity = velocity)
//                                    .animateDecay(decaySpec) {
//                                        // В каждом кадре анимации вычисляем изменение (delta) с прошлого кадра.
//                                        coroutineScope.launch {
//                                            val delta = value - lastValue
//                                            // Пытаемся прокрутить список на delta пикселей.
//                                            // Метод scrollBy возвращает фактически потреблённое изменение.
//                                            val consumed = listState.scrollBy(-delta)
//
//                                            // Если список уже не может прокрутиться (находится в самом верху),
//                                            // используем оставшуюся энергию fling-а для "оттягивания" тулбара вниз.
//                                            if (!canScrollUp) {
//                                                val unconsumed = delta - consumed
//                                                // Если оставшаяся энергия (unconsumed) значительна (больше порогового значения),
//                                                // преобразуем её из пикселей в dp и применяем к высоте тулбара.
//                                                if (abs(unconsumed) > 0.5f) {
//                                                    val unconsumedDp =
//                                                        with(density) { unconsumed.toDp() }
//                                                    // Вычисляем новое значение высоты тулбара, ограниченное диапазоном от collapsed до halfExpanded.
//                                                    val newHeight =
//                                                        (toolbarHeightAnimatable.value + unconsumedDp)
//                                                            .coerceIn(
//                                                                toolbarConfig.toolbarHeightConfig.collapsed,
//                                                                toolbarConfig.toolbarHeightConfig.halfExpanded
//                                                            )
//                                                    // Обновляем высоту тулбара мгновенно.
//                                                    toolbarHeightAnimatable.snapTo(newHeight)
//                                                }
//                                            }
//                                            // Обновляем lastValue для расчёта delta в следующем кадре анимации.
//                                            lastValue = value
//                                        }
//                                    }
//                            }
//                        }
//                    }
//
//                    when (targetState) {
//                        ToolbarState.COLLAPSED -> {}
//
//                        ToolbarState.HALF_EXPANDED -> {
//                            if (!canScrollUp) {
//                                coroutineScope.launch {
//                                    toolbarHeightAnimatable.animateTo(
//                                        toolbarConfig.toolbarHeightConfig.halfExpanded,
//                                        animationSpec = tween(durationMillis = 300)
//                                    )
//                                }
//                            }
//
//                        }
//
//                        ToolbarState.FULL_EXPANDED -> {
//                            if (!canScrollUp) {
//                                coroutineScope.launch {
//                                    toolbarHeightAnimatable.animateTo(
//                                        targetValue = toolbarConfig.toolbarHeightConfig.fullExpanded,
//                                        animationSpec = tween(durationMillis = 300)
//                                    )
//                                }
//                            }
//
//                        }
//
//                        ToolbarState.OVER_FULL_EXPANDED -> {
//                            if (!canScrollUp) {
//                                coroutineScope.launch {
//                                    toolbarHeightAnimatable.animateTo(
//                                        targetValue = toolbarConfig.toolbarHeightConfig.fullExpanded,
//                                        animationSpec = tween(durationMillis = 300)
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            )
//        }
//}


@Composable
fun Modifier.collapsibleToolbarGestures(
    toolbarConfig: ToolbarConfig,
    toolbarHeightAnimatable: Animatable<Dp, AnimationVector1D>,
    listState: LazyListState,
    isEditMode: Boolean,
    isOnlyHalfExpanded: Boolean
): Modifier {

    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    var targetState by remember { mutableStateOf(ToolbarState.HALF_EXPANDED) }
    val decaySpec = rememberSplineBasedDecay<Float>()
    val flingJob = remember { mutableStateOf<Job?>(null) }
    val velocityTracker = VelocityTracker()

    val canScrollUpState = remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    val gestureHandler: GestureHandler = if (isEditMode) {
        EditModeGestureHandler(
            listState = listState,
            velocityTracker = velocityTracker,
            coroutineScope = coroutineScope,
            decaySpec = decaySpec,
            flingJob = flingJob
        )
    } else {
        NormalModeGestureHandler(
            toolbarConfig = toolbarConfig,
            toolbarHeightAnimatable = toolbarHeightAnimatable,
            listState = listState,
            density = density,
            velocityTracker = velocityTracker,
            coroutineScope = coroutineScope,
            decaySpec = decaySpec,
            flingJob = flingJob,
            targetState = targetState,
            canScrollUpState = canScrollUpState,
            isOnlyHalfExpanded = isOnlyHalfExpanded
        )
    }

    return this
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown()
                flingJob.value?.cancel()
            }
        }
        .pointerInput(Unit, isEditMode, gestureHandler) {
            detectVerticalDragGestures(
                onDragStart = {
                    flingJob.value?.cancel()
                    velocityTracker.resetTracking()
                },
                onVerticalDrag = { change, verticalDragAmount ->
                    gestureHandler.onVerticalDrag(change, verticalDragAmount)
                },
                onDragEnd = {
                    coroutineScope.launch {
                        gestureHandler.onDragEnd()
                    }

                }
            )
        }
}