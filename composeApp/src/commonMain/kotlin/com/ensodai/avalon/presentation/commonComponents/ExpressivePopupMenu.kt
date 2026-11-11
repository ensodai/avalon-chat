package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.ensodai.avalon.navigation.AvalonBackHandler
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.utils.currentTimeMillis

@Composable
fun ExpressivePopupMenu(
    modifier: Modifier = Modifier,
    isEnableHapticImpact: Boolean = true,
    color: Color = AvalonTheme.colors.overlaySurface,
    borderColor: Color = AvalonTheme.colors.overlayBorder,
    triggerContent: @Composable (onClick: () -> Unit) -> Unit,
    menuContent: @Composable ColumnScope.(onDismiss: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var lastDismissTime by remember { mutableLongStateOf(0L) }

    // Состояние: открылось ли меню ВВЕРХ (true) или ВНИЗ (false)
    // Это определится автоматически внутри PopupPositionProvider
    var isPlacedAbove by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val onTriggerClick = {
        val currentTime = currentTimeMillis()
        if (currentTime - lastDismissTime > 300) {
            if (isEnableHapticImpact) {
                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
            }
            expanded = !expanded
        }
    }

    AvalonBackHandler(enabled = expanded) {
        expanded = false
    }

    val transition = updateTransition(targetState = expanded, label = "MenuTransition")

    val scale by transition.animateFloat(
        transitionSpec = {
            if (targetState) spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.8f)
            else spring(stiffness = Spring.StiffnessHigh, dampingRatio = 1f)
        },
        label = "Scale"
    ) { isOpen -> if (isOpen) 1f else 0.8f }

    val alpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 150) },
        label = "Alpha"
    ) { isOpen -> if (isOpen) 1f else 0f }

    Box(modifier = modifier) {
        triggerContent(onTriggerClick)

        if (expanded || transition.currentState) {

            //  провайдер позиции
            val popupPositionProvider = remember(density) {
                AutoPositionProvider(
                    density = density,
                    onPositionCalculated = { isAbove ->
                        if (isPlacedAbove != isAbove) {
                            isPlacedAbove = isAbove
                        }
                    }
                )
            }

            Popup(
                popupPositionProvider = popupPositionProvider,
                onDismissRequest = {
                    expanded = false
                },
                properties = PopupProperties(
                    focusable = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                // Вычисляем точку трансформации и смещение на основе того, куда выпало меню
                val transformOrigin =
                    if (isPlacedAbove) TransformOrigin(1f, 1f) else TransformOrigin(1f, 0f)

                // Если меню сверху — оно должно "подпрыгнуть" снизу вверх. Если снизу — сверху вниз.
                // Но так как Popup уже смещает нас, нам нужно лишь легкое смещение контента для красоты
                val verticalOffset by transition.animateDp(label = "Offset") { isOpen ->
                    if (!isOpen) {
                        if (isPlacedAbove) 10.dp else (-10).dp
                    } else 0.dp
                }

                val popupShape = RoundedCornerShape(12.dp)

                Column(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                            this.transformOrigin = transformOrigin
                            translationY = verticalOffset.toPx()
                        }
                        .width(IntrinsicSize.Max)
                        .widthIn(min = 160.dp)
                        .clip(popupShape)
                        .background(color)
                        .border(1.dp, borderColor, popupShape)
                        .padding(vertical = 4.dp)
                ) {
                    menuContent { expanded = false }
                }
            }
        }
    }
}


/**
 * Умный провайдер позиции.
 * Пытается расположить меню СНИЗУ справа.
 * Если места снизу не хватает, располагает СВЕРХУ справа.
 */
class AutoPositionProvider(
    private val density: Density,
    private val onPositionCalculated: (Boolean) -> Unit // Callback: true если меню сверху
) : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val margin = with(density) { 8.dp.roundToPx() }

        // Позиция X: Прижимаем к правому краю якоря (anchor)
        var x = anchorBounds.right - popupContentSize.width

        // Если меню уезжает за левый край экрана, сдвигаем
        if (x < 0) x = anchorBounds.left

        // Позиция Y:
        val spaceBelow = windowSize.height - anchorBounds.bottom
        val spaceAbove = anchorBounds.top

        // Логика: По умолчанию вниз. Но если внизу мало места (< высоты меню) И сверху места больше, то вверх.
        val isAbove = spaceBelow < popupContentSize.height && spaceAbove > spaceBelow

        val y = if (isAbove) {
            anchorBounds.top - popupContentSize.height - margin
        } else {
            anchorBounds.bottom + margin
        }

        // Сообщаем UI, куда мы встали
        onPositionCalculated(isAbove)

        return IntOffset(x, y)
    }
}

@Composable
fun ExpressiveMenuItem(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color = Color.Unspecified,
    isDestructive: Boolean = false
) {
    val colors = AvalonTheme.colors
    val baseColor = if (color == Color.Unspecified) colors.textMain else color
    val finalColor = if (isDestructive) colors.error else baseColor

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = finalColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = finalColor
        )
    }
}
