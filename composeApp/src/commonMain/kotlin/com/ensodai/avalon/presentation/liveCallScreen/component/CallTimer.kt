package com.ensodai.avalon.presentation.liveCallScreen.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun CallTimer(seconds: Long, visible: Boolean) {
    val colors = AvalonTheme.colors
    val minutes = seconds / 60
    val secs = seconds % 60
    val text = buildString {
        if (minutes < 10) append('0')
        append(minutes)
        append(':')
        if (secs < 10) append('0')
        append(secs)
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMain.copy(alpha = 0.6f)
        )
    }
}
