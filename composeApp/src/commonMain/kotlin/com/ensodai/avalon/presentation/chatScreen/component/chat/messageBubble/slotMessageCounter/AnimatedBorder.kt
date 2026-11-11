package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.slotMessageCounter

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Модификатор, который рисует анимированную обводку вокруг компонента с опциональными
 * эффектами свечения и "кометного" градиента.
 *
 * ## Принцип работы:
 * Анимация управляется параметром `progress`. Она всегда начинается от центральной верхней
 * точки, разделяется на две симметричные части, которые обрисовывают контур компонента,
 * и стремятся сомкнуться в центральной нижней точке.
 *
 * ## Режимы отрисовки:
 * 1.  **Простой режим (по умолчанию):** Если `headColor` не задан (null), обводка рисуется
 *     сплошным цветом `borderColor`.
 * 2.  **"Эпичный" режим ("комета"):** Если задан `headColor`, обводка рисуется с помощью
 *     линейного градиента от `borderColor` (хвост) до `headColor` (голова), что создает
 *     эффект яркого наконечника у движущейся линии.
 *
 * ## Эффект свечения:
 * Если `glowRadius` больше 0, под основной линией рисуется более широкая полупрозрачная
 * копия той же траектории. Это даёт multiplatform-совместимый мягкий ореол без Android API.
 *
 * @param progress Прогресс анимации отрисовки, от 0.0f (ничего не нарисовано) до 1.0f (полностью нарисовано).
 * @param cornerRadius Радиус скругления углов для пути обводки. Должен соответствовать
 *                     радиусу скругления самого компонента для идеального совпадения.
 * @param borderWidth Толщина линии обводки.
 * @param borderColor Основной цвет обводки. В "эпичном" режиме используется как цвет "хвоста" кометы.
 * @param glowRadius Радиус размытия для эффекта свечения. Значение `0.dp` отключает свечение.
 * @param glowColor Опциональный цвет для свечения. Если null, используется `borderColor`.
 * @param headColor Опциональный цвет для "головы" кометы. Если не null, активирует "эпичный"
 *                  режим отрисовки с градиентом.
 */
fun Modifier.animatedBorder(
    progress: Float,
    cornerRadius: Dp,
    borderWidth: Dp,
    borderColor: Color,
    glowRadius: Dp = 0.dp,
    glowColor: Color? = null,
    headColor: Color? = null
): Modifier = this.drawWithContent {
    // Сначала рисуем основное содержимое компонента (фон, текст, и т.д.),
    // чтобы наша обводка рисовалась поверх.
    drawContent()

    // Если анимация не должна быть видима, прекращаем отрисовку.
    if (progress <= 0f) return@drawWithContent

    // Конвертируем Dp в пиксели для использования в Canvas API.
    val cornerRadiusPx = cornerRadius.toPx()
    val borderWidthPx = borderWidth.toPx()
    val glowRadiusPx = glowRadius.toPx()

    // --- Построение полного пути для правой и левой половин обводки ---
    val rightPath = Path()
    rightPath.moveTo(size.width / 2, 0f)
    rightPath.lineTo(size.width - cornerRadiusPx, 0f)
    rightPath.arcTo(
        rect = Rect(
            left = size.width - 2 * cornerRadiusPx,
            top = 0f,
            right = size.width,
            bottom = 2 * cornerRadiusPx
        ),
        startAngleDegrees = 270f,
        sweepAngleDegrees = 90f,
        forceMoveTo = false
    )
    rightPath.lineTo(size.width, size.height - cornerRadiusPx)
    rightPath.arcTo(
        rect = Rect(
            left = size.width - 2 * cornerRadiusPx,
            top = size.height - 2 * cornerRadiusPx,
            right = size.width,
            bottom = size.height
        ),
        startAngleDegrees = 0f,
        sweepAngleDegrees = 90f,
        forceMoveTo = false
    )
    rightPath.lineTo(size.width / 2, size.height)

    val leftPath = Path()
    leftPath.moveTo(size.width / 2, 0f)
    leftPath.lineTo(cornerRadiusPx, 0f)
    leftPath.arcTo(
        rect = Rect(left = 0f, top = 0f, right = 2 * cornerRadiusPx, bottom = 2 * cornerRadiusPx),
        startAngleDegrees = 270f,
        sweepAngleDegrees = -90f,
        forceMoveTo = false
    )
    leftPath.lineTo(0f, size.height - cornerRadiusPx)
    leftPath.arcTo(
        rect = Rect(
            left = 0f,
            top = size.height - 2 * cornerRadiusPx,
            right = 2 * cornerRadiusPx,
            bottom = size.height
        ),
        startAngleDegrees = 180f,
        sweepAngleDegrees = -90f,
        forceMoveTo = false
    )
    leftPath.lineTo(size.width / 2, size.height)

    // Инициализируем PathMeasure, чтобы "отрезать" часть пути в соответствии с прогрессом.
    val pathMeasureRight = PathMeasure()
    pathMeasureRight.setPath(rightPath, false)
    val pathMeasureLeft = PathMeasure()
    pathMeasureLeft.setPath(leftPath, false)

    // Создаем два отдельных пути-сегмента для каждой половины.
    val rightSegment = Path()
    pathMeasureRight.getSegment(
        0f,
        pathMeasureRight.length * progress,
        rightSegment,
        startWithMoveTo = true
    )

    val leftSegment = Path()
    pathMeasureLeft.getSegment(
        0f,
        pathMeasureLeft.length * progress,
        leftSegment,
        startWithMoveTo = true
    )

    // --- ЛОГИКА ОТРИСОВКИ ---

    // 1. Рисуем свечение (если оно включено).
    // Оно рисуется первым, чтобы основная линия была поверх него.
    if (glowRadius > 0.dp) {
        val finalGlowColor = glowColor ?: borderColor
        val combinedGlowPath = Path().apply {
            addPath(rightSegment)
            addPath(leftSegment)
        }
        drawPath(
            path = combinedGlowPath,
            color = finalGlowColor.copy(alpha = 0.22f),
            style = Stroke(
                width = borderWidthPx + glowRadiusPx,
                cap = StrokeCap.Round
            )
        )
    }

    // 2. Рисуем основную линию.
    if (headColor == null) {
        // --- Простой режим: рисуем сплошным цветом ---
        val combinedPath = Path().apply {
            addPath(rightSegment)
            addPath(leftSegment)
        }
        drawPath(
            path = combinedPath,
            color = borderColor,
            style = Stroke(width = borderWidthPx, cap = StrokeCap.Round)
        )
    } else {
        // --- "Эпичный" режим: рисуем градиентом ---
        val gradientColors = listOf(borderColor.copy(alpha = 0.2f), headColor)

        // Кисть для правой стороны: градиент от центра вправо-вниз.
        val rightBrush = Brush.linearGradient(
            colors = gradientColors,
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width, size.height)
        )

        // Кисть для левой стороны: градиент от центра влево-вниз.
        val leftBrush = Brush.linearGradient(
            colors = gradientColors,
            start = Offset(size.width / 2, 0f),
            end = Offset(0f, size.height)
        )

        // Рисуем каждый сегмент своей собственной градиентной кистью.
        drawPath(
            path = rightSegment,
            brush = rightBrush,
            style = Stroke(width = borderWidthPx, cap = StrokeCap.Round)
        )
        drawPath(
            path = leftSegment,
            brush = leftBrush,
            style = Stroke(width = borderWidthPx, cap = StrokeCap.Round)
        )
    }
}
