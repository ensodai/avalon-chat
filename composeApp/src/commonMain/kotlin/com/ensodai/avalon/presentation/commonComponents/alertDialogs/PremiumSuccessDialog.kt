package com.ensodai.avalon.presentation.commonComponents.alertDialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.dialog_success_button
import com.ensodai.avalon.presentation.commonComponents.buttons.CompactTechnoButton
import com.ensodai.avalon.theme.AvalonTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.math.min

@Composable
fun PremiumSuccessDialog(
    visible: Boolean,
    title: String,
    subtitle: String,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val colors = AvalonTheme.colors
    val haptic = LocalHapticFeedback.current
    
    // Внутреннее состояние для запуска анимаций ПОСЛЕ входа
    var animateTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animateTrigger = true
        // Запуск автозакрытия через 2200мс
        delay(2200)
        onDismiss()
    }

    // Анимация масштабирования центрального круга с галочкой (Bouncy Spring)
    val checkScale by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "check_scale"
    )

    // Анимация контурного внешнего круга
    val contourSweepAngle by animateFloatAsState(
        targetValue = if (animateTrigger) 360f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "contour_sweep"
    )

    // Анимация прорисовки галочки из центра
    val checkProgress by animateFloatAsState(
        targetValue = if (animateTrigger) 1f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = 150,
            easing = LinearOutSlowInEasing
        ),
        label = "check_progress"
    )

    // Анимации неонового импульса свечения (Glow Ring)
    val glowScale by animateFloatAsState(
        targetValue = if (animateTrigger) 2.2f else 0.4f,
        animationSpec = tween(
            durationMillis = 700,
            delayMillis = 100,
            easing = FastOutLinearInEasing
        ),
        label = "glow_scale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (animateTrigger) 0f else 0.8f,
        animationSpec = tween(
            durationMillis = 700,
            delayMillis = 100,
            easing = LinearEasing
        ),
        label = "glow_alpha"
    )

    // Двойной виброотклик в момент "соприкосновения" и прорисовки галочки
    LaunchedEffect(animateTrigger) {
        if (animateTrigger) {
            delay(100)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(100)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f)) // Мягкое затемнение фона
                .clickable(enabled = false) {}, // Блокируем клики под оверлеем
            contentAlignment = Alignment.Center
        ) {
            // Карточка успеха
            val cardShape = RoundedCornerShape(24.dp)
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .clip(cardShape)
                    .background(colors.overlaySurface)
                    .border(1.dp, colors.strokeSubtle, cardShape)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Анимированная Canvas-область для галочки и импульсного кольца
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer {
                            scaleX = checkScale
                            scaleY = checkScale
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasSize = size.minDimension
                        val center = Offset(size.width / 2, size.height / 2)
                        
                        // 1. Рисуем импульсное неоновое кольцо свечения (Glow Ring)
                        if (glowAlpha > 0.01f) {
                            drawCircle(
                                color = colors.neonAccent.copy(alpha = glowAlpha),
                                radius = (canvasSize / 2) * glowScale,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }

                        // 2. Внешний тонкий контурный круг (по часовой стрелке)
                        drawArc(
                            color = colors.strokeSubtle,
                            startAngle = -90f,
                            sweepAngle = contourSweepAngle,
                            useCenter = false,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            ),
                            size = Size(canvasSize, canvasSize),
                            topLeft = Offset((size.width - canvasSize) / 2, (size.height - canvasSize) / 2)
                        )

                        // 3. Заполненный неоновый круг посередине
                        drawCircle(
                            color = colors.neonAccent,
                            radius = (40.dp.toPx())
                        )

                        // 4. Отрисовка самой галочки
                        if (checkProgress > 0f) {
                            val checkMarkSize = 40.dp.toPx()
                            val interpolatedProgress = LinearOutSlowInEasing.transform(checkProgress)

                            // Центральный изгиб галочки
                            val midX = center.x - 0.05f * checkMarkSize
                            val midY = center.y + 0.15f * checkMarkSize

                            // Начальная и конечная точки
                            val startX = center.x - 0.25f * checkMarkSize
                            val startY = center.y - 0.05f * checkMarkSize
                            val endX = center.x + 0.25f * checkMarkSize
                            val endY = center.y - 0.25f * checkMarkSize

                            val checkColor = colors.userBubbleTextColor

                            // Левый сегмент галочки (вниз-направо)
                            val leftSegmentProgress = min(interpolatedProgress * 2f, 1f)
                            if (leftSegmentProgress > 0f) {
                                drawLine(
                                    color = checkColor,
                                    start = Offset(
                                        midX - (midX - startX) * leftSegmentProgress,
                                        midY - (midY - startY) * leftSegmentProgress
                                    ),
                                    end = Offset(midX, midY),
                                    strokeWidth = 3.5.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }

                            // Правый сегмент галочки (вверх-направо)
                            if (interpolatedProgress > 0.5f) {
                                val rightSegmentProgress = (interpolatedProgress - 0.5f) * 2f
                                if (rightSegmentProgress > 0f) {
                                    drawLine(
                                        color = checkColor,
                                        start = Offset(midX, midY),
                                        end = Offset(
                                            midX + (endX - midX) * rightSegmentProgress,
                                            midY + (endY - midY) * rightSegmentProgress
                                        ),
                                        strokeWidth = 3.5.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Текст Успеха
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = colors.textMain,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Кнопка подтверждения
                CompactTechnoButton(
                    text = stringResource(Res.string.dialog_success_button),
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
