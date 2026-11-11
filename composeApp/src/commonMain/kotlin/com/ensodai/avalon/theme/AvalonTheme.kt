package com.ensodai.avalon.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleFill
import com.ensodai.avalon.theme.models.BubbleShapeType
import com.ensodai.avalon.theme.models.ChatBackgroundConfig
import com.ensodai.avalon.theme.presets.CrimsonNightColors
import com.ensodai.avalon.theme.presets.GruvboxColors
import com.ensodai.avalon.theme.presets.MidnightPurpleColors
import com.ensodai.avalon.theme.presets.OceanDepthColors
import com.ensodai.avalon.theme.presets.PaperLightColors
import com.ensodai.avalon.theme.presets.RoseDawnColors
import com.ensodai.avalon.theme.presets.SketchPadColors
import com.ensodai.avalon.theme.presets.TechNoirColors
import kotlin.math.pow

/**
 * 🎨 [AvalonColors] — Единый источник истины для цветовой системы всего приложения.
 * Заменяет стандартный Material Theme и предоставляет расширенный набор токенов для
 * универсального создания как темных, так и светлых (и даже кастомных, вроде Gruvbox) тем.
 */
@Immutable
data class AvalonColors(
    /** 🔖 Уникальный идентификатор пресета темы (для сохранения в БД/настройках). */
    val preset: AvalonThemePreset,
    
    /** ☀️ Флаг светлой темы. Влияет на иконки системного статус-бара (черные/белые). */
    val isLight: Boolean,

    // --- Акценты (Accents) ---
    /** ⚡ Главный акцентный цвет (неоновые свечения, главные кнопки, основные выделения). */
    val neonAccent: Color,
    /** ℹ️ Информационный акцентный цвет (ссылки, информационные плашки). */
    val infoAccent: Color,
    /** 🔮 Вторичный акцент (дополнительные элементы, градиенты, парный цвет к neonAccent). */
    val accentSecondary: Color,
    /** 🤫 Приглушенный акцентный цвет (нейтральные действия, отмены, вторичные кнопки). */
    val mutedAccent: Color,
    /** 👑 Премиум-акцент (золотой/желтый цвет для подписок, платных функций). */
    val premiumAccent: Color,
    /** ✅ Цвет успеха (выполненные действия, зеленые галочки). */
    val successAccent: Color,
    /** ⚠️ Цвет предупреждения (оранжевый, важные уведомления). */
    val warningAccent: Color,

    // --- Утилиты (Utilities) ---
    /** 🛤️ Фон трека для ползунков и прогресс-баров. */
    val progressTrack: Color,
    /** 👻 Цвет текста-плейсхолдера (пустые поля ввода). */
    val placeholderText: Color,
    /** 🍔 Цвет "ручки" для перетаскивания (в боттом-шитах). */
    val dragHandle: Color,
    /** 🔘 Цвет нейтральных кнопок (кнопки отмены или закрытия). */
    val neutralButton: Color,

    // --- Статусы (Statuses) ---
    /** 🟢 Индикатор онлайна (зеленая точка). */
    val onlineStatus: Color,
    /** 🟡 Индикатор загрузки/подключения (ожидание ответа). */
    val connectingStatus: Color,

    // --- Премиум градиенты (Premium Gradients) ---
    /** 💎 Начальный цвет для градиентов подписки (кнопки апгрейда). */
    val upgradeGradientStart: Color,
    /** 💎 Конечный цвет для градиентов подписки. */
    val upgradeGradientEnd: Color,

    // --- Редкости (Rarities) ---
    /** 🃏 Свечение для "Необычных" персонажей. */
    val rarityUncommonGlow: Color,
    /** 🃏 Свечение для "Редких" персонажей. */
    val rarityRareGlow: Color,
    /** 🃏 Свечение для "Эпических" персонажей. */
    val rarityEpicGlow: Color,
    /** 🃏 Свечение для "Мифических" персонажей. */
    val rarityMythicGlow: Color,

    // --- Пузыри сообщений чата (Chat Bubbles) ---
    /** 💬 Цвет текста внутри сообщений пользователя. */
    val userBubbleTextColor: Color,
    /** 💬 Цвет текста внутри сообщений бота (персонажа). */
    val botBubbleTextColor: Color,
    /** 💭 Стиль заливки сообщений пользователя (сплошной или градиент). */
    val userBubble: BubbleFill,
    /** 💭 Стиль заливки сообщений бота (сплошной или градиент). */
    val botBubble: BubbleFill,
    /** 🫧 Обводка для сообщений пользователя. Если не нужна — [Color.Transparent]. */
    val userBubbleBorder: Color = Color.Transparent,
    /** 🫧 Обводка для сообщений бота. Если не нужна — [Color.Transparent]. */
    val botBubbleBorder: Color = Color.Transparent,

    // --- Поверхности и фоны (Surfaces & Backgrounds) ---
    /** 🌌 Глобальный фон приложения (экраны профиля, настройки). Базовый, самый глубокий слой. */
    val background: Color,
    /** 💬 Специфичный фон для экрана чата. Позволяет задать цвет (или картинку), независимый от [background]. */
    val chatBackground: ChatBackgroundConfig = ChatBackgroundConfig.Default,
    /** 🎴 Цвет фона для карточек ([GemCard], элементы списков). Выделяется на фоне [background]. */
    val cardBackground: Color,
    /** 🏔️ Самый высокий уровень поверхности (нижний бар ввода текста, пикеры эмодзи, выпадающие списки). */
    val surfaceHigh: Color,
    
    // --- Оверлеи и Попапы (Overlays & Popups) ---
    /** 🗂️ Цвет всплывающих окон (модалки, диалоги, попапы). */
    val overlaySurface: Color,
    /** 🖼️ Обводка для всплывающих окон. */
    val overlayBorder: Color,

    // --- Обводки (Strokes) ---
    /** 🖌️ Тонкая, едва заметная обводка (рамки карточек, разделители). В белых темах часто [Color.Transparent]. */
    val strokeSubtle: Color,
    /** 🖍️ Активная обводка (фокус на текстовом поле, выделенные элементы). */
    val strokeActive: Color,

    // --- Текст и Ошибки (Text & Errors) ---
    /** 📝 Основной цвет текста (заголовки, основной контент). */
    val textMain: Color,
    /** 📄 Вторичный текст (подписи, даты, менее важная информация). */
    val textSecondary: Color,
    /** 🚨 Цвет ошибки (красный, критические сбои, предупреждения). */
    val error: Color,

    // --- Тени (Elevations) ---
    /** ☁️ Тень для карточек. В плоских/темных темах `0.dp`, в материальных/светлых может быть `1.dp` или выше. */
    val cardElevation: Dp = 0.dp,

    // --- Геометрия пузырей (Bubble Geometry) ---
    /** 🔤 Размер шрифта в пузырях сообщений (12..30, default 16). */
    val bubbleFontSize: Int = 16,
    /** 💬 Форма пузыря: с хвостиком, округлый, или с мягким углом. */
    val bubbleShapeType: BubbleShapeType = BubbleShapeType.Tail,
    /** 📐 Радиус скругления углов пузыря (4..16, default 16). */
    val bubbleCornerRadius: Int = 16
)

