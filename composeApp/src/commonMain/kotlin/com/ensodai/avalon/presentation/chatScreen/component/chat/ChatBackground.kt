package com.ensodai.avalon.presentation.chatScreen.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.presentation.commonComponents.ShimmerAsyncImage
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.models.ChatBackgroundConfig
import com.ensodai.avalon.theme.models.ChatPatternPreset

@Composable
fun ChatBackground(
    modifier: Modifier = Modifier
) {
    val config = AvalonTheme.colors.chatBackground

    Box(modifier = modifier.fillMaxSize()) {
        when (config) {
            is ChatBackgroundConfig.Default -> {
                Box(modifier = Modifier.fillMaxSize().background(AvalonTheme.colors.background))
            }
            is ChatBackgroundConfig.Solid -> {
                Box(modifier = Modifier.fillMaxSize().background(config.color))
            }
            is ChatBackgroundConfig.Image -> {
                ShimmerAsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(config.blurRadius.dp)
                        .drawWithContent {
                            drawContent()
                            if (config.dimAmount > 0f) {
                                drawRect(Color.Black.copy(alpha = config.dimAmount))
                            }
                        },
                    model = config.url,
                    seed = config.url, // Using URL as seed
                    blurhash = null,
                    contentScale = ContentScale.Crop
                )
            }
            is ChatBackgroundConfig.Pattern -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(config.baseColor)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        when (config.preset) {
                            ChatPatternPreset.Aurora -> drawAuroraPattern(
                                tintColor = config.tintColor,
                                intensity = config.intensity
                            )

                            ChatPatternPreset.DiagonalGrid -> drawDiagonalGridPattern(
                                tintColor = config.tintColor,
                                intensity = config.intensity
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAuroraPattern(
    tintColor: Color,
    intensity: Float
) {
    val alpha = intensity.coerceIn(0f, 1f)
    drawCircle(
        color = tintColor.copy(alpha = alpha),
        radius = size.minDimension * 0.34f,
        center = Offset(size.width * 0.18f, size.height * 0.2f)
    )
    drawCircle(
        color = tintColor.copy(alpha = alpha * 0.62f),
        radius = size.minDimension * 0.42f,
        center = Offset(size.width * 0.85f, size.height * 0.28f)
    )
    drawCircle(
        color = tintColor.copy(alpha = alpha * 0.38f),
        radius = size.minDimension * 0.46f,
        center = Offset(size.width * 0.5f, size.height * 0.9f)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDiagonalGridPattern(
    tintColor: Color,
    intensity: Float
) {
    val lineColor = tintColor.copy(alpha = intensity.coerceIn(0f, 1f))
    val spacing = size.minDimension / 7f
    var startX = -size.height
    while (startX < size.width + size.height) {
        drawLine(
            color = lineColor,
            start = Offset(startX, 0f),
            end = Offset(startX + size.height, size.height),
            strokeWidth = 1.dp.toPx()
        )
        startX += spacing
    }

    drawRect(
        color = tintColor.copy(alpha = intensity * 0.08f),
        topLeft = Offset(size.width * 0.08f, size.height * 0.12f),
        size = Size(size.width * 0.28f, size.height * 0.18f)
    )
    drawRect(
        color = tintColor.copy(alpha = intensity * 0.06f),
        topLeft = Offset(size.width * 0.58f, size.height * 0.62f),
        size = Size(size.width * 0.24f, size.height * 0.16f)
    )
}
