package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager


data class AvatarStyle(
    val icon: ImageVector,
    val backgroundBrush: Color,
    val iconColor: Color = Color.White // Иконка всегда светлая поверх аватара
)

object AvatarGenerator {
    private val icons: List<ImageVector> = listOf(
        IconsManager.SmartToy,
        IconsManager.Fingerprint,
        IconsManager.Extension,
        IconsManager.Dns,
        IconsManager.Hub,
        IconsManager.Memory,
        IconsManager.Code,

        IconsManager.Face,
        IconsManager.Psychology,
        IconsManager.Visibility,
        IconsManager.SelfImprovement,
        IconsManager.Pets,
        IconsManager.AcUnit,
        IconsManager.Bolt,
        IconsManager.Waves,
        IconsManager.Public,
        IconsManager.Spa,
        IconsManager.Diamond,
        IconsManager.Explore,
        IconsManager.Shield,
        IconsManager.HourglassEmpty,
        IconsManager.SentimentSatisfied,
        IconsManager.SentimentDissatisfied
    )
    fun getStyle(seed: String, palette: List<Color>): AvatarStyle {
        if (seed.isBlank()) {
            return AvatarStyle(
                icon = IconsManager.Person,
                backgroundBrush = palette.first()
            )
        }

        val hash = seed.hashCode() and Int.MAX_VALUE

        val icon = icons[hash % icons.size]
        val brush = palette[hash % palette.size]

        return AvatarStyle(
            icon = icon,
            backgroundBrush = brush
        )
    }

}

@Composable
fun rememberAvatarStyle(seed: String): AvatarStyle {
    val colors = AvalonTheme.colors
    val palette = remember(colors) {
        listOf(
            colors.cardBackground,
            colors.overlaySurface,
            colors.surfaceHigh,
            colors.background.copy(alpha = 0.92f),
            colors.strokeSubtle,
            colors.neonAccent.copy(alpha = 0.22f).compositeOver(colors.background),
            colors.accentSecondary.copy(alpha = 0.20f).compositeOver(colors.background),
            colors.warningAccent.copy(alpha = 0.18f).compositeOver(colors.background)
        )
    }
    return remember(seed, palette) {
        AvatarGenerator.getStyle(seed, palette)
    }
}
