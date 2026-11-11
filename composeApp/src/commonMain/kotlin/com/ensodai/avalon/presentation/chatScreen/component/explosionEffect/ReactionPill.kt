package com.ensodai.avalon.presentation.chatScreen.component.explosionEffect

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ensodai.avalon.theme.AvalonTheme
import kotlin.math.abs

@Composable
fun ReactionPill(
    reaction: ReactionUiModel,
    onClick: () -> Unit,
    onEmojiAnchorChanged: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    // Реакция рядом с пузырём — используем карточный фон и обводки из темы.
    val containerColor = colors.cardBackground.copy(alpha = 0.85f)
    val borderColor = colors.strokeSubtle
    val contentColor = colors.textMain
    val counterColor = colors.textSecondary
    var lastReportedCenter by remember { mutableStateOf<Offset?>(null) }

    Surface(
        modifier = modifier
            .height(28.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        val topLeftInWindow = coordinates.localToWindow(Offset.Zero)
                        val newCenter = Offset(
                            x = topLeftInWindow.x + coordinates.size.width / 2f,
                            y = topLeftInWindow.y + coordinates.size.height / 2f
                        )
                        val previousCenter = lastReportedCenter
                        val hasMeaningfulPositionChange = previousCenter == null ||
                                abs(previousCenter.x - newCenter.x) > 0.5f ||
                                abs(previousCenter.y - newCenter.y) > 0.5f

                        if (hasMeaningfulPositionChange) {
                            lastReportedCenter = newCenter
                            onEmojiAnchorChanged(newCenter)
                        }
                    }
                    .padding(end = if (reaction.count > 1) 4.dp else 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = reaction.emoji,
                    fontSize = 14.sp,
                )
            }

            if (reaction.count > 1) {
                Text(
                    text = reaction.count.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = counterColor,
                    modifier = Modifier.padding(end = 2.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(borderColor, CircleShape)
                        .padding(1.dp)
                ) {
                    val avatarUrl = reaction.avatars.firstOrNull()
                    if (!avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            model = avatarUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
