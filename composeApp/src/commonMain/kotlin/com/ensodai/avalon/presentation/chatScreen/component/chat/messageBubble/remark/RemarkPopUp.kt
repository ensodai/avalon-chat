package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.remark

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.presentation.commonComponents.ExpressivePopupMenu
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun RemarkPopUp(
    text: String,
    isVisible: Boolean
) {
    val colors = AvalonTheme.colors
    if (!isVisible) return

    ExpressivePopupMenu(
        modifier = Modifier.padding(end = 8.dp),
        triggerContent = { onClick ->
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(colors.surfaceHigh, CircleShape)
                    .clip(CircleShape)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconsManager.SystemInstruction,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        },
        menuContent = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMain,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .padding(12.dp)
            )
        }
    )

}
