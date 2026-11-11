package com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.gestureExtention

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.ToolbarConfig
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.ToolbarState
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.getTargetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

//class NormalModeGestureHandler(
//    private val toolbarConfig: ToolbarConfig,
//    private val toolbarHeightAnimatable: Animatable<Dp, AnimationVector1D>,
//    private val listState: LazyListState,
//    private val density: Density,
//    private val velocityTracker: VelocityTracker,
//    private val coroutineScope: CoroutineScope,
//    private val decaySpec: DecayAnimationSpec<Float>,
//    private val flingJob: MutableState<Job?>,
//    private var targetState: ToolbarState,
//    private val canScrollUpState: State<Boolean>,
//) : GestureHandler {
//    override fun onVerticalDrag(change: PointerInputChange, verticalDragAmount: Float) {
//        velocityTracker.addPosition(change.uptimeMillis, change.position)
//        if (!canScrollUpState.value) {
//            updateToolbarHeight(verticalDragAmount, density, toolbarHeightAnimatable, toolbarConfig, coroutineScope)
//        }
//        if (toolbarHeightAnimatable.value == toolbarConfig.toolbarHeightConfig.collapsed) {
//            coroutineScope.launch {
//                listState.scrollBy(-verticalDragAmount)
//            }
//        }
//    }
//
//    override suspend fun onDragEnd() {
//        val currentHeight = toolbarHeightAnimatable.value
//        targetState = getTargetState(currentHeight, toolbarConfig.toolbarHeightConfig)
//        Log.d("EditModeHandler", "START: currentHeight=$currentHeight, collapsed=${toolbarConfig.toolbarHeightConfig.collapsed}")
//
//        // Обработка fling-анимации, если тулбар свернут
//        if (toolbarHeightAnimatable.value == toolbarConfig.toolbarHeightConfig.collapsed) {
//            val velocity = velocityTracker.calculateVelocity().y
//            val targetHeight = toolbarConfig.toolbarHeightConfig.halfExpanded
//            Log.d("EditModeHandler", "INSIDE IF: velocity=$velocity")
//            if (abs(velocity) > 1f) {
//                flingJob.value = coroutineScope.launch {
//                    var lastValue = 0f
//                    callbackFlow {
//                        AnimationState(initialValue = 0f, initialVelocity = velocity)
//                            .animateDecay(decaySpec) {
//                                val delta = value - lastValue
//                                trySend(delta)  // Используем trySend вместо emit
//                                lastValue = value
//                                if (toolbarHeightAnimatable.value >= targetHeight) {
//                                    cancelAnimation() // Останавливаем анимацию
//                                    close() // Завершаем поток
//                                }
//                                if (!listState.canScrollForward){
//                                    cancelAnimation() // Останавливаем анимацию
//                                    close() // Завершаем поток
//                                }
//                            }
//                        close()  // Закрываем поток после завершения анимации
//                    }.collect { delta ->
//                        Log.d("EditModeHandler", "animateDecay processing delta=$delta")
//                        val consumed = listState.scrollBy(-delta)
//                        if (!canScrollUpState.value) {
//                            val unconsumed = delta - consumed
//                            if (abs(unconsumed) > 0.5f) {
//                                val unconsumedDp = with(density) { unconsumed.toDp() }
//                                val newHeight = (toolbarHeightAnimatable.value + unconsumedDp)
//                                    .coerceIn(
//                                        toolbarConfig.toolbarHeightConfig.collapsed,
//                                        toolbarConfig.toolbarHeightConfig.halfExpanded
//                                    )
//                                Log.d("EditModeHandler", "Updating height to $newHeight target $targetHeight")
//                                toolbarHeightAnimatable.snapTo(newHeight)
//                            }
//                        }
//                    }
//                }
//                // Дождемся завершения fling-анимации перед переходом к следующей
////                flingJob.value?.join()
//            }
//        }
//
//
//        when (targetState) {
//            ToolbarState.COLLAPSED -> {}
//            ToolbarState.HALF_EXPANDED -> {
//                if (!canScrollUpState.value) {
//                    coroutineScope.launch {
//                        toolbarHeightAnimatable.animateTo(
//                            toolbarConfig.toolbarHeightConfig.halfExpanded,
//                            animationSpec = tween(durationMillis = 300)
//                        )
//                    }
//                }
//            }
//            ToolbarState.FULL_EXPANDED -> {
//                if (!canScrollUpState.value) {
//                    coroutineScope.launch {
//                        toolbarHeightAnimatable.animateTo(
//                            toolbarConfig.toolbarHeightConfig.fullExpanded,
//                            animationSpec = tween(durationMillis = 300)
//                        )
//                    }
//                }
//            }
//            ToolbarState.OVER_FULL_EXPANDED -> {
//                if (!canScrollUpState.value) {
//                    coroutineScope.launch {
//                        toolbarHeightAnimatable.animateTo(
//                            toolbarConfig.toolbarHeightConfig.fullExpanded,
//                            animationSpec = tween(durationMillis = 300)
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

