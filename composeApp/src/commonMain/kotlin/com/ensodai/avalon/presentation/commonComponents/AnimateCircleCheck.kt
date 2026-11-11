package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.theme.AvalonTheme
import kotlin.math.min

@Composable
fun AnimateCircleCheck(
    isSelected: Boolean,
    modifier: Modifier,
    isEnableCircularContour: Boolean = true
) {
    val colors = AvalonTheme.colors
    val animationDuration = 300
    val delayBetweenCircles = 100

    // Размеры кругов
    val accentCircleSize = 22.dp
    val contourCircleSize = 24.dp

    // Анимация увеличения акцентного круга
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "CircleScale"
    )

    // Анимация прорисовки контурного круга по часовой стрелке
    val contourSweepAngle by animateFloatAsState(
        targetValue = if (isSelected) 360f else 0f,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "ContourSweepAngle"
    )

    // Анимация прорисовки галочки с задержкой
    val checkMarkProgress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = delayBetweenCircles,
            easing = LinearOutSlowInEasing
        ),
        label = "CheckMarkProgress"
    )

    // Цвета из темы
    val contourColor = colors.background
    val fillColor = colors.neonAccent
    val checkColor = colors.userBubbleTextColor

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier.size(contourCircleSize)
        ) {

            // Контурный круг: тонкая обводка из темы
            if (isEnableCircularContour) {
                drawArc(
                    color = contourColor,
                    startAngle = 90f,
                    sweepAngle = contourSweepAngle,
                    useCenter = false,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    ),
                    size = Size(size.width, size.height)
                )
            }

            // Акцентный заполненный круг
            drawCircle(
                color = fillColor,
                radius = (accentCircleSize.toPx() / 2) * scale
            )

            // Галочка — белая (на акцентном фоне)
            if (scale > 0f || checkMarkProgress > 0f) {
                drawCheckMark(checkMarkProgress, checkColor)
            }
        }

    }
}


fun DrawScope.drawCheckMark(progress: Float, checkColor: Color = Color.White) {
    // плавную интерполяцию
    val interpolatedProgress = LinearOutSlowInEasing.transform(progress)

    // Центральная точка
    val midX = 0.45f * size.width
    val midY = 0.65f * size.height

    // Точки галочки
    val startX = 0.3f * size.width
    val startY = 0.5f * size.height
    val endX = 0.7f * size.width
    val endY = 0.35f * size.height

    // Прогресс левого сегмента
    val leftSegmentProgress = min(interpolatedProgress * 2, 1f)
    if (leftSegmentProgress > 0f) {
        drawLine(
            color = checkColor,
            start = Offset(
                midX - (midX - startX) * leftSegmentProgress,
                midY - (midY - startY) * leftSegmentProgress
            ),
            end = Offset(midX, midY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    // Прогресс правого сегмента
    if (interpolatedProgress > 0.5f) {
        val rightSegmentProgress = (interpolatedProgress - 0.5f) * 2
        if (rightSegmentProgress > 0f) {
            drawLine(
                color = checkColor,
                start = Offset(midX, midY),
                end = Offset(
                    midX + (endX - midX) * rightSegmentProgress,
                    midY + (endY - midY) * rightSegmentProgress
                ),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
