package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.slotMessageCounter

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.avalon.theme.AvalonColors
import com.ensodai.avalon.theme.AvalonTheme

private fun overshootEasing(tension: Float): Easing = Easing { fraction ->
    val shifted = fraction - 1f
    shifted * shifted * ((tension + 1f) * shifted + tension) + 1f
}

// =================================================================================
// 1. КОНФИГУРАЦИЯ: Все "магические числа" и конфиги анимаций в одном месте.
// =================================================================================

/**
 * Синглтон-объект, который централизует все "магические числа" и конфигурации анимаций
 * для компонента `SlotMessageCounter`.
 *
 * Этот подход позволяет легко настраивать внешний вид и поведение всех анимаций в одном месте,
 * не затрагивая основную логику Composable-функций.
 */
class MessageCounterDefaults(
    private val colors: AvalonColors
) {
    // =================================================================================
    // 1. БАЗОВЫЕ РАЗМЕРЫ И КОНСТАНТЫ
    // =================================================================================

    // Основные размеры UI элементов.
    val BaseWidth: Dp = 70.dp
    val GigaslepWidth: Dp = 70.dp
    val BaseHeight: Dp = 35.dp
    val Padding = PaddingValues(horizontal = 8.dp)
    val CornerRadius: Dp = 16.dp
    val CircleSize: Dp = 12.dp


    // =================================================================================
    // 2. КОНФИГУРАЦИИ АНИМАЦИЙ
    // =================================================================================

    /**
     * Определяет "окна" появления для каждого из трех кругов во время свайпа.
     * Каждый `CircleSwipeConfig` задает начальную и конечную точку анимации как долю
     * от общего порога свайпа (`midSwipeThreshold`).
     */
    val CircleAppearanceConfigs = listOf(
        CircleSwipeConfig(startFactor = 0.1f, endFactor = 0.2f), // Первый круг
        CircleSwipeConfig(startFactor = 0.3f, endFactor = 0.5f), // Второй круг
        CircleSwipeConfig(startFactor = 0.6f, endFactor = 0.8f), // Третий круг
    )

    /** Задержки в миллисекундах для стартовой фазы анимации пульсации в состоянии `Typing`.
     *  Это создает эффект асинхронной, "живой" пульсации. */
    val CircleDelaysMs = listOf(0, 150, 300)

    /**
     * Конфигурация для анимированной обводки, которая появляется в состоянии `Ready`.
     * @param borderWidth Толщина линии обводки.
     * @param borderColor Основной цвет обводки. В режиме "кометы" используется как цвет "хвоста".
     * @param glowRadius Радиус размытия для эффекта свечения. `0.dp` отключает свечение.
     * @param glowColor Опциональный цвет для свечения. Если `null`, используется `borderColor`.
     * @param headColor Опциональный цвет для "головы" кометы. Если не `null`, активирует
     *                  режим отрисовки с градиентом.
     */
    data class BorderAnimationConfig(
        val borderWidth: Dp = 2.dp,
        val borderColor: Color = Color.Unspecified,
        val glowRadius: Dp = 0.dp,
        val glowColor: Color? = null,
        val headColor: Color? = null
    )

    val borderAnimationConfig get() = BorderAnimationConfig(borderColor = colors.textMain)

    /**
     * Конфигурация для двухфазной анимации "слияния и разделения" кругов в состоянии `Ready`.
     * @param mergeDuration Длительность фазы "всасывания" кругов к центру (в мс).
     * @param releaseDuration Длительность фазы "выплевывания" кругов обратно (в мс).
     * @param centerCircleScale Целевой масштаб центрального круга в конце фазы "всасывания".
     * @param sideCircleScale Целевой масштаб боковых кругов в конце фазы "всасывания".
     * @param easingIn Кривая анимации для фазы "всасывания".
     * @param easingOut Кривая анимации для фазы "выплевывания" (часто с эффектом "отскока").
     * @param borderEasing Кривая анимации для появления и исчезновения обводки.
     */
    data class ReadyAnimationConfig(
        val mergeDuration: Int = 500,
        val releaseDuration: Int = 600,
        val centerCircleScale: Float = 1.8f,
        val sideCircleScale: Float = 0.5f,
        val easingIn: Easing = FastOutSlowInEasing,
        val easingOut: Easing = overshootEasing(2f),
        val borderEasing: Easing = FastOutSlowInEasing
    )

    val readyAnimationConfig = ReadyAnimationConfig()

    /**
     * Конфигурация для непрерывной анимации пульсации в состоянии `Typing`.
     * @param targetScale Максимальный масштаб, до которого пульсирует круг.
     * @param duration Длительность одного цикла пульсации (от 1.0 до targetScale) в мс.
     */
    data class TypingStateConfig(
        // Максимальный размер круга при вдохе (1.4 = +40%)
        val targetScale: Float = 1.4f,

        // Базовая скорость одного такта (в мс), которая умножается на multiplier
        val baseDuration: Int = 400,

        // Множитель замедления. 3.5 означает, что реальный цикл будет 400 * 3.5 = 1400 мс
        val durationMultiplier: Float = 3.5f,

        // Какую часть времени занимает ВДОХ (0.45 = 45% времени растем, 55% сжимаемся)
        val inhaleRatio: Float = 0.45f,

        // Задержка между запуском анимации у соседних кругов (эффект волны)
        val waveDelayStep: Long = 120L,

        // Длительность паузы (отдыха) в нижней точке перед новым вдохом
        val restDuration: Long = 200L,

        // Прозрачность в нижней точке (выдох). Чем меньше, тем сильнее "тонет" круг.
        val minAlpha: Float = 0.6f,

        // Прозрачность, с которой начинается анимация (после взрыва)
        val startAlpha: Float = 0.7f
    )

    val typingStateConfig = TypingStateConfig()

    /**
     * Мастер-контейнер, который группирует все конфигурации анимаций для определенной "редкости".
     * @param readyAnimation Конфигурация для состояния `Ready`.
     * @param typing Конфигурация для состояния `Typing`.
     * @param borderAnimation Опциональная конфигурация для анимированной обводки.
     *                        Если `null`, обводка для данной редкости не отображается.
     */
    data class RarityAnimationConfig(
        val readyAnimation: ReadyAnimationConfig = ReadyAnimationConfig(),
        val typing: TypingStateConfig = TypingStateConfig(),
        val borderAnimation: BorderAnimationConfig? = null
    )

    val rarityAnimationConfig = RarityAnimationConfig()

    /**
     * Центральная карта (look-up table), связывающая каждую `Rarity` с ее уникальным набором
     * анимационных параметров. Это единственное место, которое нужно редактировать для
     * настройки визуальных эффектов для разных уровней редкости.
     */
    /**
     * НАСТРОЙКИ ДЛЯ РАЗНЫХ РЕДКОСТЕЙ
     * Теперь ты можешь делать EPIC более медленным и глубоким, а COMMON более быстрым.
     */
    val RarityAnimations: Map<Rarity, RarityAnimationConfig> = mapOf(
        Rarity.COMMON to RarityAnimationConfig(
            borderAnimation = null,
            readyAnimation = ReadyAnimationConfig(),
            typing = TypingStateConfig(
                targetScale = 1.3f,         // Чуть меньше
                durationMultiplier = 3.0f,  // Чуть быстрее
                minAlpha = 0.7f             // Менее глубокое затухание
            )
        ),
        Rarity.UNCOMMON to RarityAnimationConfig(
            borderAnimation = BorderAnimationConfig(
                borderWidth = 0.5.dp,
                borderColor = colors.rarityUncommonGlow
            ),
            readyAnimation = ReadyAnimationConfig(centerCircleScale = 1.4f),
            typing = TypingStateConfig(
                targetScale = 1.4f,
                durationMultiplier = 3.2f
            )
        ),
        Rarity.RARE to RarityAnimationConfig(
            borderAnimation = BorderAnimationConfig(
                borderWidth = 0.6.dp,
                borderColor = colors.rarityRareGlow,
                glowRadius = 4.dp,
                headColor = colors.textMain
            ),
            readyAnimation = ReadyAnimationConfig(centerCircleScale = 1.4f),
            typing = TypingStateConfig(
                targetScale = 1.45f,
                durationMultiplier = 3.5f // Стандартное глубокое дыхание
            )
        ),
        Rarity.EPIC to RarityAnimationConfig(
            borderAnimation = BorderAnimationConfig(
                borderWidth = 0.7.dp,
                borderColor = colors.rarityEpicGlow,
                glowRadius = 8.dp,
                headColor = colors.textMain.copy(alpha = 0.9f)
            ),
            readyAnimation = ReadyAnimationConfig(centerCircleScale = 1.4f),
            typing = TypingStateConfig(
                targetScale = 1.5f,        // Сильное увеличение
                durationMultiplier = 3.8f, // Очень медленное, величественное дыхание
                minAlpha = 0.5f,           // Глубокое затухание
                restDuration = 300L        // Долгая пауза
            )
        ),
        Rarity.MYTHIC to RarityAnimationConfig(
            borderAnimation = BorderAnimationConfig(
                borderWidth = 0.8.dp,
                borderColor = colors.rarityMythicGlow,
                glowRadius = 4.dp,
                headColor = colors.textMain
            ),
            readyAnimation = ReadyAnimationConfig(centerCircleScale = 1.6f, releaseDuration = 700),
            typing = TypingStateConfig(
                targetScale = 1.6f,         // Огромный пульс
                durationMultiplier = 4.0f,  // Максимально медленно
                minAlpha = 0.4f,            // Почти исчезает
                restDuration = 400L
            )
        ),
    )
}

/**
 * Конфигурация анимации для одного круга во время свайпа.
 * @param startFactor Множитель (0.0 до 1.0) от `midSwipeThreshold`, когда круг начинает появляться.
 * @param endFactor Множитель (0.0 до 1.0) от `midSwipeThreshold`, когда круг заканчивает появляться.
 */
data class CircleSwipeConfig(val startFactor: Float, val endFactor: Float)
