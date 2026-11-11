package com.ensodai.avalon.presentation.userAccountScreen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.user_profile_battery
import com.ensodai.avalon.generated.resources.user_profile_battery_in_reserve
import com.ensodai.avalon.generated.resources.user_profile_quota
import com.ensodai.avalon.generated.resources.user_profile_quota_updated
import com.ensodai.avalon.presentation.benchmark.BenchmarkTags
import com.ensodai.avalon.presentation.commonComponents.GemCard
import com.ensodai.avalon.presentation.commonComponents.shimmer.ShimmerContainer
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun ResourcesSection(
    modifier: Modifier = Modifier,
    loadingState: LoadingState,
    dailyQuotaCurrent: Int,
    dailyQuotaMax: Int,
    purchasedBatteries: Int,
    onQuotaClick: () -> Unit,
    onBatteriesClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ResourceCard(
            modifier = Modifier
                .weight(1f)
                .testTag(BenchmarkTags.UserAccountQuotaCard),
            title = stringResource(Res.string.user_profile_quota),
            value = "$dailyQuotaCurrent/$dailyQuotaMax",
            subtext = stringResource(Res.string.user_profile_quota_updated),
            icon = IconsManager.Bolt,
            loadingState = loadingState,
            onClick = onQuotaClick
        )

        ResourceCard(
            modifier = Modifier
                .weight(1f)
                .testTag(BenchmarkTags.UserAccountBatteriesCard),
            title = stringResource(Res.string.user_profile_battery),
            value = purchasedBatteries.toString(),
            subtext = stringResource(Res.string.user_profile_battery_in_reserve),
            icon = IconsManager.BatteryChargingFull,
            loadingState = loadingState,
            onClick = onBatteriesClick
        )
    }
}

@Composable
fun ResourceCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    loadingState: LoadingState,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors

    GemCard(
        modifier = modifier,
        onClick = { onClick() },
        enabled = true,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.textMain.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.textMain.copy(alpha = 0.9f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            ShimmerContainer(
                isLoading = loadingState == LoadingState.Loading,
                modifier = Modifier.wrapContentSize()
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMain,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            ShimmerContainer(
                isLoading = loadingState == LoadingState.Loading,
                modifier = Modifier.wrapContentSize()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMain.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }

            ShimmerContainer(
                isLoading = loadingState == LoadingState.Loading,
                modifier = Modifier.wrapContentSize()
            ) {
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
