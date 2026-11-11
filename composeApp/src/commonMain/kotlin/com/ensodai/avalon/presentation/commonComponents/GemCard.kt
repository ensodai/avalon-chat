package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.theme.AvalonTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GemCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val colors = AvalonTheme.colors

    // Базовый модификатор с формой и границей
    var cardModifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .border(
            width = 1.dp,
            color = colors.strokeSubtle,
            shape = RoundedCornerShape(16.dp)
        )

    // Проверяем, нужно ли вообще делать карточку кликабельной
    val isClickable = onClick != null || onLongClick != null

    if (isClickable) {
        cardModifier = cardModifier.combinedClickable(
            enabled = enabled,
            // Обычный клик
            onClick = {
                if (onClick != null) {
                    // Твой легкий "премиум" клик
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            },
            // Долгий клик
            onLongClick = if (onLongClick != null) {
                {

                    onLongClick()
                }
            } else null
        )
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = colors.cardElevation
        )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}