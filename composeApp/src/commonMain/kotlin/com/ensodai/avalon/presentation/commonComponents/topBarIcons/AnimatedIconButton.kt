package com.ensodai.avalon.presentation.commonComponents.topBarIcons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedIconButton(
    isVisible: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    animationDuration: Int = 120
) {
    val transition = updateTransition(
        targetState = isVisible,
        label = "Icon Transition"
    )

    val animationValue by transition.animateFloat(
        transitionSpec = { tween(durationMillis = animationDuration) },
        label = "Scale Animation"
    ) { state ->
        if (state) 1f else 0f
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(animationDuration)) +
                expandHorizontally(
                    animationSpec = tween(animationDuration),
                    expandFrom = Alignment.End,
                    clip = false
                ),
        exit = fadeOut(animationSpec = tween(animationDuration)) +
                shrinkHorizontally(
                    animationSpec = tween(animationDuration),
                    shrinkTowards = Alignment.End,
                    clip = false
                )
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .requiredSize(40.dp)
                .graphicsLayer(
                    scaleX = animationValue,
                    scaleY = animationValue,
                    alpha = animationValue
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription
            )
        }
    }
}
