package com.ensodai.avalon.presentation.customizationScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.models.BubbleShapeType
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.generated.resources.*
import kotlin.math.roundToInt
import com.ensodai.avalon.theme.IconsManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.ensodai.avalon.theme.models.BubbleColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BubbleStyleSettings(
    fontSize: Int,
    shapeType: BubbleShapeType?,
    cornerRadius: Int?,
    onFontSizeChanged: (Int) -> Unit,
    onFontSizeDragging: (Int) -> Unit,
    onShapeTypeChanged: ((BubbleShapeType) -> Unit)?,
    onCornerRadiusChanged: ((Int) -> Unit)?,
    onCornerRadiusDragging: ((Int) -> Unit)?,
    availableBubbleColorSchemes: List<BubbleColorScheme> = emptyList(),
    selectedBubbleColorSchemeId: String? = null,
    onBubbleColorSchemeSelected: ((BubbleColorScheme?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    val haptic = LocalHapticFeedback.current

    // Локальный стейт для плавной работы слайдеров без лага
    var localFontSize by remember(fontSize) { mutableFloatStateOf(fontSize.toFloat()) }
    var localCornerRadius by remember(cornerRadius) { mutableFloatStateOf((cornerRadius ?: 12).toFloat()) }

    var lastFontSizeStep by remember(fontSize) { mutableIntStateOf(fontSize) }
    var lastCornerStep by remember(cornerRadius) { mutableIntStateOf(cornerRadius ?: 12) }

    val currentFontSize = localFontSize.roundToInt()
    val currentCornerRadius = localCornerRadius.roundToInt()

    val sliderColors = SliderDefaults.colors(
        thumbColor = colors.neonAccent,
        activeTrackColor = colors.neonAccent,
        inactiveTrackColor = colors.neonAccent.copy(alpha = 0.15f)
    )

    Column(modifier = modifier) {
        var isExpanded by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isExpanded = !isExpanded
                }
                .padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(Res.string.customization_advanced_settings),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textSecondary
            )
            Icon(
                imageVector = if (isExpanded) IconsManager.KeyboardArrowUp else IconsManager.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textSecondary.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))

                GemCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // --- Font Size ---
                    SectionLabel(label = stringResource(Res.string.customization_font_size), value = "${currentFontSize}sp")
                    Slider(
                        value = localFontSize,
                        onValueChange = { newValue ->
                            localFontSize = newValue
                            val step = newValue.roundToInt()
                            if (step != lastFontSizeStep) {
                                lastFontSizeStep = step
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onFontSizeDragging(step)
                            }
                        },
                        onValueChangeFinished = {
                            onFontSizeChanged(localFontSize.roundToInt())
                        },
                        valueRange = 12f..30f,
                        colors = sliderColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (availableBubbleColorSchemes.isNotEmpty() && onBubbleColorSchemeSelected != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        BubbleColorSchemeRow(
                            schemes = availableBubbleColorSchemes,
                            selectedSchemeId = selectedBubbleColorSchemeId,
                            onSchemeSelected = onBubbleColorSchemeSelected
                        )
                    }

                    if (shapeType != null && onShapeTypeChanged != null) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // --- Bubble Shape ---
                        SectionLabel(
                            label = stringResource(Res.string.customization_shape),
                            value = when (shapeType) {
                                BubbleShapeType.Tail -> stringResource(Res.string.customization_shape_tail)
                                BubbleShapeType.Rounded -> stringResource(Res.string.customization_shape_rounded)
                                BubbleShapeType.SoftCorner -> stringResource(Res.string.customization_shape_soft_corner)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BubbleShapeType.entries.forEach { type ->
                                ShapeOptionChip(
                                    label = when (type) {
                                        BubbleShapeType.Tail -> stringResource(Res.string.customization_shape_tail)
                                        BubbleShapeType.Rounded -> stringResource(Res.string.customization_shape_rounded)
                                        BubbleShapeType.SoftCorner -> stringResource(Res.string.customization_shape_soft)
                                    },
                                    isSelected = type == shapeType,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onShapeTypeChanged(type)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    if (cornerRadius != null && onCornerRadiusChanged != null && onCornerRadiusDragging != null) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // --- Corner Radius ---
                        SectionLabel(label = stringResource(Res.string.customization_corner_radius), value = "${currentCornerRadius}dp")
                        Slider(
                            value = localCornerRadius,
                            onValueChange = { newValue ->
                                localCornerRadius = newValue
                                val step = newValue.roundToInt()
                                if (step != lastCornerStep) {
                                    lastCornerStep = step
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onCornerRadiusDragging(step)
                                }
                            },
                            onValueChangeFinished = {
                                onCornerRadiusChanged(localCornerRadius.roundToInt())
                            },
                            valueRange = 4f..16f,
                            colors = sliderColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(label: String, value: String) {
    val colors = AvalonTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMain
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun ShapeOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    val bg = if (isSelected) colors.neonAccent.copy(alpha = 0.15f) else colors.surfaceHigh
    val borderColor = if (isSelected) colors.neonAccent else colors.textSecondary.copy(alpha = 0.3f)
    val textColor = if (isSelected) colors.neonAccent else colors.textMain

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}
