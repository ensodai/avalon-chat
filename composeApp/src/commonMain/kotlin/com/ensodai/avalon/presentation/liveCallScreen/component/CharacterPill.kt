package com.ensodai.avalon.presentation.liveCallScreen.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.livecall_unknown_character
import com.ensodai.avalon.presentation.commonComponents.drawHexagonPath
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun CharacterPill(name: String?, avatarUrl: String?) {
    val colors = AvalonTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(colors.textMain.copy(alpha = 0.08f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (avatarUrl != null) {
            Box(
                modifier = Modifier.size(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(28.dp)) {
                    drawHexagonPath(
                        color = colors.textMain.copy(alpha = 0.6f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                )
            }
        } else {
            Canvas(modifier = Modifier.size(24.dp)) {
                drawHexagonPath(
                    color = colors.textMain.copy(alpha = 0.6f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.padding(horizontal = 8.dp))

        Text(
            text = name ?: stringResource(Res.string.livecall_unknown_character),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMain.copy(alpha = 0.9f)
        )
    }
}