class NormalModeGestureHandler(
    private val toolbarConfig: ToolbarConfig,
    private val toolbarHeightAnimatable: Animatable<Dp, AnimationVector1D>,
    private val listState: LazyListState,
    private val density: Density,
    private val velocityTracker: VelocityTracker,
    private val coroutineScope: CoroutineScope,
    private val decaySpec: DecayAnimationSpec<Float>,
    private val flingJob: MutableState<Job?>,
    private var targetState: ToolbarState,
    private val canScrollUpState: State<Boolean>,
    private val isOnlyHalfExpanded: Boolean
) : GestureHandler {

    override fun onVerticalDrag(change: PointerInputChange, verticalDragAmount: Float) {
        velocityTracker.addPosition(change.uptimeMillis, change.position)
        if (!canScrollUpState.value) {
            updateToolbarHeight(
                verticalDragAmount = verticalDragAmount,
                density = density,
                toolbarHeightAnimatable = toolbarHeightAnimatable,
                toolbarConfig = toolbarConfig,
                coroutineScope = coroutineScope,
                isOnlyHalfExpanded = isOnlyHalfExpanded
            )
        }
        if (toolbarHeightAnimatable.value == toolbarConfig.toolbarHeightConfig.collapsed) {
            coroutineScope.launch {
                listState.scrollBy(-verticalDragAmount)
            }
        }
    }

    override suspend fun onDragEnd() {
        val currentHeight = toolbarHeightAnimatable.value
        targetState = getTargetState(currentHeight, toolbarConfig.toolbarHeightConfig)
        val velocity = velocityTracker.calculateVelocity().y
        velocityTracker.resetTracking() // Сбрасываем трекер после использования

        if (isToolbarCollapsed()) {
            handleCollapsedToolbarDragEnd(velocity)
        } else {
            if (isOnlyHalfExpanded) {
                if (currentHeight <= toolbarConfig.toolbarHeightConfig.halfExpanded) {
                    animateToolbarToTargetState()
                }
            } else {
                animateToolbarToTargetState()
            }
        }
    }

    private fun isToolbarCollapsed(): Boolean {
        return toolbarHeightAnimatable.value == toolbarConfig.toolbarHeightConfig.collapsed
    }

    private suspend fun handleCollapsedToolbarDragEnd(velocity: Float) {
        if (abs(velocity) > 1f) {
            startFlingAnimation(velocity)
        } else {
            animateToolbarToTargetState() // Still animate to target state even if no fling
        }
    }

    private suspend fun startFlingAnimation(velocity: Float) {
        val targetHeight = toolbarConfig.toolbarHeightConfig.halfExpanded
        flingJob.value = coroutineScope.launch {
            animateDecayScroll(velocity, targetHeight)
        }
    }

    private suspend fun animateDecayScroll(velocity: Float, targetHeight: Dp) {
        var lastValue = 0f
        callbackFlow {
            AnimationState(initialValue = 0f, initialVelocity = velocity)
                .animateDecay(decaySpec) {
                    val delta = value - lastValue
                    trySend(delta)
                    lastValue = value
                    if (toolbarHeightAnimatable.value >= targetHeight || !listState.canScrollForward) {
                        cancelAnimation()
                        close()
                    }
                }
            close()
        }.collect { delta ->
            processFlingDelta(delta)
        }
    }


    private suspend fun processFlingDelta(delta: Float) {
        val consumed = listState.scrollBy(-delta)
        if (!canScrollUpState.value) {
            adjustToolbarHeightForUnconsumedDelta(delta, consumed)
        }
    }


    private suspend fun adjustToolbarHeightForUnconsumedDelta(delta: Float, consumed: Float) {
        val unconsumed = delta - consumed
        if (abs(unconsumed) > 0.5f) {
            val unconsumedDp = with(density) { unconsumed.toDp() }
            val newHeight = (toolbarHeightAnimatable.value + unconsumedDp)
                .coerceIn(
                    toolbarConfig.toolbarHeightConfig.collapsed,
                    toolbarConfig.toolbarHeightConfig.halfExpanded
                )
            toolbarHeightAnimatable.snapTo(newHeight)
        }
    }


    private suspend fun animateToolbarToTargetState() {
        when (targetState) {
            ToolbarState.COLLAPSED -> {}
            ToolbarState.HALF_EXPANDED -> {
                if (!canScrollUpState.value) {
                    coroutineScope.launch {
                        toolbarHeightAnimatable.animateTo(
                            toolbarConfig.toolbarHeightConfig.halfExpanded,
                            animationSpec = tween(durationMillis = 300)
                        )
                    }
                }
            }

            ToolbarState.FULL_EXPANDED -> {
                if (!canScrollUpState.value) {
                    coroutineScope.launch {
                        toolbarHeightAnimatable.animateTo(
                            toolbarConfig.toolbarHeightConfig.fullExpanded,
                            animationSpec = tween(durationMillis = 300)
                        )
                    }
                }
            }

            ToolbarState.OVER_FULL_EXPANDED -> {
                if (!canScrollUpState.value) {
                    coroutineScope.launch {
                        toolbarHeightAnimatable.animateTo(
                            toolbarConfig.toolbarHeightConfig.fullExpanded,
                            animationSpec = tween(durationMillis = 300)
                        )
                    }
                }
            }
        }
    }
}

