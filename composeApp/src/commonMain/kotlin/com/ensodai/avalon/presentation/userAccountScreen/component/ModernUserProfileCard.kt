package com.ensodai.avalon.presentation.userAccountScreen.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.ShimmerAsyncImage
import com.ensodai.avalon.presentation.commonComponents.shimmer.ShimmerContainer
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.paymentScreen.viewState.UserTariff
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.presentation.paymentScreen.viewState.primaryTint
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun ModernUserProfileCard(
    modifier: Modifier = Modifier,
    loadingState: LoadingState,
    userAvatar: String,
    userName: String,
    userNickName: String,
    premiumStatus: UserTariff,
    onClick: () -> Unit,
    onCopyNick: () -> Unit
) {
    val colors = AvalonTheme.colors
    val avatarSeed = remember(userAvatar, userNickName, userName) {
        when {
            userAvatar.isNotBlank() -> userAvatar
            userNickName.isNotBlank() -> userNickName
            else -> userName
        }
    }

    GemCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = true,
        contentPadding = PaddingValues(16.dp)
    ) {
        Box {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center) {

                    ShimmerContainer(
                        isLoading = loadingState == LoadingState.Loading,
                        modifier = Modifier.wrapContentSize(),
                        cornerShape = CircleShape
                    ) {
                        TechnoGlowingAvatar(
                            imageUrl = userAvatar,
                            seed = avatarSeed,
                            tariff = premiumStatus,
                            modifier = Modifier.size(70.dp) // Р—Р°РґР°РµРј СЂР°Р·РјРµСЂ С‚СѓС‚
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    ShimmerContainer(
                        isLoading = loadingState == LoadingState.Loading,
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMain
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    ShimmerContainer(
                        isLoading = loadingState == LoadingState.Loading,
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Text(
                            text = "@$userNickName",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onCopyNick() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    ShimmerContainer(
                        isLoading = loadingState == LoadingState.Loading,
                        modifier = Modifier.wrapContentSize()
                    ) {
                        TariffBadge(tariff = premiumStatus)
                    }
                }

                if (loadingState != LoadingState.Loading) {
                    Icon(
                        imageVector = IconsManager.KeyboardArrowRight,
                        contentDescription = "Go to details",
                        tint = if (premiumStatus == UserTariff.Free) {
                            colors.textSecondary.copy(alpha = 0.5f)
                        } else {
                            premiumStatus.primaryTint()
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TechnoGlowingAvatar(
    imageUrl: String,
    seed: String,
    tariff: UserTariff,
    modifier: Modifier = Modifier
) {
    val tariffColor = tariff.primaryTint()
    val isPremium = tariff != UserTariff.Free
    if (isPremium) {
        val infiniteTransition = rememberInfiniteTransition(label = "avatarPulse")
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .graphicsLayer { clip = false }
                .drawBehind {
                    val radius = size.minDimension / 2

                    drawCircle(
                        color = tariffColor.copy(alpha = 0.12f * pulseAlpha),
                        radius = radius + 10.dp.toPx(),
                        style = Stroke(width = 16.dp.toPx())
                    )
                    drawCircle(
                        color = tariffColor.copy(alpha = 0.24f * pulseAlpha),
                        radius = radius + 4.dp.toPx(),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    drawCircle(
                        color = tariffColor.copy(alpha = 0.75f * pulseAlpha),
                        radius = radius + 2.dp.toPx(),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
        ) {
            ShimmerAsyncImage(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = tariffColor,
                        shape = CircleShape
                    ),
                model = imageUrl,
                seed = seed,
                emojiSize = 26.dp,
                blurhash = null
            )
        }
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
        ) {
            ShimmerAsyncImage(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                model = imageUrl,
                seed = seed,
                emojiSize = 26.dp,
                blurhash = null
            )
        }
    }
}
