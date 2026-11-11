package com.ensodai.avalon.presentation.customizationScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.AvalonThemePresets
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleFill
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.customization_selected
import org.jetbrains.compose.resources.stringResource

/**
 * Компактная карточка темы для горизонтального скролла.
 * Цветной кружок с превью + название снизу.
 */
@Composable
fun ThemePresetCard(
    preset: AvalonThemePreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    val previewColors = AvalonThemePresets.resolve(preset)

    val borderModifier = if (isSelected) {
        Modifier.border(2.dp, colors.neonAccent, RoundedCornerShape(16.dp))
    } else {
        Modifier.border(1.dp, colors.strokeSubtle, RoundedCornerShape(16.dp))
    }

    Column(
        modifier = Modifier
            .width(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardBackground)
            .then(borderModifier)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Цветной кружок — превью темы
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .then(
                        when (val fill = previewColors.userBubble) {
                            is BubbleFill.Solid -> Modifier.background(fill.color)
                            is BubbleFill.Gradient -> Modifier.background(
                                Brush.linearGradient(
                                    listOf(fill.startColor, fill.endColor)
                                )
                            )
                        }
                    )
            )
            // Галочка поверх кружка
            if (isSelected) {
                Icon(
                    imageVector = IconsManager.Check,
                    contentDescription = stringResource(Res.string.customization_selected),
                    modifier = Modifier.size(20.dp),
                    tint = previewColors.userBubbleTextColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Название темы
        Text(
            text = preset.name.replace(Regex("([a-z])([A-Z])"), "$1 $2"),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) colors.neonAccent else colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}