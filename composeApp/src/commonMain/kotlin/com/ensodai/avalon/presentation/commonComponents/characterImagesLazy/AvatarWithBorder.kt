package com.ensodai.avalon.presentation.commonComponents.characterImagesLazy

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun AvatarWithBorder(
    modifier: Modifier = Modifier,
    image: String?,
    isSelected: Boolean = false,
    onSelectImage: () -> Unit,
    onDeleteImage: () -> Unit,
) {
    val colors = AvalonTheme.colors
    val scale by animateDpAsState(
        targetValue = if (isSelected) 90.dp else 80.dp,
        animationSpec = tween(durationMillis = 300)
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 30.dp else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        modifier = modifier
            .drawBehind {
                if (isSelected) {
                    val strokeWidthPx = 4.dp.toPx()
                    val yOffset = scale.toPx() + 8.dp.toPx()
                    val startX = (size.width - borderWidth.toPx()) / 2f
                    drawRoundRect(
                        color = colors.strokeSubtle.copy(alpha = 0.3f),
                        topLeft = Offset(startX, yOffset),
                        size = size.copy(width = borderWidth.toPx(), height = strokeWidthPx),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }
    ) {
        AsyncImage(
            model = image,
            contentDescription = "Character Avatar",
            modifier = Modifier
                .size(scale)
                .clip(CircleShape)
                .clickable {
                    onSelectImage()
                },
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(colors.textMain.copy(alpha = 0.6f))
                .clickable { onDeleteImage() }
                .align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = IconsManager.Close,
                contentDescription = "Detach file",
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.Center)
            )
        }
    }
}
