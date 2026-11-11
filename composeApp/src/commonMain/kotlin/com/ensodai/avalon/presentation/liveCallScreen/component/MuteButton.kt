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
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.livecall_mute_disable
import com.ensodai.avalon.generated.resources.livecall_mute_enable
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun MuteButton(isMuted: Boolean, onClick: () -> Unit) {
    val colors = AvalonTheme.colors
    val bgColor = if (isMuted) colors.mutedAccent.copy(alpha = 0.2f) else colors.neutralButton
    val iconColor = if (isMuted) colors.mutedAccent else colors.textSecondary
    val icon = if (isMuted) IconsManager.MicOff else IconsManager.Mic
    val contentDesc = if (isMuted) stringResource(Res.string.livecall_mute_enable) else stringResource(Res.string.livecall_mute_disable)

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDesc,
            modifier = Modifier.size(24.dp),
            tint = iconColor
        )
    }
}
