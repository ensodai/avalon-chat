package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.theme.AvalonTheme
import androidx.compose.foundation.layout.Arrangement

private val GemPillShape = RoundedCornerShape(999.dp)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GemPill(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    fillContentSize: Boolean = false,
    fillMaxHeight: Boolean = false,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val colors = AvalonTheme.colors

    var pillModifier = modifier
        .clip(GemPillShape)
        .border(
            width = 1.dp,
            color = colors.strokeSubtle,
            shape = GemPillShape
        )

    val isClickable = onClick != null || onLongClick != null

    if (isClickable) {
        pillModifier = pillModifier.combinedClickable(
            enabled = enabled,
            onClick = {
                if (onClick != null) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            },
            onLongClick = onLongClick
        )
    }

    Card(
        modifier = pillModifier,
        shape = GemPillShape,
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = colors.cardElevation
        )
    ) {
        Row(
            modifier = if (fillContentSize) {
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            } else if (fillMaxHeight) {
                Modifier
                    .fillMaxHeight()
                    .padding(contentPadding)
            } else {
                Modifier.padding(contentPadding)
            },
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            content = content
        )
    }
}
