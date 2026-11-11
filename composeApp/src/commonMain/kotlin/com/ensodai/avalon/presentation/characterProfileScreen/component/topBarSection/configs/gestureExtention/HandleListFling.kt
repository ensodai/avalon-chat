package com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.gestureExtention

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

fun handleListFling(
    velocity: Float,
    listState: LazyListState,
    decaySpec: DecayAnimationSpec<Float>,
    flingJob: MutableState<Job?>,
    coroutineScope: CoroutineScope,
) {
    if (abs(velocity) > 1f) {
        flingJob.value = coroutineScope.launch {
            var lastValue = 0f
            callbackFlow {
                AnimationState(initialValue = 0f, initialVelocity = velocity)
                    .animateDecay(decaySpec) {
                        val delta = value - lastValue
                        trySend(delta)
                        lastValue = value
                        if (!listState.canScrollForward) {
                            cancelAnimation()
                            close()
                        }
                    }
                close()
            }.collect { delta ->
                listState.scrollBy(-delta)
            }
        }
    }
}
