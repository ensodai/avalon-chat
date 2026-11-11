package com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.topBar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager

@Composable
fun ChatButton(
    modifier: Modifier,
    isEnableChatButton: Boolean,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    val alpha by animateFloatAsState(
        targetValue = if (isEnableChatButton) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )
    val scale by animateFloatAsState(
        targetValue = if (isEnableChatButton) 1f else 0.8f,
        animationSpec = tween(durationMillis = 300)
    )

    IconButton(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
            }
            .background(
                color = colors.warningAccent,
                shape = CircleShape
            ),
        onClick = { if (isEnableChatButton) onClick() },
        enabled = isEnableChatButton
    ) {
        Icon(
            imageVector = IconsManager.Chat,
            contentDescription = "Chat Icon",
            tint = colors.textMain,
            modifier = Modifier.size(28.dp)
        )
    }
}
