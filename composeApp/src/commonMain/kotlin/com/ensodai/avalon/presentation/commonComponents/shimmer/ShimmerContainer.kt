package com.ensodai.avalon.presentation.commonComponents.shimmer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun ShimmerContainer(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    cornerShape: Shape = RoundedCornerShape(4.dp),
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier.alpha(if (isLoading) 0f else 1f)
        ) {
            content()
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .technoShimmer(visible = true, shape = cornerShape)
            )
        }
    }
}

fun Modifier.technoShimmer(
    visible: Boolean = true,
    shape: Shape,
    baseColor: Color? = null,
    highlightColor: Color? = null
): Modifier = composed {
    if (!visible) return@composed this
    val colors = AvalonTheme.colors
    val resolvedBaseColor = baseColor ?: colors.strokeSubtle.copy(alpha = 0.7f)
    val resolvedHighlightColor = highlightColor ?: colors.surfaceHigh

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200, // Скорость перелива
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_float"
    )

    val brush = Brush.linearGradient(
        colors = listOf(resolvedBaseColor, resolvedHighlightColor, resolvedBaseColor),
        start = Offset.Zero,
        end = Offset(x = translateAnimation, y = translateAnimation),
        tileMode = TileMode.Mirror
    )

    this
        .clip(shape)
        .background(brush)
}
