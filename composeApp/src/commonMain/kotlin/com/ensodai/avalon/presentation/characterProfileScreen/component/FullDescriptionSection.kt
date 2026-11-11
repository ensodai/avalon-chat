package com.ensodai.avalon.presentation.characterProfileScreen.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.full_description_private
import com.ensodai.avalon.generated.resources.full_description_stage
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun FullDescriptionSection(
    isPublicFullDescription: Boolean,
    fullDescription: String,
    copyToClipboard: () -> Unit
) {
    val colors = AvalonTheme.colors
    var isExpanded by remember { mutableStateOf(false) }

    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ArrowRotation",
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    GemCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onClick = if (isPublicFullDescription) {
            { isExpanded = !isExpanded }
        } else null,
        onLongClick = if (isPublicFullDescription) {
            { copyToClipboard() }
        } else null,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isPublicFullDescription) IconsManager.Description else IconsManager.PrivacyTip,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = stringResource(Res.string.full_description_stage).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.weight(1f)
                )

                if (isPublicFullDescription) {
                    Icon(
                        modifier = Modifier
                            .rotate(rotationState)
                            .size(24.dp),
                        imageVector = IconsManager.ExpandMore,
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isPublicFullDescription) {
                PremiumExpandableText(
                    text = fullDescription,
                    isExpanded = isExpanded,
                    collapsedMaxLines = 4
                )
            } else {
                Text(
                    text = stringResource(Res.string.full_description_private),
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    color = colors.placeholderText
                )
            }
        }
    }
}

@Composable
fun PremiumExpandableText(
    text: String,
    isExpanded: Boolean,
    collapsedMaxLines: Int = 4
) {
    val colors = AvalonTheme.colors
    val density = LocalDensity.current

    val heightAnim = remember { Animatable(0f) }

    var isCalculated by remember { mutableStateOf(false) }

    var collapsedHeightPx by remember { mutableFloatStateOf(0f) }
    var fullHeightPx by remember { mutableFloatStateOf(0f) }


    LaunchedEffect(isExpanded) {

        if (!isCalculated) return@LaunchedEffect

        val target = if (isExpanded) fullHeightPx else collapsedHeightPx
        heightAnim.animateTo(
            targetValue = target,
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioNoBouncy
            )
        )
    }

    val gradientAlpha by animateFloatAsState(
        targetValue = if (isExpanded) 0f else 1f,
        animationSpec = tween(300),
        label = "GradientAlpha"
    )

    Box(
        modifier = Modifier
            .alpha(if (isCalculated) 1f else 0f)
            .fillMaxWidth()
            .height(
                if (isCalculated) with(density) { heightAnim.value.toDp() } else Dp.Unspecified
            )
            .clipToBounds()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textMain,
            lineHeight = 22.sp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.Top, unbounded = true),
            onTextLayout = { layout ->

                val totalHeight = layout.size.height.toFloat()

                val lines = layout.lineCount
                val visibleLines = minOf(lines, collapsedMaxLines)
                val collapsedHeight = layout.getLineBottom(visibleLines - 1)

                fullHeightPx = totalHeight
                collapsedHeightPx = collapsedHeight
            }
        )

        LaunchedEffect(fullHeightPx, collapsedHeightPx) {
            if (fullHeightPx > 0 && collapsedHeightPx > 0 && !isCalculated) {
                val target = if (isExpanded) fullHeightPx else collapsedHeightPx
                heightAnim.snapTo(target)
                isCalculated = true
            } else if (isCalculated) {
                // Р•СЃР»Рё С‚РµРєСЃС‚ РёР·РјРµРЅРёР»СЃСЏ РґРёРЅР°РјРёС‡РµСЃРєРё (РЅР°РїСЂРёРјРµСЂ, РїРµСЂРµРІРѕРґ),
                // РјРѕР¶РЅРѕ С‚СѓС‚ СЃРєРѕСЂСЂРµРєС‚РёСЂРѕРІР°С‚СЊ РІС‹СЃРѕС‚Сѓ, РµСЃР»Рё РЅСѓР¶РЅРѕ.
                // РќРѕ РґР»СЏ СЃС‚Р°С‚РёРєРё СЌС‚Рѕ РЅРµ РѕР±СЏР·Р°С‚РµР»СЊРЅРѕ.
            }
        }

        // РўСѓРјР°РЅ (Р“СЂР°РґРёРµРЅС‚)
        if (isCalculated && (fullHeightPx > collapsedHeightPx + 10f)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(40.dp)
                    .alpha(gradientAlpha)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                colors.cardBackground
                            )
                        )
                    )
            )
        }
    }
}
