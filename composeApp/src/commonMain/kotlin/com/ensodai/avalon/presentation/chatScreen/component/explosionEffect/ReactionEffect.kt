package com.ensodai.avalon.presentation.chatScreen.component.explosionEffect

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private const val DEBUG_ORIGIN_ONLY = false

@Composable
fun ReactionEffect(
    explosionId: Long = -1L,
    emojis: List<String>,
    modifier: Modifier = Modifier,
    origin: Offset? = null,
    config: ExplosionConfig = ExplosionConfig(),
    onFinished: () -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val measuredLayouts = remember(emojis) {
        emojis.map { textMeasurer.measure(text = it, style = TextStyle(fontSize = 24.sp)) }
    }

    val animatable = remember { Animatable(0f) }
    val particles = remember { mutableStateListOf<Particle>() }
    var size by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(explosionId, emojis, size) {
        if (size == IntSize.Zero) return@LaunchedEffect

        particles.clear()
        animatable.snapTo(0f)

        if (measuredLayouts.isNotEmpty()) {
            origin?.x ?: (size.width / 2f)
            origin?.y ?: (size.height / 2f)

            if (DEBUG_ORIGIN_ONLY) {
                delay(config.durationMs.toLong())
                onFinished()
                return@LaunchedEffect
            }

            repeat(config.particleCount) { index ->
                val randomLayout = measuredLayouts.random()
                particles.add(
                    Particle(
                        config = config,
                        textLayout = randomLayout
                    )
                )
            }

            val result = animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = config.durationMs,
                    easing = CubicBezierEasing(0.32f, 0f, 0.67f, 0f)
                )
            )

            if (result.endReason == AnimationEndReason.Finished) {
                onFinished()
            }
        } else {
            onFinished()
        }
    }

    Spacer(
        modifier = modifier
            .onSizeChanged { size = it }
            .drawBehind {
                val progress = animatable.value
                if (progress > 0f) {
                    val currentOrigin = origin ?: Offset(size.width / 2f, size.height / 2f)
                    particles.forEach { p ->
                        p.update(progress)
                        p.draw(this, currentOrigin)
                    }
                }
            }
    )
}
