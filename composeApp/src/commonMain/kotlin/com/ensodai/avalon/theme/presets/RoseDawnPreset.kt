package com.ensodai.avalon.theme.presets

import androidx.compose.ui.graphics.Color
import com.ensodai.avalon.theme.AvalonColors
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleFill
import com.ensodai.avalon.theme.models.ChatBackgroundConfig
import androidx.compose.ui.unit.dp

/**
 * 🌸 RoseDawn — Розовый рассвет
 *
 * Светлая тема, вдохновлённая нежным утренним рассветом.
 * Основа — тёплые пастельные тона: бледно-розовый фон, белые карточки.
 * Ощущение: мягкий, женственный, уютный.
 *
 * Принципы (по аналогии с PaperLight / Telegram Light):
 * - Background (#F8F0F4): светло-розовый фон для экранов профиля, настроек
 * - CardSurface (#FFFFFF): белые карточки на розовом фоне
 * - ChatBackground (#FFFFFF): белый фон чата (как в Telegram)
 * - Пузыри бота (#F5E8EE): нежно-розовые, хорошо видны на белом
 * - Обводки: ПРОЗРАЧНЫЕ (как в PaperLight — светлая тема = без обводок)
 * - Elevation: 1.dp (тень вместо обводки для карточек)
 */
private object RoseDawnPalette {
    // --- Иерархия фонов ---
    // 🌸 Светло-розовый фон: профиль, настройки, списки
    val background   = Color(0xFFF8F0F4)
    // 🎴 Белые карточки (на розовом фоне выделяются за счёт elevation)
    val cardSurface  = Color(0xFFFFFFFF)
    // 🏔️ Верхний слой: нижний бар, пикеры — слегка розоватый белый
    val surfaceHigh  = Color(0xFFFFFFFF)
    // 💬 Фон чата: чистый белый (как в Telegram)
    val chatSurface  = Color(0xFFFFFFFF)

    // --- Обводки ---
    // В светлых темах обводки не нужны — карточки отделяются тенью
    val strokeSubtle = Color.Transparent
    val strokeActive = Color.Transparent

    // --- Текст ---
    // 📝 Основной: тёмный, тёплый коричнево-розовый
    val textMain      = Color(0xFF2D1B25)
    // 📄 Вторичный: приглушенный розово-серый
    val textSecondary = Color(0xFF8A7580)

    // --- Акценты ---
    // ⚡ Главный: насыщенная роза (кнопки, ссылки)
    val accentPink    = Color(0xFFE05089)
    // 🔮 Вторичный: тёплый персиковый (градиенты, парные элементы)
    val accentPeach   = Color(0xFFF09B59)
    // ℹ️ Инфо: мягкий фиолетовый (отличается от розового)
    val info          = Color(0xFF7B6BFF)
    // ✅ Успех: природный зелёный
    val success       = Color(0xFF2D9650)
    // ⚠️ Предупреждение: тёплый оранжевый
    val warning       = Color(0xFFE68A00)
    // 🤫 Muted: тёмная роза (для деструктивных вторичных действий)
    val muted         = Color(0xFFC2255C)
    // 👑 Премиум: тёплое золото
    val premium       = Color(0xFFD49B04)

    // --- Пузыри ---
    // 💬 Пользователь: градиент от розы к персику
    val userBubbleStart = Color(0xFFE95A99)
    val userBubbleEnd   = Color(0xFFF2A05E)
    // 💬 Бот: нежно-розовый (хорошо выделяется на белом chatSurface)
    val botBubble       = Color(0xFFF5E8EE)

    // --- Утилиты ---
    val progressTrack    = Color(0xFFF0D6E0)
    val placeholderText  = Color(0xFFB09AA6)
    val dragHandle       = Color(0xFFC4AFBA).copy(alpha = 0.4f)
    val neutralButton    = Color(0xFFF3EBF0)
    val onlineStatus     = Color(0xFFE05089)
    val connectingStatus = Color(0xFF8A7580)
    val upgradeStart     = Color(0xFFF09B59)

    // --- Редкости ---
    val rarityUncommon = Color(0xFF8A7580)
    val rarityRare     = Color(0xFFE05089)
    val rarityEpic     = Color(0xFFF09B59)
    val rarityMythic   = Color(0xFFD49B04)

    // --- Оверлеи ---
    val overlaySurface = Color(0xFFFFFFFF)
    val overlayBorder  = Color.Transparent
    val error          = Color(0xFFD32F2F)
}

val RoseDawnColors = AvalonColors(
    preset = AvalonThemePreset.RoseDawn,
    isLight = true,
    neonAccent = RoseDawnPalette.accentPink,
    infoAccent = RoseDawnPalette.info,
    accentSecondary = RoseDawnPalette.accentPeach,
    mutedAccent = RoseDawnPalette.muted,
    premiumAccent = RoseDawnPalette.premium,
    successAccent = RoseDawnPalette.success,
    warningAccent = RoseDawnPalette.warning,
    progressTrack = RoseDawnPalette.progressTrack,
    placeholderText = RoseDawnPalette.placeholderText,
    dragHandle = RoseDawnPalette.dragHandle,
    neutralButton = RoseDawnPalette.neutralButton,
    onlineStatus = RoseDawnPalette.onlineStatus,
    connectingStatus = RoseDawnPalette.connectingStatus,
    upgradeGradientStart = RoseDawnPalette.upgradeStart,
    upgradeGradientEnd = RoseDawnPalette.accentPink,
    rarityUncommonGlow = RoseDawnPalette.rarityUncommon,
    rarityRareGlow = RoseDawnPalette.rarityRare,
    rarityEpicGlow = RoseDawnPalette.rarityEpic,
    rarityMythicGlow = RoseDawnPalette.rarityMythic,
    userBubbleTextColor = Color.White,
    botBubbleTextColor = RoseDawnPalette.textMain,
    userBubble = BubbleFill.Gradient(
        startColor = RoseDawnPalette.userBubbleStart,
        endColor = RoseDawnPalette.userBubbleEnd
    ),
    botBubble = BubbleFill.Solid(RoseDawnPalette.botBubble),
    cardBackground = RoseDawnPalette.cardSurface,
    overlaySurface = RoseDawnPalette.overlaySurface,
    overlayBorder = RoseDawnPalette.overlayBorder,
    strokeSubtle = RoseDawnPalette.strokeSubtle,
    strokeActive = RoseDawnPalette.strokeActive,
    surfaceHigh = RoseDawnPalette.surfaceHigh,
    textMain = RoseDawnPalette.textMain,
    textSecondary = RoseDawnPalette.textSecondary,
    error = RoseDawnPalette.error,
    background = RoseDawnPalette.background,
    cardElevation = 1.dp,
    chatBackground = ChatBackgroundConfig.Solid(RoseDawnPalette.chatSurface)
)
