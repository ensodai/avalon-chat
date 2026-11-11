package com.ensodai.avalon.presentation.chatVariantScreen.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun DashedRoundedRectBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    onAddImageClick: () -> Unit,
) {
    val colors = AvalonTheme.colors
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .clickable { onAddImageClick() }
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                val dashWidth = 10.dp.toPx()
                val dashGap = 6.dp.toPx()
                val cornerRadiusPx = cornerRadius.toPx()

                drawRoundRect(
                    color = colors.textSecondary,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(this.size.width - strokeWidth, this.size.height - strokeWidth),
                    cornerRadius = CornerRadius(cornerRadiusPx - (strokeWidth / 2)),
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(dashWidth, dashGap),
                            0f
                        )
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = IconsManager.Image,
                contentDescription = "Add image",
                tint = colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
            Icon(
                imageVector = IconsManager.Add,
                contentDescription = "Add image",
                tint = colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