object AvalonThemePresets {
    val fallback: AvalonColors = TechNoirColors

    fun resolve(preset: AvalonThemePreset): AvalonColors {
        return when (preset) {
            AvalonThemePreset.TechNoir -> TechNoirColors
            AvalonThemePreset.PaperLight -> PaperLightColors
            AvalonThemePreset.OceanDepth -> OceanDepthColors
            AvalonThemePreset.RoseDawn -> RoseDawnColors
            AvalonThemePreset.Gruvbox -> GruvboxColors
            AvalonThemePreset.MidnightPurple -> MidnightPurpleColors
            AvalonThemePreset.CrimsonNight -> CrimsonNightColors
            AvalonThemePreset.SketchPad -> SketchPadColors
        }
    }
}

fun AvalonColors.toMaterialColorScheme(): ColorScheme {
    return if (isLight) {
        lightColorScheme(
            primary = userBubble.primaryColor,
            onPrimary = Color.White,
            secondary = accentSecondary,
            onSecondary = Color.White,
            tertiary = neonAccent,
            background = background,
            onBackground = textMain,
            surface = background,
            onSurface = textMain,
            surfaceContainer = cardBackground,
            outline = strokeActive,
            outlineVariant = strokeSubtle,
            onSurfaceVariant = textSecondary,
            surfaceTint = Color.Transparent
        )
    } else {
        darkColorScheme(
            primary = neonAccent,
            onPrimary = Color.Black,
            secondary = accentSecondary,
            onSecondary = Color.White,
            tertiary = neonAccent,
            background = background,
            onBackground = textMain,
            surface = background,
            onSurface = textMain,
            surfaceContainer = cardBackground,
            surfaceContainerLow = cardBackground,
            surfaceContainerHigh = surfaceHigh,
            surfaceContainerHighest = surfaceHigh,
            outlineVariant = strokeSubtle,
            outline = strokeActive,
            onSurfaceVariant = textSecondary,
            surfaceTint = Color.Transparent
        )
    }
}

