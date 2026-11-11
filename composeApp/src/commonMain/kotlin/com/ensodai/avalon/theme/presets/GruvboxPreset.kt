package com.ensodai.avalon.theme.presets

import androidx.compose.ui.graphics.Color
import com.ensodai.avalon.theme.AvalonColors
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleFill
import com.ensodai.avalon.theme.models.ChatBackgroundConfig

/**
 * 🍂 Gruvbox — Тёплый ретро
 *
 * Тёмная тема, вдохновлённая культовой палитрой Gruvbox.
 * Основа — тёплый тёмно-коричневый фон с янтарными, оранжевыми и оливковыми акцентами.
 * Ощущение: уютный, ностальгический, как тёплый свет лампы.
 *
 * Принципы:
 * - Background: тёплый тёмно-коричневый (#282828)
 * - Поверхности: плавная лестница тёплых серо-коричневых тонов
 * - Акценты: оранжевый + жёлтый (классические Gruvbox bright)
 * - Пузыри пользователя: тёплый оранжево-коричневый
 * - Текст: кремово-белый, не холодный
 */
private object GruvboxPalette {
    // --- Иерархия фонов (тёплая лестница) ---
    // 🌑 bg0: основной фон
    val background   = Color(0xFF282828)
    // 🎴 bg1: карточки, элементы списков
    val cardSurface  = Color(0xFF3C3836)
    // 🏔️ bg2: верхний слой (бары, пикеры)
    val surfaceHigh  = Color(0xFF504945)

    // --- Обводки ---
    // 🖌️ bg3: тонкие рамки
    val strokeSubtle = Color(0xFF504945)
    // 🖍️ bg4: активные
    val strokeActive = Color(0xFF7C6F64)

    // --- Текст ---
    // 📝 fg0: кремово-белый (тёплый, не холодный!)
    val textMain      = Color(0xFFFBF1C7)
    // 📄 fg2: приглушенный бежевый
    val textSecondary = Color(0xFFBDAE93)

    // --- Акценты (Gruvbox bright) ---
    // ⚡ bright_orange: главный акцент
    val neonAccent    = Color(0xFFFE8019)
    // 🔮 bright_aqua: вторичный
    val accentSecondary = Color(0xFF8EC07C)
    // ℹ️ bright_blue: инфо
    val info          = Color(0xFF83A598)
    // ✅ bright_green: успех
    val success       = Color(0xFFB8BB26)
    // ⚠️ bright_yellow: предупреждение
    val warning       = Color(0xFFFABD2F)
    // 🤫 bright_purple: muted
    val muted         = Color(0xFFD3869B)
    // 👑 bright_yellow: премиум
    val premium       = Color(0xFFFABD2F)

    // --- Пузыри ---
    // 💬 Пользователь: тёплый оранжево-коричневый
    val userBubble    = Color(0xFF79520A)
    // 💬 Бот: чуть светлее cardSurface
    val botBubble     = Color(0xFF3C3836)

    // --- Утилиты ---
    val progressTrack    = Color(0xFF504945)
    val placeholderText  = Color(0xFF928374)
    val dragHandle       = Color(0xFF7C6F64)
    val neutralButton    = Color(0xFF504945)
    val onlineStatus     = Color(0xFFB8BB26)
    val connectingStatus = Color(0xFFBDAE93)
    val upgradeStart     = Color(0xFFFE8019)

    // --- Редкости ---
    val rarityUncommon = Color(0xFF928374)
    val rarityRare     = Color(0xFF83A598)
    val rarityEpic     = Color(0xFFD3869B)
    val rarityMythic   = Color(0xFFFABD2F)

    // --- Оверлеи ---
    val overlaySurface = Color(0xFF3C3836)
    val overlayBorder  = Color(0xFF504945)
    val error          = Color(0xFFCC241D)
    // bright_red
    val errorBright    = Color(0xFFFB4934)
}

val GruvboxColors = AvalonColors(
    preset = AvalonThemePreset.Gruvbox,
    isLight = false,
    neonAccent = GruvboxPalette.neonAccent,
    infoAccent = GruvboxPalette.info,
    accentSecondary = GruvboxPalette.accentSecondary,
    mutedAccent = GruvboxPalette.muted,
    premiumAccent = GruvboxPalette.premium,
    successAccent = GruvboxPalette.success,
    warningAccent = GruvboxPalette.warning,
    progressTrack = GruvboxPalette.progressTrack,
    placeholderText = GruvboxPalette.placeholderText,
    dragHandle = GruvboxPalette.dragHandle,
    neutralButton = GruvboxPalette.neutralButton,
    onlineStatus = GruvboxPalette.onlineStatus,
    connectingStatus = GruvboxPalette.connectingStatus,
    upgradeGradientStart = GruvboxPalette.upgradeStart,
    upgradeGradientEnd = GruvboxPalette.muted,
    rarityUncommonGlow = GruvboxPalette.rarityUncommon,
    rarityRareGlow = GruvboxPalette.rarityRare,
    rarityEpicGlow = GruvboxPalette.rarityEpic,
    rarityMythicGlow = GruvboxPalette.rarityMythic,
    userBubbleTextColor = GruvboxPalette.textMain,
    botBubbleTextColor = GruvboxPalette.textMain,
    userBubble = BubbleFill.Solid(GruvboxPalette.userBubble),
    botBubble = BubbleFill.Solid(GruvboxPalette.botBubble),
    botBubbleBorder = GruvboxPalette.strokeSubtle,
    cardBackground = GruvboxPalette.cardSurface,
    overlaySurface = GruvboxPalette.overlaySurface,
    overlayBorder = GruvboxPalette.overlayBorder,
    strokeSubtle = GruvboxPalette.strokeSubtle,
    strokeActive = GruvboxPalette.strokeActive,
    surfaceHigh = GruvboxPalette.surfaceHigh,
    textMain = GruvboxPalette.textMain,
    textSecondary = GruvboxPalette.textSecondary,
    error = GruvboxPalette.errorBright,
    background = GruvboxPalette.background,
    chatBackground = ChatBackgroundConfig.Solid(GruvboxPalette.background)
)
