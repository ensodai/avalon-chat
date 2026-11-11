package com.ensodai.avalon.presentation.userAccountScreen.component

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.shimmer.ShimmerContainer
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.paymentScreen.viewState.UserTariff
import com.ensodai.avalon.presentation.paymentScreen.viewState.accentTint
import com.ensodai.avalon.presentation.paymentScreen.viewState.icon
import com.ensodai.avalon.presentation.paymentScreen.viewState.primaryTint
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.sub_promo_free_title
import com.ensodai.avalon.generated.resources.sub_promo_free_subtitle
import com.ensodai.avalon.generated.resources.sub_promo_active_title
import com.ensodai.avalon.generated.resources.sub_promo_active_subtitle

@Composable
fun TariffPromoCard(
    modifier: Modifier = Modifier,
    loadingState: LoadingState,
    premiumStatus: UserTariff,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    val isFree = premiumStatus == UserTariff.Free

    ShimmerContainer(
        isLoading = loadingState == LoadingState.Loading,
        modifier = modifier.fillMaxWidth(),
        cornerShape = RoundedCornerShape(16.dp)
    ) {
        if (isFree) {
            FreePromoCard(onClick = onClick)
        } else {
            ActiveTariffCard(tariff = premiumStatus, onClick = onClick)
        }
    }
}

@Composable
private fun FreePromoCard(
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors

    // Мягкая анимация свечения обводки для wow-эффекта
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaMultiplier by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    GemCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = true,
        contentPadding = PaddingValues(0.dp) // Убираем дефолтные отступы, чтобы заполнить градиентом
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            colors.upgradeGradientStart.copy(alpha = 0.12f),
                            colors.upgradeGradientEnd.copy(alpha = 0.04f)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            colors.upgradeGradientStart.copy(alpha = 0.8f * alphaMultiplier),
                            colors.upgradeGradientEnd.copy(alpha = 0.8f * alphaMultiplier)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Иконка молнии/активации
                Surface(
                    color = colors.upgradeGradientStart.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = IconsManager.Bolt,
                            contentDescription = null,
                            tint = colors.upgradeGradientStart,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.sub_promo_free_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textMain
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(Res.string.sub_promo_free_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = IconsManager.KeyboardArrowRight,
                    contentDescription = "Upgrade to Premium",
                    tint = colors.upgradeGradientStart,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ActiveTariffCard(
    tariff: UserTariff,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    val tariffColor = tariff.primaryTint()

    GemCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = true,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            tariffColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = colors.strokeSubtle,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = tariffColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = tariff.icon,
                            contentDescription = null,
                            tint = tariffColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(Res.string.sub_promo_active_title, tariff.title.uppercase()),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMain
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(Res.string.sub_promo_active_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = IconsManager.KeyboardArrowRight,
                    contentDescription = "Manage subscription",
                    tint = colors.textSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
