package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun BaseItemScaleAnimation(
    isVisible: Boolean,
    animationDuration: Int = 120,
    content: @Composable () -> Unit
) {
    val transition = updateTransition(
        targetState = isVisible,
        label = "Basic Item Transition"
    )

    val animationValue by transition.animateFloat(
        transitionSpec = { tween(durationMillis = animationDuration) },
        label = "Scale Animation"
    ) { state ->
        if (state) 1f else 0f
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(animationDuration)),
        exit = fadeOut(animationSpec = tween(animationDuration))
    ) {
        Box(
            modifier = Modifier.graphicsLayer(
                scaleX = animationValue,
                scaleY = animationValue,
                alpha = animationValue
            ),

            ) {
            content()
        }
    }
}

@Composable
fun BaseItemAlphaAnimation(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    animationDuration: Int = 120,
    content: @Composable BoxScope.() -> Unit
) {
    val transition = updateTransition(
        targetState = isVisible,
        label = "Basic Item Transition"
    )

    val animationValue by transition.animateFloat(
        transitionSpec = { tween(durationMillis = animationDuration) },
        label = "Scale Animation"
    ) { state ->
        if (state) 1f else 0f
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(animationDuration)),
        exit = fadeOut(animationSpec = tween(animationDuration))
    ) {
        Box(modifier = Modifier.graphicsLayer(alpha = animationValue)) {
            content()
        }
    }
}

@Composable
fun BaseItemScaleUpToDownAnimation(
    isVisible: Boolean,
    animationDuration: Int = 300,
    content: @Composable () -> Unit
) {
    val transition = updateTransition(
        targetState = isVisible,
        label = "Basic Item Transition"
    )

    val animationValue by transition.animateFloat(
        transitionSpec = { tween(durationMillis = animationDuration) },
        label = "Scale Animation"
    ) { state ->
        if (state) 1f else 0f
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(animationSpec = tween(durationMillis = 300)) + fadeIn(),
        exit = shrinkVertically(animationSpec = tween(durationMillis = 300)) + fadeOut()
    ) {
        Box(
            modifier = Modifier.graphicsLayer(
                scaleY = animationValue,
                alpha = animationValue
            ),

            ) {
            content()
        }
    }
}

@Composable
fun BaseRotateAnimation(isRotated: Boolean, content: @Composable () -> Unit) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRotated) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "rotationAngle"
    )

    Box(
        modifier = Modifier.rotate(rotationAngle)
    ) {
        content()
    }
}
