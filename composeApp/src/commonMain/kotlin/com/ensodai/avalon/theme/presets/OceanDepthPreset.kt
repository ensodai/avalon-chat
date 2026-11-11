package com.ensodai.avalon.theme.presets

import androidx.compose.ui.graphics.Color
import com.ensodai.avalon.theme.AvalonColors
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleFill
import com.ensodai.avalon.theme.models.ChatBackgroundConfig

/**
 * 🌊 OceanDepth — Глубокий океан
 *
 * Тёмная тема, вдохновлённая глубинами океана.
 * Основа — тёмно-синие тона с голубым/бирюзовым акцентом.
 * Ощущение: спокойная глубина, мягкий свет сквозь толщу воды.
 *
 * Принципы:
 * - Background → CardSurface → SurfaceHigh: плавная лестница яркости (+2..3% lightness на шаг)
 * - Обводки тонкие, но заметные (не прозрачные, как в светлых темах)
 * - Пузыри пользователя: градиент от бирюзового к тёмно-морскому
 * - Пузыри бота: чуть светлее background, чтобы выделяться
 * - Акценты: голубой + бирюзовый для морской атмосферы
 */
private object OceanDepthPalette {
    // --- Иерархия фонов (от глубокого к поверхностному) ---
    // 🌑 Самый глубокий фон: экраны, навигация
    val background   = Color(0xFF0A1929)
    // 🎴 Карточки, элементы списков — чуть светлее фона
    val cardSurface  = Color(0xFF112240)
    // 🏔️ Верхний слой: нижний бар, пикеры, дропдауны
    val surfaceHigh  = Color(0xFF173050)

    // --- Обводки ---
    // 🖌️ Тонкие рамки карточек, разделители
    val strokeSubtle = Color(0xFF1E3A5F)
    // 🖍️ Активные: фокус на поле ввода
    val strokeActive = Color(0xFF3D6A99)

    // --- Текст ---
    // 📝 Основной: чистый белый с лёгким голубым оттенком
    val textMain      = Color(0xFFE8F1FA)
    // 📄 Вторичный: приглушенный серо-голубой
    val textSecondary = Color(0xFF8EABC4)

    // --- Акценты ---
    // ⚡ Главный неон: яркий голубой (ссылки, главные кнопки)
    val neonAccent    = Color(0xFF42A5F5)
    // 🔮 Вторичный: приглушенный тёмно-голубой (парный к неону)
    val accentSecondary = Color(0xFF2C7DA0)
    // ℹ️ Инфо: классический синий
    val info          = Color(0xFF448AFF)
    // ✅ Успех: морской изумруд
    val success       = Color(0xFF4DD0A1)
    // ⚠️ Предупреждение: тёплый янтарь
    val warning       = Color(0xFFFFB74D)
    // 🤫 Muted: кораллово-розовый (контраст к синей палитре)
    val muted         = Color(0xFFFF6B8A)
    // 👑 Премиум: золотистый
    val premium       = Color(0xFFFFCA28)

    // --- Пузыри ---
    // 💬 Пользователь: градиент от морского к бирюзовому
    val userBubbleStart = Color(0xFF0D47A1)
    val userBubbleEnd   = Color(0xFF00838F)
    // 💬 Бот: выделяется на фоне — чуть светлее cardSurface
    val botBubble       = Color(0xFF142D4F)

    // --- Утилиты ---
    val progressTrack    = Color(0xFF1E3A5F)
    val placeholderText  = Color(0xFF607D9B)
    val dragHandle       = Color(0xFF4A6F8F)
    val neutralButton    = Color(0xFF1E3A5F)
    val onlineStatus     = Color(0xFF4FC3F7)
    val connectingStatus = Color(0xFF8EABC4)
    val upgradeStart     = Color(0xFFFFB74D)

    // --- Редкости ---
    val rarityUncommon = Color(0xFF90A4AE)
    val rarityRare     = Color(0xFF42A5F5)
    val rarityEpic     = Color(0xFF4DD0E1)
    val rarityMythic   = Color(0xFFFFCA28)

    // --- Оверлеи ---
    val overlaySurface = Color(0xFF142D4F)
    val overlayBorder  = Color(0xFF1E3A5F)
    val error          = Color(0xFFEF5350)
}

val OceanDepthColors = AvalonColors(
    preset = AvalonThemePreset.OceanDepth,
    isLight = false,
    neonAccent = OceanDepthPalette.neonAccent,
    infoAccent = OceanDepthPalette.info,
    accentSecondary = OceanDepthPalette.accentSecondary,
    mutedAccent = OceanDepthPalette.muted,
    premiumAccent = OceanDepthPalette.premium,
    successAccent = OceanDepthPalette.success,
    warningAccent = OceanDepthPalette.warning,
    progressTrack = OceanDepthPalette.progressTrack,
    placeholderText = OceanDepthPalette.placeholderText,
    dragHandle = OceanDepthPalette.dragHandle,
    neutralButton = OceanDepthPalette.neutralButton,
    onlineStatus = OceanDepthPalette.onlineStatus,
    connectingStatus = OceanDepthPalette.connectingStatus,
    upgradeGradientStart = OceanDepthPalette.upgradeStart,
    upgradeGradientEnd = OceanDepthPalette.accentSecondary,
    rarityUncommonGlow = OceanDepthPalette.rarityUncommon,
    rarityRareGlow = OceanDepthPalette.rarityRare,
    rarityEpicGlow = OceanDepthPalette.rarityEpic,
    rarityMythicGlow = OceanDepthPalette.rarityMythic,
    userBubbleTextColor = Color.White,
    botBubbleTextColor = OceanDepthPalette.textMain,
    userBubble = BubbleFill.Gradient(
        startColor = OceanDepthPalette.userBubbleStart,
        endColor = OceanDepthPalette.userBubbleEnd
    ),
    botBubble = BubbleFill.Solid(OceanDepthPalette.botBubble),
    cardBackground = OceanDepthPalette.cardSurface,
    overlaySurface = OceanDepthPalette.overlaySurface,
    overlayBorder = OceanDepthPalette.overlayBorder,
    strokeSubtle = OceanDepthPalette.strokeSubtle,
    strokeActive = OceanDepthPalette.strokeActive,
    surfaceHigh = OceanDepthPalette.surfaceHigh,
    textMain = OceanDepthPalette.textMain,
    textSecondary = OceanDepthPalette.textSecondary,
    error = OceanDepthPalette.error,
    background = OceanDepthPalette.background,
    chatBackground = ChatBackgroundConfig.Solid(OceanDepthPalette.background)
)
