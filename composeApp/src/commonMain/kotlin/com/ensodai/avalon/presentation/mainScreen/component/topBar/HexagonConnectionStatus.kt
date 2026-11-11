package com.ensodai.avalon.presentation.mainScreen.component.topBar

import androidx.compose.animation.animateColor as animateTint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.domain.chatEvents.RpcConnectionState
import com.ensodai.avalon.presentation.commonComponents.drawHexagonPath
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun HexagonConnectionStatus(
    connectionState: RpcConnectionState,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors

    val transition = updateTransition(targetState = connectionState, label = "ConnectionState")

    val color by transition.animateTint(
        label = "Color",
        transitionSpec = { tween(500) }) { state ->
        when (state) {
            is RpcConnectionState.Connected -> colors.onlineStatus
            is RpcConnectionState.Idle -> colors.connectingStatus
            is RpcConnectionState.Error -> colors.error
            is RpcConnectionState.Connecting,
            is RpcConnectionState.Authenticating -> colors.connectingStatus
        }
    }

    val pulseAmplitude by transition.animateFloat(
        label = "PulseAmplitude",
        transitionSpec = { tween(500) }
    ) { state ->
        if (state is RpcConnectionState.Connected) 0f else 1f
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")

    val beatProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "BeatProgress"
    )

    val rippleProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = "Ripple"
    )

    Canvas(modifier = modifier.size(24.dp)) {
        if (pulseAmplitude > 0.01f) {
            val rippleScale = 1f + (rippleProgress * 0.8f)
            val rippleAlpha = (1f - rippleProgress) * 0.5f * pulseAmplitude

            withTransform({
                scale(scale = rippleScale, pivot = center)
            }) {
                drawHexagonPath(
                    color = color.copy(alpha = rippleAlpha),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        val maxScaleGrowth = 0.15f
        val mainScale = 1f + (beatProgress * maxScaleGrowth * pulseAmplitude)

        withTransform({
            scale(scale = mainScale, pivot = center)
        }) {
            drawHexagonPath(
                color = color,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
