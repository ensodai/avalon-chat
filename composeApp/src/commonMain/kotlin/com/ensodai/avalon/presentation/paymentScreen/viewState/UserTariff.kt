package com.ensodai.avalon.presentation.paymentScreen.viewState

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager

enum class UserTariff(val title: String) {
    Free("Free"),
    Supporter("Supporter"),
    Premium("Premium"),
    Crystal("Crystal"),
    Elite("Elite"),
    Special("Special")
}

val UserTariff.icon: ImageVector
    get() = when (this) {
        UserTariff.Free -> IconsManager.Person
        UserTariff.Supporter -> IconsManager.Favorite
        UserTariff.Premium -> IconsManager.Bolt
        UserTariff.Crystal -> IconsManager.Diamond
        UserTariff.Elite -> IconsManager.Star
        UserTariff.Special -> IconsManager.AllInclusive
    }

@Composable
fun UserTariff.primaryTint(): Color {
    val colors = AvalonTheme.colors
    return when (this) {
        UserTariff.Free -> colors.textSecondary
        UserTariff.Supporter -> colors.onlineStatus
        UserTariff.Premium -> colors.neonAccent
        UserTariff.Crystal -> colors.rarityEpicGlow
        UserTariff.Elite -> colors.premiumAccent
        UserTariff.Special -> colors.rarityRareGlow
    }
}

@Composable
fun UserTariff.accentTint(): Color {
    val colors = AvalonTheme.colors
    return when (this) {
        UserTariff.Free -> colors.strokeActive
        UserTariff.Supporter -> colors.accentSecondary
        UserTariff.Premium -> colors.onlineStatus
        UserTariff.Crystal -> colors.accentSecondary
        UserTariff.Elite -> colors.upgradeGradientStart
        UserTariff.Special -> colors.accentSecondary
    }
}
