package com.ensodai.avalon.presentation.liveCallScreen.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.livecall_status_active
import com.ensodai.avalon.generated.resources.livecall_status_connecting
import com.ensodai.avalon.generated.resources.livecall_status_idle
import com.ensodai.avalon.generated.resources.livecall_status_interrupted
import com.ensodai.avalon.generated.resources.livecall_status_reconnecting
import com.ensodai.avalon.generated.resources.livecall_status_stopped
import com.ensodai.avalon.generated.resources.livecall_status_stopping
import com.ensodai.avalon.presentation.liveCallScreen.viewState.LiveCallPhase
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun StatusText(phase: LiveCallPhase) {
    val text = stringResource(getStatusTextRes(phase))
    val color = statusTintForPhase(phase)

    AnimatedContent(
        targetState = text to color,
        transitionSpec = {
            fadeIn(animationSpec = tween(400)) togetherWith
                    fadeOut(animationSpec = tween(400))
        },
        label = "StatusTextTransition"
    ) { (currentText, currentColor) ->
        Text(
            text = currentText,
            style = MaterialTheme.typography.titleLarge,
            color = currentColor
        )
    }
}

@Composable
private fun getStatusTextRes(phase: LiveCallPhase): StringResource = when (phase) {
    LiveCallPhase.IDLE -> Res.string.livecall_status_idle
    LiveCallPhase.CONNECTING -> Res.string.livecall_status_connecting
    LiveCallPhase.ACTIVE -> Res.string.livecall_status_active
    LiveCallPhase.INTERRUPTED -> Res.string.livecall_status_interrupted
    LiveCallPhase.RECONNECTING -> Res.string.livecall_status_reconnecting
    LiveCallPhase.STOPPING -> Res.string.livecall_status_stopping
    LiveCallPhase.STOPPED -> Res.string.livecall_status_stopped
}

@Composable
private fun statusTintForPhase(phase: LiveCallPhase): Color {
    val colors = AvalonTheme.colors
    return when (phase) {
        LiveCallPhase.IDLE -> colors.textSecondary
        LiveCallPhase.CONNECTING -> colors.textMain.copy(alpha = 0.7f)
        LiveCallPhase.ACTIVE -> colors.accentSecondary
        LiveCallPhase.INTERRUPTED -> colors.warningAccent
        LiveCallPhase.RECONNECTING -> colors.warningAccent
        LiveCallPhase.STOPPING -> colors.error.copy(alpha = 0.7f)
        LiveCallPhase.STOPPED -> colors.textSecondary
    }
}
