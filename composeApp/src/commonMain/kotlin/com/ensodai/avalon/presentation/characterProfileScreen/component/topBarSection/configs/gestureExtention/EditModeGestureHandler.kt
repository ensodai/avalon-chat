package com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.gestureExtention

import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class EditModeGestureHandler(
    private val listState: LazyListState,
    private val velocityTracker: VelocityTracker,
    private val coroutineScope: CoroutineScope,
    private val decaySpec: DecayAnimationSpec<Float>,
    private val flingJob: MutableState<Job?>,
) : GestureHandler {
    override fun onVerticalDrag(change: PointerInputChange, verticalDragAmount: Float) {
        velocityTracker.addPosition(change.uptimeMillis, change.position)
        coroutineScope.launch {
            listState.scrollBy(-verticalDragAmount)
        }
    }

    override suspend fun onDragEnd() {
        val velocity = velocityTracker.calculateVelocity().y
        handleListFling(
            velocity = velocity,
            listState = listState,
            decaySpec = decaySpec,
            flingJob = flingJob,
            coroutineScope = coroutineScope,
        )
    }
}