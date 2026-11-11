package com.ensodai.avalon.presentation.userAccountScreen.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun QuotaIndicator3(
    modifier: Modifier = Modifier,
    currentQuota: Int,
    maxQuota: Int,
    isLoading: Boolean
) {
    val colors = AvalonTheme.colors

    val progress = if (maxQuota > 0) {
        (currentQuota.toFloat() / maxQuota.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val progressAnim = remember { Animatable(0f) }
    val currentQuotaAnim = remember { Animatable(0f) }

    val startColor = colors.error
    val endColor = colors.textMain

    val iconColor = lerp(startColor, endColor, progressAnim.value)

    LaunchedEffect(isLoading, maxQuota, currentQuota) {
        if (!isLoading && maxQuota > 0) {
            progressAnim.animateTo(
                targetValue = progress,
                animationSpec = tween(durationMillis = 1000)
            )
        } else {
            progressAnim.snapTo(0f)
        }
    }

    LaunchedEffect(isLoading, currentQuota) {
        if (!isLoading && maxQuota > 0) {
            currentQuotaAnim.animateTo(
                targetValue = currentQuota.toFloat(),
                animationSpec = tween(durationMillis = 1000)
            )
        } else {
            currentQuotaAnim.snapTo(0f)
        }
    }

    val progressAlignment = object : Alignment {
        override fun align(
            size: IntSize,
            space: IntSize,
            layoutDirection: LayoutDirection
        ): IntOffset {
            val x = (progressAnim.value * space.width - size.width / 2).toInt()
                .coerceIn(0, space.width - size.width)
            return IntOffset(x, 0)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
        ) {
            Row(
                modifier = Modifier.align(progressAlignment),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = IconsManager.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = iconColor
                )
                Text(
                    text = currentQuotaAnim.value.toInt().toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = colors.textMain
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(Color.Transparent)
                .border(
                    width = 4.dp,
                    color = colors.textMain,
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressAnim.value)
                    .height(36.dp)
                    .background(colors.textMain, RoundedCornerShape(4.dp))
            )
        }
    }
}
