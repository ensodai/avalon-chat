package com.ensodai.avalon.theme.presets

import androidx.compose.ui.graphics.Color
import com.ensodai.avalon.theme.AvalonColors
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleFill
import com.ensodai.avalon.theme.models.ChatBackgroundConfig

/**
 * 🔮 MidnightPurple — Полуночный пурпур
 *
 * Тёмная тема с глубокими фиолетовыми тонами и сиреневым неоном.
 * Ощущение: премиальный, загадочный, гламурный.
 * Гендерно-нейтральная: фиолетовый одинаково нравится всем.
 *
 * Принципы:
 * - Background: глубокий тёмно-фиолетовый (#1A1025)
 * - Поверхности: плавная лестница от индиго к сливовому
 * - Акценты: сиреневый неон + фиолетовый
 * - Пузыри пользователя: градиент от индиго к фиолетовому
 * - Текст: светло-лавандовый
 */
private object MidnightPurplePalette {
    // --- Иерархия фонов ---
    // 🌑 Самый глубокий: тёмно-фиолетовый с синим подтоном
    val background   = Color(0xFF1A1025)
    // 🎴 Карточки: насыщенный индиго
    val cardSurface  = Color(0xFF251838)
    // 🏔️ Верхний слой: сливовый
    val surfaceHigh  = Color(0xFF302148)

    // --- Обводки ---
    // 🖌️ Тонкие рамки
    val strokeSubtle = Color(0xFF3A2855)
    // 🖍️ Активные
    val strokeActive = Color(0xFF6B4D8A)

    // --- Текст ---
    // 📝 Основной: светлая лаванда
    val textMain      = Color(0xFFEDE4F7)
    // 📄 Вторичный: приглушённый сиреневый
    val textSecondary = Color(0xFFA08FBF)

    // --- Акценты ---
    // ⚡ Главный: яркий сиреневый неон
    val neonAccent    = Color(0xFFB388FF)
    // 🔮 Вторичный: тёплый розово-фиолетовый
    val accentSecondary = Color(0xFFE040FB)
    // ℹ️ Инфо: голубовато-фиолетовый
    val info          = Color(0xFF7C8CFF)
    // ✅ Успех: мятный (контраст к фиолетовому)
    val success       = Color(0xFF69F0AE)
    // ⚠️ Предупреждение: тёплый янтарь
    val warning       = Color(0xFFFFD740)
    // 🤫 Muted: приглушённый розовый
    val muted         = Color(0xFFFF4081)
    // 👑 Премиум: золотистый
    val premium       = Color(0xFFFFD54F)

    // --- Пузыри ---
    // 💬 Пользователь: градиент от индиго к пурпуру
    val userBubbleStart = Color(0xFF4527A0)
    val userBubbleEnd   = Color(0xFF7B1FA2)
    // 💬 Бот: чуть светлее card, с фиолетовым оттенком
    val botBubble       = Color(0xFF2A1D40)

    // --- Утилиты ---
    val progressTrack    = Color(0xFF3A2855)
    val placeholderText  = Color(0xFF7A6B96)
    val dragHandle       = Color(0xFF5C4580)
    val neutralButton    = Color(0xFF3A2855)
    val onlineStatus     = Color(0xFFB388FF)
    val connectingStatus = Color(0xFFA08FBF)
    val upgradeStart     = Color(0xFFFFD740)

    // --- Редкости ---
    val rarityUncommon = Color(0xFF9E8FBF)
    val rarityRare     = Color(0xFFB388FF)
    val rarityEpic     = Color(0xFFE040FB)
    val rarityMythic   = Color(0xFFFFD54F)

    // --- Оверлеи ---
    val overlaySurface = Color(0xFF2A1D40)
    val overlayBorder  = Color(0xFF3A2855)
    val error          = Color(0xFFFF5252)
}

val MidnightPurpleColors = AvalonColors(
    preset = AvalonThemePreset.MidnightPurple,
    isLight = false,
    neonAccent = MidnightPurplePalette.neonAccent,
    infoAccent = MidnightPurplePalette.info,
    accentSecondary = MidnightPurplePalette.accentSecondary,
    mutedAccent = MidnightPurplePalette.muted,
    premiumAccent = MidnightPurplePalette.premium,
    successAccent = MidnightPurplePalette.success,
    warningAccent = MidnightPurplePalette.warning,
    progressTrack = MidnightPurplePalette.progressTrack,
    placeholderText = MidnightPurplePalette.placeholderText,
    dragHandle = MidnightPurplePalette.dragHandle,
    neutralButton = MidnightPurplePalette.neutralButton,
    onlineStatus = MidnightPurplePalette.onlineStatus,
    connectingStatus = MidnightPurplePalette.connectingStatus,
    upgradeGradientStart = MidnightPurplePalette.upgradeStart,
    upgradeGradientEnd = MidnightPurplePalette.accentSecondary,
    rarityUncommonGlow = MidnightPurplePalette.rarityUncommon,
    rarityRareGlow = MidnightPurplePalette.rarityRare,
    rarityEpicGlow = MidnightPurplePalette.rarityEpic,
    rarityMythicGlow = MidnightPurplePalette.rarityMythic,
    userBubbleTextColor = Color.White,
    botBubbleTextColor = MidnightPurplePalette.textMain,
    userBubble = BubbleFill.Gradient(
        startColor = MidnightPurplePalette.userBubbleStart,
        endColor = MidnightPurplePalette.userBubbleEnd
    ),
    botBubble = BubbleFill.Solid(MidnightPurplePalette.botBubble),
    cardBackground = MidnightPurplePalette.cardSurface,
    overlaySurface = MidnightPurplePalette.overlaySurface,
    overlayBorder = MidnightPurplePalette.overlayBorder,
    strokeSubtle = MidnightPurplePalette.strokeSubtle,
    strokeActive = MidnightPurplePalette.strokeActive,
    surfaceHigh = MidnightPurplePalette.surfaceHigh,
    textMain = MidnightPurplePalette.textMain,
    textSecondary = MidnightPurplePalette.textSecondary,
    error = MidnightPurplePalette.error,
    background = MidnightPurplePalette.background,
    chatBackground = ChatBackgroundConfig.Solid(MidnightPurplePalette.background)
)
