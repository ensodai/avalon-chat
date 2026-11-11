package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun SegmentedProgressBar(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.Unspecified,
    inactiveColor: Color = Color.Unspecified,
    animationDuration: Int = 300,
    expandedWeightFactor: Float = 1.3f,
    segmentHeight: Dp = 8.dp
) {
    val colors = AvalonTheme.colors
    val resolvedActive = if (activeColor == Color.Unspecified) colors.textMain else activeColor
    val resolvedInactive = if (inactiveColor == Color.Unspecified) colors.textMain.copy(alpha = 0.3f) else inactiveColor
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (step in 1..totalSteps) {

            val animatedWeight by animateFloatAsState(
                targetValue = if (step == currentStep) expandedWeightFactor else 1f,
                animationSpec = tween(durationMillis = animationDuration)
            )

            val segmentColor by animateColorAsState(
                targetValue = when {
                    step < currentStep -> resolvedActive.copy(alpha = 0.7f)
                    step == currentStep -> resolvedActive
                    else -> resolvedInactive
                },
                animationSpec = tween(durationMillis = animationDuration)
            )

            Box(
                modifier = Modifier
                    .weight(animatedWeight)
                    .height(segmentHeight)
                    .background(segmentColor, shape = RoundedCornerShape(4.dp))
            )
        }
    }
}
