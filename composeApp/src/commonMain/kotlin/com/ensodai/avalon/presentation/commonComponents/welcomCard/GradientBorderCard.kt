package com.ensodai.avalon.presentation.commonComponents.welcomCard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.ensodai.avalon.theme.AvalonTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Карточка с градиентной обводкой, тенью и закругленными углами.
 *
 * @param modifier Модификатор для настройки внешнего вида и расположения.
 * @param gradientStartColor Начальный цвет градиента для обводки.
 * @param gradientEndColor Конечный цвет градиента для обводки.
 * @param borderWidth Ширина обводки.
 * @param isEnableLight Свечение.
 * @param shape Форма карточки (для обводки, фона, тени и обрезки контента).
 * @param elevation Высота тени.
 * @param spotColor Цвет тени (по умолчанию совпадает с начальным цветом градиента).
 * @param backgroundColor Цвет фона карточки.
 * @param content Содержимое, которое будет отображаться внутри карточки.
 */
@Composable
fun GradientBorderCard(
    modifier: Modifier = Modifier,
    gradientStartColor: Color,
    gradientEndColor: Color,
    borderWidth: Dp = 1.dp,
    isEnableLight: Boolean,
    shape: Shape = RoundedCornerShape(24.dp),
    elevation: Dp = 16.dp,
    spotColor: Color = gradientStartColor,
    backgroundColor: Color = com.ensodai.avalon.theme.AvalonTheme.colors.cardBackground,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .let {
                if (isEnableLight) {
                    it.shadow(
                        elevation = elevation,
                        spotColor = spotColor,
                        shape = shape
                    )
                } else it
            }
            .border(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        gradientStartColor.copy(alpha = 0.8f),
                        gradientEndColor.copy(alpha = 0.8f)
                    )
                ),
                shape = shape
            )
            .background(
                color = backgroundColor,
                shape = shape
            )
            .clip(shape)
    ) {
        content()
    }
}