val LocalAvalonColors = staticCompositionLocalOf { TechNoirColors }

object AvalonTheme {
    val colors: AvalonColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAvalonColors.current
}

fun String.toColor(): Color? {
    return try {
        val hex = removePrefix("#")
        val color = hex.toLong(16)
        if (hex.length == 6) {
            Color(color or 0xFF000000L)
        } else {
            Color(color)
        }
    } catch (_: Exception) {
        null
    }
}

fun Color.toHex(): String {
    val alpha = (this.alpha * 255).toInt().toString(16).padStart(2, '0')
    val red = (this.red * 255).toInt().toString(16).padStart(2, '0')
    val green = (this.green * 255).toInt().toString(16).padStart(2, '0')
    val blue = (this.blue * 255).toInt().toString(16).padStart(2, '0')
    return "#$alpha$red$green$blue".uppercase()
}

/**
 * Подбирает читаемый цвет текста для цветного фона.
 * Использует WCAG contrast ratio вместо простого порога по RGB и,
 * если это безопасно по контрасту, предпочитает более мягкий near-white/near-black.
 */
fun Color.contrastText(): Color {
    return selectReadableTextColor(listOf(this))
}

fun BubbleFill.contrastText(): Color {
    val backgroundSamples = when (this) {
        is BubbleFill.Solid -> listOf(color)
        is BubbleFill.Gradient -> listOf(
            startColor,
            lerp(startColor, endColor, 0.5f),
            endColor
        )
    }

    return selectReadableTextColor(backgroundSamples)
}

private fun selectReadableTextColor(backgroundSamples: List<Color>): Color {
    val lightCandidates = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFF5F7FA),
        Color(0xFFEEF2F6)
    )
    val darkCandidates = listOf(
        Color(0xFF000000),
        Color(0xFF111318),
        Color(0xFF1A1D24)
    )

    val bestLight = lightCandidates.maxBy { candidate ->
        minimumContrastRatio(candidate, backgroundSamples)
    }
    val bestDark = darkCandidates.maxBy { candidate ->
        minimumContrastRatio(candidate, backgroundSamples)
    }

    val lightRatio = minimumContrastRatio(bestLight, backgroundSamples)
    val darkRatio = minimumContrastRatio(bestDark, backgroundSamples)
    val preferredGroup = if (lightRatio >= darkRatio) lightCandidates else darkCandidates
    val fallback = if (lightRatio >= darkRatio) bestLight else bestDark

    return preferredGroup.firstOrNull { candidate ->
        minimumContrastRatio(candidate, backgroundSamples) >= 4.5f
    } ?: fallback
}

private fun minimumContrastRatio(foreground: Color, backgrounds: List<Color>): Float {
    return backgrounds.minOf { background -> contrastRatio(foreground, background) }
}

private fun contrastRatio(foreground: Color, background: Color): Float {
    val lighter = maxOf(foreground.relativeLuminance(), background.relativeLuminance())
    val darker = minOf(foreground.relativeLuminance(), background.relativeLuminance())
    return (lighter + 0.05f) / (darker + 0.05f)
}

private fun Color.relativeLuminance(): Float {
    fun channel(value: Float): Float {
        return if (value <= 0.03928f) {
            value / 12.92f
        } else {
            ((value + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
        }
    }

    val r = channel(red)
    val g = channel(green)
    val b = channel(blue)
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}
