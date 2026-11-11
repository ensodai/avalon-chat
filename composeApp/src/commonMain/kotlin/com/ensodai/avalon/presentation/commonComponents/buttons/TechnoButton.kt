package com.ensodai.avalon.presentation.commonComponents.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun TechnoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    // Цвета из темы по умолчанию
    primaryColor: Color = AvalonTheme.colors.textMain,
    onPrimaryColor: Color = AvalonTheme.colors.background,
    secondaryColor: Color = AvalonTheme.colors.cardBackground,
    onSecondaryColor: Color = AvalonTheme.colors.textSecondary,
    gradient: Brush? = null,
    customBorderColor: Color? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        label = "button_scale"
    )

    val haptic = LocalHapticFeedback.current
    LaunchedEffect(isPressed) {
        if (isPressed) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val containerColor = if (!enabled) {
        if (isPrimary) AvalonTheme.colors.strokeSubtle else secondaryColor
    } else {
        when {
            gradient != null -> secondaryColor
            isPrimary -> primaryColor
            else -> secondaryColor
        }
    }

    val contentColor = if (!enabled) {
        AvalonTheme.colors.textSecondary.copy(alpha = 0.5f)
    } else {
        if (isPrimary) onPrimaryColor else onSecondaryColor
    }

    val borderStroke = if (!isPrimary && enabled) {
        BorderStroke(1.dp, customBorderColor ?: AvalonTheme.colors.strokeActive)
    } else null

    val shape = RoundedCornerShape(14.dp)

    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (gradient != null && enabled) {
                    Modifier.background(brush = gradient, shape = shape)
                } else {
                    Modifier
                }
            ),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = AvalonTheme.colors.surfaceHigh,
            disabledContentColor = AvalonTheme.colors.textSecondary.copy(alpha = 0.5f)
        ),
        border = borderStroke,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.5.dp,
                trackColor = contentColor.copy(alpha = 0.3f)
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}
