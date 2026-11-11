package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.presentation.commonComponents.AnimateCircleCheck
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun AnimateCircleCheckMessage(
    modifier: Modifier = Modifier,
    isShow: Boolean,
    isSelected: Boolean,
) {

    val offsetXCircleCheck by
    animateDpAsState(if (isShow) 8.dp else (-36).dp, label = "")

    Box(
        modifier = modifier
            .offset { IntOffset(offsetXCircleCheck.roundToPx(), 0) }
    ) {
        if (isShow) {
            TransparentCenterCircle(isEnable = isSelected)
        }
        AnimateCircleCheck(
            isSelected = isSelected,
            isEnableCircularContour = false,
            modifier = Modifier
        )
    }
}

@Composable
private fun TransparentCenterCircle(
    modifier: Modifier = Modifier,
    isEnable: Boolean
) {
    val colors = AvalonTheme.colors

    val circleDiameter = 24.dp
    val strokeWidth = 1.5.dp

    val scale by animateFloatAsState(
        targetValue = if (isEnable) 0f else 1f,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        modifier = modifier.size(circleDiameter),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = (size.minDimension / 2f - strokeWidth.toPx() / 2f) * scale
            if (radius > 0f) {
                drawCircle(
                    color = colors.strokeActive,
                    radius = radius,
                    style = Stroke(width = strokeWidth.toPx())
                )
            }
        }
    }
}

