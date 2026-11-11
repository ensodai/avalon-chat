package com.ensodai.avalon.presentation.liveCallScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.livecall_end_call
import com.ensodai.avalon.generated.resources.livecall_start_call
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun CallButton(
    isCallActive: Boolean,
    isConnecting: Boolean,
    onStartCall: () -> Unit,
    onStopCall: () -> Unit
) {
    val colors = AvalonTheme.colors
    val bgColor = if (isCallActive) colors.error else colors.accentSecondary
    val alpha = if (isConnecting) 0.5f else 1f
    val icon = if (isCallActive) IconsManager.PhoneOff else IconsManager.Phone
    val contentDesc = if (isCallActive) stringResource(Res.string.livecall_end_call) else stringResource(Res.string.livecall_start_call)

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(bgColor.copy(alpha = alpha))
            .clickable(
                enabled = !isConnecting,
                onClick = { if (isCallActive) onStopCall() else onStartCall() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            modifier = Modifier.size(28.dp),
            tint = colors.textMain
        )
    }
}
