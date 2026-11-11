package com.ensodai.avalon.presentation.addCharacterScreen.component.helloCardStageScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.presentation.commonComponents.welcomCard.CardColor
import com.ensodai.avalon.theme.AvalonTheme

/**
 * Горизонтальный селектор для выбора цвета из списка.
 *
 * @param colors Список цветов для отображения.
 * @param onColorSelected Лямбда-функция, возвращающая выбранный цвет.
 */
@Composable
fun ColorSelector(
    colors: List<CardColor>,
    onColorSelected: (CardColor) -> Unit,
    selectedColor: CardColor
) {
    var selectedColor by remember { mutableStateOf(selectedColor) }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(colors) { color ->
            ColorItem(
                color = color,
                isSelected = (color == selectedColor),
                onClick = {
                    selectedColor = color
                    onColorSelected(color)
                }
            )
        }
    }
}

/**
 * Отображает один кружок с цветом.
 *
 * @param color Цвет для отображения.
 * @param isSelected Указывает, выбран ли этот цвет в данный момент.
 * @param onClick Лямбда-функция, вызываемая при нажатии на кружок.
 */
@Composable
fun ColorItem(
    color: CardColor,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    Box(
        modifier = Modifier
            .size(50.dp)
            .padding(4.dp)
            .clip(CircleShape)
            .background(color.value)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) colors.textMain else Color.Transparent,
                shape = CircleShape
            )
    )
}


@Preview(showBackground = true, backgroundColor = 0xFF2C2C2C)
@Composable
fun ColorSelectorPreview() {
    val sampleColors = CardColor.getSelectableColors()

    ColorSelector(
        colors = sampleColors,
        { selected ->
            println("Выбран цвет: $selected")
        },
        selectedColor = CardColor.DEFAULT
    )
}