//override fun onDragEnd() {
//    val currentHeight = toolbarHeightAnimatable.value
//    targetState = getTargetState(currentHeight, toolbarConfig.toolbarHeightConfig)
//    Log.d("EditModeHandler", "START: currentHeight=$currentHeight, collapsed=${toolbarConfig.toolbarHeightConfig.collapsed}")
//
//    if (toolbarHeightAnimatable.value == toolbarConfig.toolbarHeightConfig.collapsed) {
//        val velocity = velocityTracker.calculateVelocity().y
//        Log.d("EditModeHandler", "INSIDE IF: velocity=$velocity")
//        if (abs(velocity) > 1f) {
//            flingJob.value = coroutineScope.launch {
//                var lastValue = 0f
//                AnimationState(initialValue = 0f, initialVelocity = velocity)
//                    .animateDecay(decaySpec) {
//                        coroutineScope.launch {
//                            val delta = value - lastValue
//                            Log.d("EditModeHandler", "animateDecay start")
//                            Log.d("EditModeHandler", "${toolbarHeightAnimatable.value} ${toolbarConfig.toolbarHeightConfig.collapsed}")
//                            val consumed = listState.scrollBy(-delta)
//                            if (!canScrollUpState.value) {
//                                val unconsumed = delta - consumed
//                                if (abs(unconsumed) > 0.5f) {
//                                    val unconsumedDp = with(density) { unconsumed.toDp() }
//                                    val newHeight = (toolbarHeightAnimatable.value + unconsumedDp)
//                                        .coerceIn(
//                                            toolbarConfig.toolbarHeightConfig.collapsed,
//                                            toolbarConfig.toolbarHeightConfig.halfExpanded
//                                        )
//                                    toolbarHeightAnimatable.snapTo(newHeight)
//                                }
//                            }
//                            Log.d("EditModeHandler", "animateDecay end")
//                            lastValue = value
//                        }
//                    }
//            }
//        }
//    }