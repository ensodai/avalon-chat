package com.ensodai.avalon.theme.presets

import androidx.compose.ui.graphics.Color
import com.ensodai.avalon.theme.AvalonColors
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleFill
import com.ensodai.avalon.theme.models.ChatBackgroundConfig
import com.ensodai.avalon.theme.models.ChatPatternPreset
import androidx.compose.ui.unit.dp

private object PaperLightPalette {
    // --- Основные фоны ---
    // Фон для экранов профиля, настроек, списков (как ты нашел: #f1f1f3)
    val background   = Color(0xFFF1F1F3)

    // Универсальный белый для поверхностей (карточки в профиле,
    // фон самого экрана чата, фон пикера эмодзи, фон нижнего бара)
    val cardSurface  = Color(0xFFFFFFFF)

    // Фон текстового поля (как ты нашел: #ffffff)
    val surfaceHigh  = Color(0xFFFFFFFF)

    // --- Пузыри сообщений ---
    // Голубой исходящий (оставляем твой или классический синий)
    val userBubble   = Color(0xFF4DA8E0)
    // Входящие сообщения (как ты нашел: #efefef)
    val botBubble    = Color(0xFFF0F0F0)

    // --- Разделители ---
    // Так как в чате всё белое (фон, бар, поле), нам нужны очень тонкие
    // и нежные линии, чтобы отделять зоны друг от друга.
    val strokeSubtle = Color.Transparent
    val strokeActive = Color.Transparent

    // --- Текст ---
    val textMain      = Color(0xFF000000)
    val textSecondary = Color(0xFF8E8E93)

    // --- Акценты (оставляем сочные цвета для иконок/кнопок) ---
    val accentBlue   = Color(0xFF007AFF)
    val accentCyan   = Color(0xFF32ADE6)
    val accentPurple = Color(0xFFAF52DE)
    val info         = Color(0xFF007AFF)
    val success      = Color(0xFF34C759)
    val warning      = Color(0xFFFF9500)
    val muted        = Color(0xFFFF2D55)
    val premium      = Color(0xFFFFCC00)

    // --- Утилиты ---
    val progressTrack    = Color(0xFFE5E5EA)
    val dragHandle       = Color(0xFF3C3C43).copy(alpha = 0.3f)
    val neutralButton    = Color(0xFFF1F1F3) // Кнопки под цвет фона
    val onlineStatus     = Color(0xFF4FC3F7)
    val connectingStatus = Color(0xFF8E8E93)
    val upgradeStart     = Color(0xFFFF9500)

    // --- Редкости ---
    val rarityUncommon = Color(0xFF8E8E93)
    val rarityRare     = Color(0xFF007AFF)
    val rarityEpic     = Color(0xFFAF52DE)
    val rarityMythic   = Color(0xFFFFCC00)

    // --- Прочее ---
    // УБИРАЕМ обводки карточек полностью!
    val overlayBorder = Color.Transparent
    val error         = Color(0xFFFF3B30)
    val patternTint   = Color(0xFF748391).copy(alpha = 0.08f) // Если захочешь узор в чате
}
val PaperLightColors = AvalonColors(
    preset = AvalonThemePreset.PaperLight,
    isLight = true,
    neonAccent = PaperLightPalette.accentBlue,
    infoAccent = PaperLightPalette.info,
    accentSecondary = PaperLightPalette.accentPurple,
    mutedAccent = PaperLightPalette.muted,
    premiumAccent = PaperLightPalette.premium,
    successAccent = PaperLightPalette.success,
    warningAccent = PaperLightPalette.warning,
    progressTrack = PaperLightPalette.progressTrack,
    placeholderText = PaperLightPalette.rarityUncommon,
    dragHandle = PaperLightPalette.dragHandle,
    neutralButton = PaperLightPalette.neutralButton,
    onlineStatus = PaperLightPalette.onlineStatus,
    connectingStatus = PaperLightPalette.connectingStatus,
    upgradeGradientStart = PaperLightPalette.upgradeStart,
    upgradeGradientEnd = PaperLightPalette.accentPurple,
    rarityUncommonGlow = PaperLightPalette.rarityUncommon,
    rarityRareGlow = PaperLightPalette.rarityRare,
    rarityEpicGlow = PaperLightPalette.rarityEpic,
    rarityMythicGlow = PaperLightPalette.rarityMythic,
    userBubbleTextColor = Color.White,
    botBubbleTextColor = PaperLightPalette.textMain,
    userBubble = BubbleFill.Solid(PaperLightPalette.userBubble),
    botBubble = BubbleFill.Solid(PaperLightPalette.botBubble),
    cardBackground = PaperLightPalette.cardSurface,
    overlaySurface = PaperLightPalette.cardSurface,
    overlayBorder = PaperLightPalette.overlayBorder,
    strokeSubtle = PaperLightPalette.strokeSubtle,
    strokeActive = PaperLightPalette.strokeActive,
    surfaceHigh = PaperLightPalette.surfaceHigh,
    textMain = PaperLightPalette.textMain,
    textSecondary = PaperLightPalette.textSecondary,
    error = PaperLightPalette.error,
    background = PaperLightPalette.background,
    cardElevation = 1.dp,
    chatBackground = ChatBackgroundConfig.Solid(PaperLightPalette.cardSurface)
)
