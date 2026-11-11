package com.ensodai.avalon.theme.presets

import androidx.compose.ui.graphics.Color
import com.ensodai.avalon.theme.AvalonColors
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleFill
import com.ensodai.avalon.theme.models.ChatBackgroundConfig
import androidx.compose.ui.unit.dp

/**
 * ✏️ SketchPad — Нарисованный карандашом
 *
 * Светлая тема в стиле Joshua Vides "Reality to Idea".
 * Создаёт впечатление, что весь интерфейс нарисован маркером на бумаге.
 *
 * Ключевой трюк: ТЁМНЫЕ ВИДИМЫЕ ОБВОДКИ на всех элементах.
 * Это единственная светлая тема, где strokeSubtle != Transparent.
 * В комбинации с бумажным фоном это создаёт "sketch" эффект.
 *
 * Принципы:
 * - Background: тёплый бумажный (#F7F3ED) — как настоящая бумага для скетчей
 * - Карточки: чистый белый (контраст с бумагой)
 * - Обводки: ТЁМНЫЕ, заметные (#3C3C3C) — ЭТО ГЛАВНАЯ ФИШКА
 * - Пузыри: плоские, без градиентов (как раскрашенный скетч)
 * - Elevation: 0dp — тени убиваем, вместо них обводки
 * - Акценты: яркие, "маркерные" — как раскрашено фломастерами
 */
private object SketchPadPalette {
    // --- Фоны ---
    // 📜 Бумага: тёплый кремовый (не холодный белый!)
    val background   = Color(0xFFF7F3ED)
    // 🎴 Карточки: чистый белый — выделяется на "бумаге"
    val cardSurface  = Color(0xFFFFFFFF)
    // 🏔️ Верхний слой
    val surfaceHigh  = Color(0xFFFFFFFF)
    // 💬 Фон чата: тот же бумажный
    val chatSurface  = Color(0xFFF7F3ED)

    // --- ОБВОДКИ (ГЛАВНАЯ ФИШКА!) ---
    // ✏️ Тонкие: тёмно-серый "карандаш" — ВИДИМЫЙ, не прозрачный!
    val strokeSubtle = Color(0xFF3C3C3C)
    // 🖊️ Активные: почти чёрный "маркер"
    val strokeActive = Color(0xFF1A1A1A)

    // --- Текст ---
    // 📝 Основной: чёрный, как чернила
    val textMain      = Color(0xFF1A1A1A)
    // 📄 Вторичный: серый карандаш
    val textSecondary = Color(0xFF6B6B6B)

    // --- Акценты ("фломастеры" — яркие, насыщенные) ---
    // ⚡ Синий маркер
    val accentBlue   = Color(0xFF2979FF)
    // 🔮 Красный маркер
    val accentRed    = Color(0xFFE53935)
    // ℹ️ Синий
    val info         = Color(0xFF2979FF)
    // ✅ Зелёный маркер
    val success      = Color(0xFF43A047)
    // ⚠️ Оранжевый маркер
    val warning      = Color(0xFFFF8F00)
    // 🤫 Розовый маркер
    val muted        = Color(0xFFD81B60)
    // 👑 Жёлтый хайлайтер
    val premium      = Color(0xFFFFC107)

    // --- Пузыри (ПЛОСКИЕ, без градиентов — как раскрашенный скетч) ---
    // 💬 Пользователь: сплошной синий "маркер"
    val userBubble   = Color(0xFF2979FF)
    // 💬 Бот: очень светлый серый (чуть темнее фона)
    val botBubble    = Color(0xFFEBE8E3)

    // --- Утилиты ---
    val progressTrack    = Color(0xFFDAD6D0)
    val placeholderText  = Color(0xFF9E9E9E)
    val dragHandle       = Color(0xFF3C3C3C).copy(alpha = 0.4f)
    val neutralButton    = Color(0xFFEBE8E3)
    val onlineStatus     = Color(0xFF43A047)
    val connectingStatus = Color(0xFF6B6B6B)
    val upgradeStart     = Color(0xFFFF8F00)

    // --- Редкости ---
    val rarityUncommon = Color(0xFF6B6B6B)
    val rarityRare     = Color(0xFF2979FF)
    val rarityEpic     = Color(0xFFE53935)
    val rarityMythic   = Color(0xFFFFC107)

    // --- Оверлеи ---
    val overlaySurface = Color(0xFFFFFFFF)
    val overlayBorder  = Color(0xFF3C3C3C)
    val error          = Color(0xFFE53935)
}

val SketchPadColors = AvalonColors(
    preset = AvalonThemePreset.SketchPad,
    isLight = true,
    neonAccent = SketchPadPalette.accentBlue,
    infoAccent = SketchPadPalette.info,
    accentSecondary = SketchPadPalette.accentRed,
    mutedAccent = SketchPadPalette.muted,
    premiumAccent = SketchPadPalette.premium,
    successAccent = SketchPadPalette.success,
    warningAccent = SketchPadPalette.warning,
    progressTrack = SketchPadPalette.progressTrack,
    placeholderText = SketchPadPalette.placeholderText,
    dragHandle = SketchPadPalette.dragHandle,
    neutralButton = SketchPadPalette.neutralButton,
    onlineStatus = SketchPadPalette.onlineStatus,
    connectingStatus = SketchPadPalette.connectingStatus,
    upgradeGradientStart = SketchPadPalette.upgradeStart,
    upgradeGradientEnd = SketchPadPalette.accentRed,
    rarityUncommonGlow = SketchPadPalette.rarityUncommon,
    rarityRareGlow = SketchPadPalette.rarityRare,
    rarityEpicGlow = SketchPadPalette.rarityEpic,
    rarityMythicGlow = SketchPadPalette.rarityMythic,
    userBubbleTextColor = Color.White,
    botBubbleTextColor = SketchPadPalette.textMain,
    // Плоские, без градиентов! Как раскрашенный скетч.
    userBubble = BubbleFill.Solid(SketchPadPalette.userBubble),
    botBubble = BubbleFill.Solid(SketchPadPalette.botBubble),
    // ✏️ ОБВОДКИ НА ПУЗЫРЯХ — ключевой "sketch" эффект!
    userBubbleBorder = SketchPadPalette.strokeSubtle,
    botBubbleBorder = SketchPadPalette.strokeSubtle,
    cardBackground = SketchPadPalette.cardSurface,
    overlaySurface = SketchPadPalette.overlaySurface,
    overlayBorder = SketchPadPalette.overlayBorder,
    // ✏️ ВИДИМЫЕ обводки — не прозрачные!
    strokeSubtle = SketchPadPalette.strokeSubtle,
    strokeActive = SketchPadPalette.strokeActive,
    surfaceHigh = SketchPadPalette.surfaceHigh,
    textMain = SketchPadPalette.textMain,
    textSecondary = SketchPadPalette.textSecondary,
    error = SketchPadPalette.error,
    background = SketchPadPalette.background,
    // Без тени! Обводки заменяют elevation.
    cardElevation = 0.dp,
    chatBackground = ChatBackgroundConfig.Solid(SketchPadPalette.chatSurface)
)
