package com.ensodai.avalon.presentation.liveCallScreen.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.presentation.commonComponents.drawHexagonPath

@Composable
fun HexRippleRings(color: Color, active: Boolean, visible: Boolean = true) {
    val ring1 = remember { Animatable(0f) }
    val ring2 = remember { Animatable(0f) }
    val ring3 = remember { Animatable(0f) }

    val duration = if (active) 2000 else 3000

    LaunchedEffect(active) {
        listOf(ring1, ring2, ring3).forEach { ring ->
            ring.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = duration, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    Canvas(modifier = Modifier.size(200.dp)) {
        // Если не visible — просто не рисуем ничего
        // Canvas остаётся в layout с фиксированным размером, предотвращая "подпрыгивание"
        if (!visible) return@Canvas

        val center = this.center
        val maxRadius = size.minDimension / 2

        listOf(ring1, ring2, ring3).forEachIndexed { _, ring ->
            val progress = ring.value
            val radiusPx = 60.dp.toPx()
            val radius = radiusPx + (maxRadius - radiusPx) * progress
            val alpha = 0.4f * (1f - progress)

            withTransform({
                scale(scale = radius / radiusPx, pivot = center)
            }) {
                drawHexagonPath(
                    color = color.copy(alpha = alpha),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
