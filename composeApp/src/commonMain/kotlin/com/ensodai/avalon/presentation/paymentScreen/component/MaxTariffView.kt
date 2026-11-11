package com.ensodai.avalon.presentation.paymentScreen.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.max_power
import com.ensodai.avalon.generated.resources.max_tariff_efficiency_description
import com.ensodai.avalon.generated.resources.return_to_base
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.presentation.paymentScreen.viewState.UserTariff
import com.ensodai.avalon.presentation.paymentScreen.viewState.primaryTint
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI

@Composable
fun MaxTariffView(
    onBackClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    val eliteColor = UserTariff.Elite.primaryTint()

    val infiniteTransition = rememberInfiniteTransition(label = "max_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.weight(1f))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(300.dp)
                .scale(pulseScale)
                .drawBehind {
                    val center = center
                    val hexSize = size.minDimension * 0.6f
                    val radius = hexSize / 2f

                    val hexPath = Path().apply {
                        for (i in 0 until 6) {
                            val angleRad = (60.0 * i) * PI / 180.0
                            val x = center.x + radius * kotlin.math.cos(angleRad).toFloat()
                            val y = center.y + radius * kotlin.math.sin(angleRad).toFloat()
                            if (i == 0) moveTo(x, y) else lineTo(x, y)
                        }
                        close()
                    }
                    drawPath(
                        path = hexPath,
                        color = eliteColor.copy(alpha = 0.10f),
                        style = Stroke(width = 42.dp.toPx())
                    )

                    drawPath(
                        path = hexPath,
                        color = eliteColor.copy(alpha = 0.22f),
                        style = Stroke(width = 18.dp.toPx())
                    )

                    drawPath(
                        path = hexPath,
                        color = eliteColor.copy(alpha = 0.80f),
                        style = Stroke(width = 6.dp.toPx())
                    )

                    drawPath(
                        path = hexPath,
                        color = eliteColor,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
        ) {
            Icon(
                imageVector = IconsManager.Star,
                contentDescription = null,
                tint = colors.textMain,
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = stringResource(Res.string.max_power),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = eliteColor
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .border(1.dp, eliteColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .background(eliteColor.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.max_tariff_efficiency_description),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        TechnoButton(
            text = stringResource(Res.string.return_to_base),
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            isPrimary = false,
            onSecondaryColor = colors.textMain,
            customBorderColor = eliteColor.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
