package com.ensodai.avalon.theme.presets

import androidx.compose.ui.graphics.Color
import com.ensodai.avalon.theme.AvalonColors
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleFill
import com.ensodai.avalon.theme.models.ChatBackgroundConfig

/**
 * 🩸 CrimsonNight — Багровая ночь
 *
 * Тёмная тема с благородным тёмно-красным/бордовым акцентом.
 * Ощущение: премиальная, дерзкая, как бордовая кожа и красное вино.
 *
 * Уникальная особенность: тонкие тёмно-красные обводки (strokeSubtle),
 * которые придают элементам характерный красноватый контур — ни одна
 * другая тема так не делает.
 *
 * Принципы:
 * - Background: почти чёрный с тёплым подтоном (#161013)
 * - Поверхности: тёмно-серые с едва заметным красным оттенком
 * - Обводки: тёмно-бордовые — видимые, но не кричащие
 * - Акценты: вишнёвый + алый
 * - Пузыри пользователя: градиент от тёмного бордо к вишнёвому
 * - Текст: тёплый off-white
 */
private object CrimsonNightPalette {
    // --- Иерархия фонов (тёплая тёмная лестница с красным подтоном) ---
    // 🌑 Самый глубокий: почти чёрный, чуть тёплый
    val background   = Color(0xFF161013)
    // 🎴 Карточки: тёмный угольный с красным подтоном
    val cardSurface  = Color(0xFF221920)
    // 🏔️ Верхний слой: чуть светлее, заметный тёплый оттенок
    val surfaceHigh  = Color(0xFF2D2128)

    // --- Обводки (УНИКАЛЬНАЯ ФИШКА: тёмно-бордовые рамки) ---
    // 🖌️ Тонкие: тёмный бордо — видимый, но деликатный
    val strokeSubtle = Color(0xFF3D2030)
    // 🖍️ Активные: ярче, заметный красный
    val strokeActive = Color(0xFF6B3040)

    // --- Текст ---
    // 📝 Основной: тёплый кремовый (не холодный белый)
    val textMain      = Color(0xFFF5EBE8)
    // 📄 Вторичный: приглушённый розовато-серый
    val textSecondary = Color(0xFFA89098)

    // --- Акценты ---
    // ⚡ Главный: благородный вишнёвый (не кислотный!)
    val neonAccent    = Color(0xFFE53E5F)
    // 🔮 Вторичный: тёплый коралл (парный)
    val accentSecondary = Color(0xFFFF7043)
    // ℹ️ Инфо: приглушённый синевато-красный
    val info          = Color(0xFFEF5350)
    // ✅ Успех: мятный (максимальный контраст к красному)
    val success       = Color(0xFF66BB6A)
    // ⚠️ Предупреждение: тёплый янтарь
    val warning       = Color(0xFFFFCA28)
    // 🤫 Muted: тёмная роза
    val muted         = Color(0xFFC62828)
    // 👑 Премиум: розовое золото
    val premium       = Color(0xFFFFB74D)

    // --- Пузыри ---
    // 💬 Пользователь: градиент от тёмного бордо к вишнёвому
    val userBubbleStart = Color(0xFF7B1D34)
    val userBubbleEnd   = Color(0xFFC62846)
    // 💬 Бот: тёмный, чуть теплее cardSurface
    val botBubble       = Color(0xFF241A20)

    // --- Утилиты ---
    val progressTrack    = Color(0xFF3D2030)
    val placeholderText  = Color(0xFF7A6670)
    val dragHandle       = Color(0xFF5A3D4A)
    val neutralButton    = Color(0xFF3D2030)
    val onlineStatus     = Color(0xFFE53E5F)
    val connectingStatus = Color(0xFFA89098)
    val upgradeStart     = Color(0xFFFFB74D)

    // --- Редкости ---
    val rarityUncommon = Color(0xFF9E8A90)
    val rarityRare     = Color(0xFFE53E5F)
    val rarityEpic     = Color(0xFFFF7043)
    val rarityMythic   = Color(0xFFFFB74D)

    // --- Оверлеи ---
    val overlaySurface = Color(0xFF241A20)
    val overlayBorder  = Color(0xFF3D2030)
    val error          = Color(0xFFFF5252)
}

val CrimsonNightColors = AvalonColors(
    preset = AvalonThemePreset.CrimsonNight,
    isLight = false,
    neonAccent = CrimsonNightPalette.neonAccent,
    infoAccent = CrimsonNightPalette.info,
    accentSecondary = CrimsonNightPalette.accentSecondary,
    mutedAccent = CrimsonNightPalette.muted,
    premiumAccent = CrimsonNightPalette.premium,
    successAccent = CrimsonNightPalette.success,
    warningAccent = CrimsonNightPalette.warning,
    progressTrack = CrimsonNightPalette.progressTrack,
    placeholderText = CrimsonNightPalette.placeholderText,
    dragHandle = CrimsonNightPalette.dragHandle,
    neutralButton = CrimsonNightPalette.neutralButton,
    onlineStatus = CrimsonNightPalette.onlineStatus,
    connectingStatus = CrimsonNightPalette.connectingStatus,
    upgradeGradientStart = CrimsonNightPalette.upgradeStart,
    upgradeGradientEnd = CrimsonNightPalette.neonAccent,
    rarityUncommonGlow = CrimsonNightPalette.rarityUncommon,
    rarityRareGlow = CrimsonNightPalette.rarityRare,
    rarityEpicGlow = CrimsonNightPalette.rarityEpic,
    rarityMythicGlow = CrimsonNightPalette.rarityMythic,
    userBubbleTextColor = Color.White,
    botBubbleTextColor = CrimsonNightPalette.textMain,
    userBubble = BubbleFill.Gradient(
        startColor = CrimsonNightPalette.userBubbleStart,
        endColor = CrimsonNightPalette.userBubbleEnd
    ),
    botBubble = BubbleFill.Solid(CrimsonNightPalette.botBubble),
    botBubbleBorder = CrimsonNightPalette.strokeSubtle,
    cardBackground = CrimsonNightPalette.cardSurface,
    overlaySurface = CrimsonNightPalette.overlaySurface,
    overlayBorder = CrimsonNightPalette.overlayBorder,
    strokeSubtle = CrimsonNightPalette.strokeSubtle,
    strokeActive = CrimsonNightPalette.strokeActive,
    surfaceHigh = CrimsonNightPalette.surfaceHigh,
    textMain = CrimsonNightPalette.textMain,
    textSecondary = CrimsonNightPalette.textSecondary,
    error = CrimsonNightPalette.error,
    background = CrimsonNightPalette.background,
    chatBackground = ChatBackgroundConfig.Solid(CrimsonNightPalette.background)
)
