package com.ensodai.avalon.presentation.commonComponents.cardMonolith

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.shared.avalon.character.spec.AvalonCardCustomization
import com.ensodai.shared.avalon.character.spec.CardAccentToken
import com.ensodai.shared.avalon.character.spec.CardBgPattern
import com.ensodai.shared.avalon.character.spec.CardFallbackGlyph
import com.ensodai.shared.avalon.character.spec.CardLayout
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager

@Composable
fun AvalonCardCustomizerSection(
    customization: AvalonCardCustomization,
    onCustomizationChange: (AvalonCardCustomization) -> Unit,
    modifier: Modifier = Modifier,
    hasAvatar: Boolean = true
) {
    val colors = AvalonTheme.colors
    val accentColor = customization.accentToken.toComposeColor()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.cardBackground)
            .border(1.dp, colors.surfaceHigh, RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Заголовок секции
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "⬡",
                color = accentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "СТИЛИЗАЦИЯ DARK MONOLITH",
                color = colors.textMain,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        // 1. Выбор геометрии компоновки
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Геометрия макета",
                color = colors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LayoutOptionChip(
                    title = "Full-Bleed",
                    subtitle = "Арт на весь фон",
                    isSelected = customization.layout == CardLayout.FULL_BLEED,
                    accentColor = accentColor,
                    onClick = {
                        onCustomizationChange(customization.copy(layout = CardLayout.FULL_BLEED))
                    },
                    modifier = Modifier.weight(1f)
                )

                LayoutOptionChip(
                    title = "Рамка",
                    subtitle = "Монолит 1px",
                    isSelected = customization.layout == CardLayout.MONOLITH_FRAME,
                    accentColor = accentColor,
                    onClick = {
                        onCustomizationChange(customization.copy(layout = CardLayout.MONOLITH_FRAME))
                    },
                    modifier = Modifier.weight(1f)
                )

                LayoutOptionChip(
                    title = "Ядро",
                    subtitle = "Гексагон",
                    isSelected = customization.layout == CardLayout.HEX_CORE,
                    accentColor = accentColor,
                    onClick = {
                        onCustomizationChange(customization.copy(layout = CardLayout.HEX_CORE))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 2. Выбор неонового акцента
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Акцентное свечение",
                color = colors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CardAccentToken.entries.forEach { token ->
                    val tokenColor = token.toComposeColor()
                    val isSelected = customization.accentToken == token

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(tokenColor.copy(alpha = if (isSelected) 0.3f else 0.1f))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) tokenColor else tokenColor.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                            .clickable {
                                onCustomizationChange(customization.copy(accentToken = token))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 18.dp else 14.dp)
                                .clip(CircleShape)
                                .background(tokenColor)
                        )
                    }
                }
            }
        }

        // 3. Выбор паттерна фона (для режимов Рамка, Ядро или когда нет арта)
        AnimatedVisibility(
            visible = customization.layout != CardLayout.FULL_BLEED || !hasAvatar
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Текстура фона",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PatternOptionChip(
                        title = "Гекса-сетка",
                        isSelected = customization.bgPattern == CardBgPattern.HEX_GRID,
                        accentColor = accentColor,
                        onClick = {
                            onCustomizationChange(customization.copy(bgPattern = CardBgPattern.HEX_GRID))
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PatternOptionChip(
                        title = "Сканлайны",
                        isSelected = customization.bgPattern == CardBgPattern.SCANLINES,
                        accentColor = accentColor,
                        onClick = {
                            onCustomizationChange(customization.copy(bgPattern = CardBgPattern.SCANLINES))
                        },
                        modifier = Modifier.weight(1f)
                    )

                    PatternOptionChip(
                        title = "Обсидиан",
                        isSelected = customization.bgPattern == CardBgPattern.MATTE_OBSIDIAN,
                        accentColor = accentColor,
                        onClick = {
                            onCustomizationChange(customization.copy(bgPattern = CardBgPattern.MATTE_OBSIDIAN))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Символ ядра (если нет арта или в режимах Рамка / Ядро)
        AnimatedVisibility(visible = !hasAvatar) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Символ ядра (заглушка)",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                val glyphScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(glyphScrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardFallbackGlyph.entries.forEach { glyph ->
                        val isSelected = customization.fallbackGlyph == glyph
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) accentColor.copy(alpha = 0.2f) else colors.surfaceHigh)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) accentColor else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onCustomizationChange(customization.copy(fallbackGlyph = glyph))
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = glyph.displayName,
                                color = if (isSelected) accentColor else colors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // 5. Переключатель телеметрии
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceHigh)
                .clickable {
                    onCustomizationChange(customization.copy(showTelemetry = !customization.showTelemetry))
                }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = IconsManager.Memory,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "Телеметрия в футере",
                        color = colors.textMain,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Показывать счетчики токенов и приветствий",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Switch(
                checked = customization.showTelemetry,
                onCheckedChange = { checked ->
                    onCustomizationChange(customization.copy(showTelemetry = checked))
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = accentColor,
                    checkedTrackColor = accentColor.copy(alpha = 0.3f),
                    uncheckedThumbColor = colors.textSecondary,
                    uncheckedTrackColor = colors.background
                )
            )
        }
    }
}

@Composable
private fun LayoutOptionChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.18f) else colors.surfaceHigh)
            .border(
                width = 1.dp,
                color = if (isSelected) accentColor else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = if (isSelected) accentColor else colors.textMain,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = if (isSelected) accentColor.copy(alpha = 0.8f) else colors.textSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun PatternOptionChip(
    title: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.18f) else colors.surfaceHigh)
            .border(
                width = 1.dp,
                color = if (isSelected) accentColor else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) accentColor else colors.textSecondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
