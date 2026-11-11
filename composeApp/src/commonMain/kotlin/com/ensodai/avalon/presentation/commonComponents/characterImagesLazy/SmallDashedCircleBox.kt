package com.ensodai.avalon.presentation.commonComponents.characterImagesLazy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun SmallDashedCircleBox(
    modifier: Modifier,
    onAddImageClick: () -> Unit,
) {
    val colors = AvalonTheme.colors
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clickable {
                onAddImageClick()
            }
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                val dashWidth = 10.dp.toPx()
                val dashGap = 6.dp.toPx()

                drawCircle(
                    color = colors.textSecondary,
                    center = Offset(size.width / 2, size.height / 2),
                    radius = size.minDimension / 2 - strokeWidth / 2,
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
