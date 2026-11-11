package com.ensodai.avalon.presentation.paymentScreen.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.continue_caps_button
import com.ensodai.avalon.generated.resources.plan_status_active_pattern
import com.ensodai.avalon.generated.resources.system_upgraded
import com.ensodai.avalon.generated.resources.upgrade_daily_energy
import com.ensodai.avalon.generated.resources.upgrade_max_capacity
import com.ensodai.avalon.presentation.paymentScreen.viewState.SubscriptionPlan
import com.ensodai.avalon.presentation.paymentScreen.viewState.accentTint
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI

@Composable
fun HexagonTechnoSuccessView(
    plan: SubscriptionPlan,
    onContinueClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    val accentColor = plan.tariff.accentTint()
    val infiniteTransition = rememberInfiniteTransition(label = "corePulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowIntensity"
    )

    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            Button(
                onClick = onContinueClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = colors.textMain
                ),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.7f),
                                accentColor
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.continue_caps_button),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        val width = size.width
                        val height = size.height
                        val hexSize = size.minDimension * 0.55f
                        val radius = hexSize / 2f
                        val centerX = width / 2f
                        val centerY = height / 2f

                        val path = Path().apply {
                            for (i in 0 until 6) {
                                val angleRad = (60.0 * i) * PI / 180.0
                                val x = centerX + radius * kotlin.math.cos(angleRad).toFloat()
                                val y = centerY + radius * kotlin.math.sin(angleRad).toFloat()
                                if (i == 0) moveTo(x, y) else lineTo(x, y)
                            }
                            close()
                        }

                        drawPath(
                            path = path,
                            color = accentColor.copy(alpha = 0.12f * glowIntensity),
                            style = Stroke(width = 54.dp.toPx())
                        )

                        drawPath(
                            path = path,
                            color = accentColor.copy(alpha = 0.22f * glowIntensity),
                            style = Stroke(width = 24.dp.toPx())
                        )

                        drawPath(
                            path = path,
                            color = accentColor.copy(alpha = 0.75f * glowIntensity),
                            style = Stroke(width = 6.dp.toPx())
                        )

                        // 3. РЎРђРњРћ РўР•Р›Рћ РЇР”Р Рђ
                        drawPath(
                            path = path,
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )

                        drawPath(
                            path = path,
                            color = accentColor,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
            ) {
                Icon(
                    imageVector = IconsManager.Bolt,
                    contentDescription = null,
                    tint = colors.textMain,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(Res.string.system_upgraded),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = colors.textMain
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(
                    Res.string.plan_status_active_pattern,
                    plan.title.uppercase()
                ),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = 1.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(Res.string.upgrade_max_capacity, plan.maxCapacity),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colors.textSecondary,
                        fontFamily = FontFamily.Monospace
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(Res.string.upgrade_daily_energy, plan.dailyEnergy),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colors.textSecondary,
                        fontFamily = FontFamily.Monospace
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
