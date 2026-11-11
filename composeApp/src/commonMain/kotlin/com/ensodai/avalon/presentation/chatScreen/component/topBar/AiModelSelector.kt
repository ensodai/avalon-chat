package com.ensodai.avalon.presentation.chatScreen.component.topBar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.presentation.commonComponents.ExpressivePopupMenu
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import kotlinx.coroutines.launch

data class AiModel(
    val id: String,
    val name: String,
    val cost: Int
)

@Composable
fun AiModelSelector(
    currentModel: AvailableLlmConfigs,
    availableModels: List<AvailableLlmConfigs>,
    onModelSelected: (AvailableLlmConfigs) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AvalonTheme.colors
    val iconScale = remember { Animatable(1f) }
    val iconFlash = remember { Animatable(0f) }

    LaunchedEffect(currentModel) {
        val intensity = (currentModel.price / 10f).coerceIn(0.2f, 1f)

        launch {
            iconFlash.snapTo(0.8f * intensity)
            iconFlash.animateTo(0f, tween(durationMillis = 300))
        }

        launch {
            iconScale.animateTo(
                targetValue = 1f + (0.5f * intensity),
                animationSpec = spring(stiffness = 500f + (500f * intensity), dampingRatio = 0.5f)
            )
            iconScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(stiffness = 600f, dampingRatio = 0.6f)
            )
        }
    }

    val energyColor = colors.successAccent
    val animatedIconColor = lerp(energyColor, colors.textMain, iconFlash.value)
    val haptic = LocalHapticFeedback.current

    ExpressivePopupMenu(
        modifier = modifier,
        isEnableHapticImpact = true,
        triggerContent = { onTriggerClick ->
            val baseEnergyColor = colors.successAccent
            val borderColor = lerp(
                baseEnergyColor.copy(alpha = 0.3f),
                baseEnergyColor,
                iconFlash.value
            )
            val backgroundColor = colors.overlaySurface.copy(alpha = 0.5f)

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(50)
                    )
                    .background(backgroundColor)
                    .clickable { onTriggerClick() }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = IconsManager.Bolt,
                    contentDescription = null,
                    tint = animatedIconColor,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer {
                            scaleX = iconScale.value
                            scaleY = iconScale.value
                        }
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = currentModel.price.toString(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = colors.textMain,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(y = (-1).dp)
                )
            }
        },
        menuContent = { onDismiss ->
            availableModels.forEach { model ->
                val isSelected = model == currentModel
                val iconTint = if (isSelected) energyColor else colors.textMain

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onModelSelected(model)
                            onDismiss()
                            if (currentModel != model) {
                                haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = model.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMain,
                        modifier = Modifier.padding(end = 24.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = model.price.toString(),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.textMain
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = IconsManager.Bolt,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    )
}
