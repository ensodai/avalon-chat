package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.domain.sessionWorker.DeletionType
import com.ensodai.avalon.domain.sessionWorker.PendingDeletionItem
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.cancel_big
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.utils.currentTimeMillis
import org.jetbrains.compose.resources.stringResource

@Composable
fun TechnoUndoStack(
    pendingDeletions: List<PendingDeletionItem>,
    onUndo: (PendingDeletionItem) -> Unit,
    onForceDelete: (PendingDeletionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        pendingDeletions.forEach { item ->
            key(item.id) {
                SwipeableUndoCardItem(
                    item = item,
                    onUndo = { onUndo(item) },
                    onForceDelete = { onForceDelete(item) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableUndoCardItem(
    item: PendingDeletionItem,
    onUndo: () -> Unit,
    onForceDelete: () -> Unit
) {

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance -> totalDistance * 0.25f }
    )

    LaunchedEffect(dismissState.currentValue) {
        when (dismissState.currentValue) {
            SwipeToDismissBoxValue.StartToEnd,
            SwipeToDismissBoxValue.EndToStart -> {
                onForceDelete()
            }

            SwipeToDismissBoxValue.Settled -> {
                // Ничего не делаем, карточка на месте
            }
        }
    }

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                // Можно оставить пустым или добавить цвет
                // Box(Modifier.fillMaxSize().background(Color.Transparent))
            },
            content = {
                UndoCardContent(item = item, onUndo = onUndo)
            }
        )
    }
}

@Composable
private fun UndoCardContent(
    item: PendingDeletionItem,
    onUndo: () -> Unit
) {
    val colors = AvalonTheme.colors
    val progressAnim = remember { Animatable(1f) }

    LaunchedEffect(item.id) {
        val currentTime = currentTimeMillis()
        val endTime = item.deletionTimestamp + item.duration
        val remainingTime = (endTime - currentTime).coerceAtLeast(0)

        if (remainingTime > 0) {
            progressAnim.snapTo(1f)
            progressAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = remainingTime.toInt(),
                    easing = LinearEasing
                )
            )
        } else {
            progressAnim.snapTo(0f)
        }
    }

    val capsuleHeight = 48.dp
    val capsuleRadius = 24.dp
    val neonColor = colors.neonAccent.copy(alpha = 0.6f)
    val darkBg = colors.surfaceHigh

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .height(capsuleHeight)
                .widthIn(max = 400.dp),
            shape = RoundedCornerShape(capsuleRadius),
            color = darkBg,
            shadowElevation = 12.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .timerBorder(
                        progress = progressAnim.value,
                        color = neonColor,
                        strokeWidth = 1.5.dp,
                        cornerRadius = capsuleRadius
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (item.type) {
                                DeletionType.CHARACTER -> IconsManager.Person
                                DeletionType.CHAT_SESSION -> IconsManager.ChatBubbleOutline
                                DeletionType.MESSAGE -> IconsManager.Message
                            },
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = item.displayText,
                            color = colors.textMain.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(capsuleRadius))
                            .clickable(onClick = onUndo)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.cancel_big).uppercase(),
                            color = neonColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp // Разрядка букв
                        )
                    }
                }
            }
        }
    }
}

fun Modifier.timerBorder(
    progress: Float,
    color: Color,
    strokeWidth: Dp = 2.dp,
    cornerRadius: Dp = 12.dp
) = drawWithCache {
    // 1. Конвертируем Dp в Px один раз
    val s = strokeWidth.toPx()
    val r = cornerRadius.toPx()
    val w = size.width
    val h = size.height

    // Координаты с учетом отступов (чтобы обводка была внутри)
    val left = s / 2
    val top = s / 2
    val right = w - s / 2
    val bottom = h - s / 2

    // 2. Строим путь ОДИН РАЗ (пока не изменится размер карточки)
    val fullPath = Path().apply {
        // Начинаем ровно с центра
        moveTo(w / 2, top)

        // Идем по часовой стрелке
        lineTo(right - r, top)
        arcTo(
            rect = Rect(left = right - 2 * r, top = top, right = right, bottom = top + 2 * r),
            startAngleDegrees = -90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        lineTo(right, bottom - r)
        arcTo(
            rect = Rect(left = right - 2 * r, top = bottom - 2 * r, right = right, bottom = bottom),
            startAngleDegrees = 0f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        lineTo(left + r, bottom)
        arcTo(
            rect = Rect(left = left, top = bottom - 2 * r, right = left + 2 * r, bottom = bottom),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )
        lineTo(left, top + r)
        arcTo(
            rect = Rect(left = left, top = top, right = left + 2 * r, bottom = top + 2 * r),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 90f,
            forceMoveTo = false
        )

        // 3. ВАЖНО: Замыкаем ровно в центр, без -0.5f.
        // PathMeasure с этим справится.
        lineTo(w / 2, top)
    }

    // Подготавливаем измеритель и временный путь для сегментов
    val pathMeasure = PathMeasure()
    pathMeasure.setPath(fullPath, false)
    val totalLength = pathMeasure.length
    val segmentPath = Path()

    onDrawWithContent {
        drawContent()

        if (progress > 0f) {
            val strokeStyle = Stroke(width = s, cap = StrokeCap.Round)

            // 4. ХИТРОСТЬ: Если прогресс почти полный, рисуем полный путь.
            // Это решает проблему "пролагивания" и дырок в первом кадре.
            if (progress >= 0.995f) {
                drawPath(
                    path = fullPath,
                    color = color,
                    style = strokeStyle
                )
            } else {
                // Рисуем сегмент
                segmentPath.reset()
                // getSegment(start, stop, dst, startWithMoveTo)
                // Рисуем от 0 (верх) до текущего прогресса
                pathMeasure.getSegment(0f, totalLength * progress, segmentPath, true)

                drawPath(
                    path = segmentPath,
                    color = color,
                    style = strokeStyle
                )
            }
        }
    }
}
