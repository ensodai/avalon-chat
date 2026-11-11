package com.ensodai.avalon.presentation.customizationScreen.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.customization_bubble_colors
import com.ensodai.avalon.generated.resources.customization_default
import com.ensodai.avalon.generated.resources.customization_selected
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.contrastText
import com.ensodai.avalon.theme.models.BubbleColorScheme
import org.jetbrains.compose.resources.stringResource

/**
 * Горизонтальная полоса с цветовыми схемами бабблов.
 * Первый элемент — «Default» (сброс на тему).
 */
@Composable
fun BubbleColorSchemeRow(
    schemes: List<BubbleColorScheme>,
    selectedSchemeId: String?,
    onSchemeSelected: (BubbleColorScheme?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
            text = stringResource(Res.string.customization_bubble_colors),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textMain,

        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Первый элемент — Default (сброс)
            item {
                DefaultColorChip(
                    isSelected = selectedSchemeId == null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSchemeSelected(null)
                    }
                )
            }

            // Цветовые пресеты
            items(schemes, key = BubbleColorScheme::id) { scheme ->
                ColorSchemeChip(
                    scheme = scheme,
                    isSelected = scheme.id == selectedSchemeId,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSchemeSelected(scheme)
                    }
                )
            }
        }
    }
}

/**
 * Чип «Default» — кружок с иконкой автоматической темы.
 */
@Composable
private fun DefaultColorChip(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) colors.neonAccent else colors.textSecondary.copy(alpha = 0.3f),
        animationSpec = tween(200)
    )
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(56.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.cardBackground)
                .border(borderWidth, borderColor, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconsManager.Palette,
                contentDescription = stringResource(Res.string.customization_default),
                modifier = Modifier.size(20.dp),
                tint = if (isSelected) colors.neonAccent else colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(Res.string.customization_default),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) colors.neonAccent else colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Цветной чип с превью: входящий цвет темы + outgoing override.
 */
@Composable
private fun ColorSchemeChip(
    scheme: BubbleColorScheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) colors.neonAccent else colors.textSecondary.copy(alpha = 0.3f),
        animationSpec = tween(200)
    )
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(56.dp)
    ) {
        // Двухцветный кружок
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.botBubble.primaryColor)
                .border(borderWidth, borderColor, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Row(modifier = Modifier.size(44.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(scheme.outgoingColor)
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            if (isSelected) {
                Icon(
                    imageVector = IconsManager.Check,
                    contentDescription = stringResource(Res.string.customization_selected),
                    modifier = Modifier.size(18.dp),
                    tint = scheme.outgoingColor.contrastText()
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = scheme.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) colors.neonAccent else colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
