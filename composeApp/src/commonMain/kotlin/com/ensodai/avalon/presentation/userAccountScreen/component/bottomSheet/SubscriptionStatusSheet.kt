package com.ensodai.avalon.presentation.userAccountScreen.component.bottomSheet

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.sub_current_plan
import com.ensodai.avalon.generated.resources.sub_manage_market
import com.ensodai.avalon.generated.resources.sub_status_active
import com.ensodai.avalon.generated.resources.sub_upgrade_btn
import com.ensodai.avalon.generated.resources.sub_upgrade_title
import com.ensodai.avalon.generated.resources.sub_valid_until
import com.ensodai.avalon.generated.resources.upgrade_in_development
import com.ensodai.avalon.presentation.commonComponents.buttons.TechnoButton
import com.ensodai.avalon.presentation.commonComponents.toastNotify
import com.ensodai.avalon.presentation.paymentScreen.viewState.UserTariff
import com.ensodai.avalon.presentation.paymentScreen.viewState.primaryTint
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.utils.toUiString
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionStatusSheet(
    tariff: UserTariff,
    onDismiss: () -> Unit,
    onManageClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    nextBillingDate: Instant?
) {
    val colors = AvalonTheme.colors
    val tariffColor = tariff.primaryTint()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val toastNotify = toastNotify()
    val notifyDevelopmentText = stringResource(Res.string.upgrade_in_development)
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = colors.overlaySurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.dragHandle) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = stringResource(Res.string.sub_current_plan).uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = colors.textSecondary,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            PremiumTariffIcon(
                tariff = tariff
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = tariff.name.uppercase(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = tariffColor,
                    letterSpacing = 2.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            ContainerInfoRow(
                icon = IconsManager.CheckCircle,
                text = stringResource(Res.string.sub_status_active),
                tint = colors.successAccent
            )

            Spacer(modifier = Modifier.height(8.dp))

            ContainerInfoRow(
                icon = IconsManager.CalendarToday,
                text = stringResource(Res.string.sub_valid_until, nextBillingDate.toUiString()),
                tint = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider(color = colors.strokeSubtle.copy(alpha = 0.6f))

            Spacer(modifier = Modifier.height(24.dp))

            if (tariff != UserTariff.Elite) {
                Text(
                    text = stringResource(Res.string.sub_upgrade_title),
                    style = MaterialTheme.typography.bodyMedium.copy(color = colors.textMain),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                TechnoButton(
                    text = stringResource(Res.string.sub_upgrade_btn),
                    onClick = {
//                        onDismiss()
                        toastNotify(notifyDevelopmentText)
//                        onUpgradeClick()
                    },
                    isPrimary = true,
                    gradient = Brush.horizontalGradient(
                        colors = listOf(
                            colors.upgradeGradientStart,
                            colors.upgradeGradientEnd
                        )
                    ),
                    onPrimaryColor = colors.textMain,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            TechnoButton(
                text = stringResource(Res.string.sub_manage_market),
                onClick = onManageClick,
                isPrimary = false,
                onSecondaryColor = colors.textMain,
                customBorderColor = colors.strokeActive.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ContainerInfoRow(icon: ImageVector, text: String, tint: Color) {
    val colors = AvalonTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
        )
    }
}

@Composable
private fun PremiumTariffIcon(tariff: UserTariff) {
    val colors = AvalonTheme.colors
    val tariffColor = tariff.primaryTint()
    val infiniteTransition = rememberInfiniteTransition(label = "sheet_pulse")

    val breathProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    val pulseAlpha = 0.5f + (0.5f * breathProgress)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(110.dp)
            .graphicsLayer { clip = false }
            .drawBehind {
                center
                val baseRadius = size.minDimension / 2

                val mainScale = 1.0f + (0.10f * breathProgress)
                val currentRadius = baseRadius * mainScale

                val decorScale = 1.0f + (0.04f * breathProgress)
                val currentDecorRadius = (baseRadius * 0.75f) * decorScale

                drawCircle(
                    color = tariffColor.copy(alpha = 0.12f * pulseAlpha),
                    radius = currentRadius,
                    style = Stroke(width = 18.dp.toPx() + (6.dp.toPx() * breathProgress))
                )

                drawCircle(
                    color = tariffColor.copy(alpha = 0.28f * pulseAlpha),
                    radius = currentRadius,
                    style = Stroke(width = 8.dp.toPx() + (2.dp.toPx() * breathProgress))
                )

                drawCircle(
                    color = tariffColor.copy(alpha = 0.80f),
                    radius = currentRadius,
                    style = Stroke(width = 3.dp.toPx())
                )

                drawCircle(
                    color = tariffColor.copy(alpha = 0.6f),
                    radius = currentRadius,
                    style = Stroke(width = 1.dp.toPx())
                )

                drawCircle(
                    color = colors.textMain.copy(alpha = 0.15f),
                    radius = currentDecorRadius,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 15f), 0f)
                    )
                )
            }
    ) {
        val iconScale = 1.0f + (0.05f * breathProgress)

        Icon(
            imageVector = when (tariff) {
                UserTariff.Elite -> IconsManager.Star
                UserTariff.Crystal -> IconsManager.Diamond
                else -> IconsManager.Bolt
            },
            contentDescription = null,
            tint = colors.textMain,
            modifier = Modifier
                .size(48.dp)
                .scale(iconScale)
        )
    }
}
