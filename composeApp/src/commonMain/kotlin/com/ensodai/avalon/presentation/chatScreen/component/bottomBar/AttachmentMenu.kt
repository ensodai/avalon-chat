package com.ensodai.avalon.presentation.chatScreen.component.bottomBar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager

@Composable
fun AttachmentMenu(
    modifier: Modifier = Modifier,
    onAddSystemInstruction: () -> Unit,
    onOpenGiftsMagazine: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AttachmentMenuItem(
            icon = IconsManager.SystemInstruction,
            label = "Remark",
            onClick = onAddSystemInstruction
        )

        AttachmentMenuItem(
            icon = IconsManager.Gift,
            label = "Gift",
            onClick = onOpenGiftsMagazine
        )
    }
}

@Composable
private fun AttachmentMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(colors.neutralButton)
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = label,
            tint = colors.textMain
        )
    }
}
