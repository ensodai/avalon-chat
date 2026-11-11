package com.ensodai.avalon.theme.presets

import androidx.compose.ui.graphics.Color
import com.ensodai.avalon.theme.AvalonColors
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleFill
import com.ensodai.avalon.theme.models.ChatBackgroundConfig
import com.ensodai.avalon.theme.models.ChatPatternPreset

private object TechNoirPalette {
    val background = Color(0xFF121212)
    val cardSurface = Color(0xFF1C1C1E)
    val surfaceHigh = Color(0xFF252525)
    val strokeSubtle = Color(0xFF2C2C2E)
    val strokeActive = Color(0xFF444444)
    val textMain = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFF8E8E93)
    val accentCyan = Color(0xFF00E5FF)
    val accentPurple = Color(0xFF9C27B0)
    val accentBlue = Color(0xFF0088CC)
    val success = Color(0xFF00E676)
    val warning = Color(0xFFFFAB40)
    val info = Color(0xFF2979FF)
    val muted = Color(0xFFE91E63)
    val premium = Color(0xFFFFC107)
    val progressTrack = Color(0xFF2C2C2C)
    val dragHandle = Color(0xFF7A7A7A)
    val neutralButton = Color(0xFF333333)
    val onlineStatus = Color(0xFF4FC3F7)
    val connectingStatus = Color(0xFFE0E0E0)
    val upgradeStart = Color(0xFFFF6D00)
    val rarityUncommon = Color(0xFFB0BEC5)
    val rarityRare = Color(0xFF4FC3F7)
    val rarityEpic = Color(0xFFE040FB)
    val rarityMythic = Color(0xFFFFD54F)
    val userBubble = Color(0xFF005885)
    val botBubble = Color(0xFF1E1E1E)
    val overlayBorder = Color(0xFF333333)
    val error = Color(0xFFFF4444)
}

val TechNoirColors = AvalonColors(
    preset = AvalonThemePreset.TechNoir,
    isLight = false,
    neonAccent = TechNoirPalette.accentCyan,
    infoAccent = TechNoirPalette.info,
    accentSecondary = TechNoirPalette.accentPurple,
    mutedAccent = TechNoirPalette.muted,
    premiumAccent = TechNoirPalette.premium,
    successAccent = TechNoirPalette.success,
    warningAccent = TechNoirPalette.warning,
    progressTrack = TechNoirPalette.progressTrack,
    placeholderText = TechNoirPalette.textSecondary,
    dragHandle = TechNoirPalette.dragHandle,
    neutralButton = TechNoirPalette.neutralButton,
    onlineStatus = TechNoirPalette.onlineStatus,
    connectingStatus = TechNoirPalette.connectingStatus,
    upgradeGradientStart = TechNoirPalette.upgradeStart,
    upgradeGradientEnd = TechNoirPalette.accentPurple,
    rarityUncommonGlow = TechNoirPalette.rarityUncommon,
    rarityRareGlow = TechNoirPalette.rarityRare,
    rarityEpicGlow = TechNoirPalette.rarityEpic,
    rarityMythicGlow = TechNoirPalette.rarityMythic,
    userBubbleTextColor = Color.White,
    botBubbleTextColor = TechNoirPalette.textMain,
    userBubble = BubbleFill.Solid(TechNoirPalette.userBubble),
    botBubble = BubbleFill.Solid(TechNoirPalette.botBubble),
    cardBackground = TechNoirPalette.cardSurface,
    overlaySurface = TechNoirPalette.botBubble,
    overlayBorder = TechNoirPalette.overlayBorder,
    strokeSubtle = TechNoirPalette.strokeSubtle,
    strokeActive = TechNoirPalette.strokeActive,
    surfaceHigh = TechNoirPalette.surfaceHigh,
    textMain = TechNoirPalette.textMain,
    textSecondary = TechNoirPalette.textSecondary,
    error = TechNoirPalette.error,
    background = TechNoirPalette.background,
    chatBackground = ChatBackgroundConfig.Default
)
