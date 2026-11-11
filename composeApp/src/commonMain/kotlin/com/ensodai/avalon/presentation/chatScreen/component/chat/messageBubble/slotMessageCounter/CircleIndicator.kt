package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.slotMessageCounter

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.presentation.chatScreen.viewState.SlotTypingStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.ensodai.avalon.theme.AvalonTheme

/**
 * Composable-компонент, отвечающий за отрисовку и анимацию одного круга-индикатора.
 *
 * Этот компонент является универсальным и управляет своим внешним видом (масштаб, прозрачность,
 * смещение) на основе текущего состояния `typingStatus`. Он инкапсулирует в себе всю
 * сложную логику анимаций, позволяя родительскому компоненту оставаться декларативным.
 *
 * ## Жизненный цикл и состояния:
 *
 * 1.  **`TypingStatus.NotTyping` (Состояние свайпа):**
 *     - Круг появляется и меняет свой масштаб (`swipeDrivenScale`) в зависимости от прогресса
 *       свайпа (`swipeProgress`).
 *     - При достижении порога "гигашлепка" (`isBouncing`), круг дополнительно "подпрыгивает".
 *
 * 2.  **`TypingStatus.Ready` (Состояние "слияния и разделения"):**
 *     - Запускается сложная двухфазная анимация, управляемая через `LaunchedEffect`.
 *     - **Фаза 1 ("Всасывание"):** Боковые круги уменьшаются, сдвигаются к центру и исчезают.
 *       Центральный круг увеличивается.
 *     - **Фаза 2 ("Выплевывание"):** После паузы происходит обратный процесс — круги с
 *       эффектом "выстрела" (`easingOut`) возвращаются на свои места.
 *
 * 3.  **`TypingStatus.Typing` (Состояние печати):**
 *     - Круг использует бесконечно анимируемый масштаб (`typingScale`), который передается
 *       извне, создавая эффект пульсации.
 *
 * ## Управление анимациями:
 *
 * -   **`updateTransition`:** Является главным "дирижером". Он плавно переключает
 *     `finalScale`, `finalAlpha` и `finalOffsetX` между значениями из разных состояний,
 *     обеспечивая бесшовные переходы. `transitionSpec` настроен так, чтобы вход и выход
 *     из состояния `Typing` были особенно плавными.
 * -   **`animateFloatAsState`:** Используется для более простых, стейт-зависимых анимаций,
 *     таких как "подпрыгивание" (`bounceScale`).
 * -   **`Animatable`:** Применяется для более сложных, императивно управляемых анимаций
 *     внутри `LaunchedEffect` (анимация слияния и разделения).
 *
 * @param index Индекс круга (0, 1 или 2). Используется для определения его роли
 *              (боковой или центральный) в анимации слияния.
 * @param swipeProgress Прогресс появления круга во время свайпа (от 0.0f до 1.0f).
 *                      Управляет начальным появлением в состоянии `NotTyping`.
 * @param isBouncing Флаг, указывающий, достигнут ли порог "гигашлепка". Активирует
 *                   пружинную анимацию "подпрыгивания".
 * @param typingStatus Текущее глобальное состояние, определяющее, какую анимацию проигрывать.
 * @param typingScale Значение масштаба для бесконечной анимации пульсации в состоянии `Typing`.
 *                    Рассчитывается и передается извне.
 * @param config Конфигурация для анимации в состоянии `Ready` (длительности, масштабы, кривые).
 * @param modifier Модификатор для компонента.
 */
@Composable
fun CircleIndicator(
    index: Int,
    containerWidth: Dp,
    swipeProgress: Float,
    isBouncing: Boolean,
    typingStatus: SlotTypingStatus,
    rarityTypingConfig: MessageCounterDefaults.TypingStateConfig,
    modifier: Modifier = Modifier,
    messageCounterDefaults: MessageCounterDefaults,
) {
    val colors = AvalonTheme.colors
    // 1. Анимации свайпа (NotTyping)
    val appearanceScale by animateFloatAsState(
        targetValue = if (swipeProgress > 0) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "Appearance"
    )
    val bounceScale by animateFloatAsState(
        targetValue = if (isBouncing) 1.3f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "Bounce"
    )
    val swipeDrivenScale = appearanceScale * bounceScale

    // 2. ГЛАВНЫЙ АНИМАТОР
    val scaleAnimatable = remember { Animatable(1f) }
    val offsetXAnimatable = remember { Animatable(0f) }
    val alphaAnimatable = remember { Animatable(1f) }

    val density = LocalDensity.current

    // 3. ЛОГИКА ЭМОДЗИ

    // Пул эмодзи
    val emojis = remember { listOf("🔥", "👀", "⚡", "💎", "👻", "💀", "💩", "🤖", "👽", "🦄", "🍕", "🚀") }

    // Текущий эмодзи
    var currentEmoji by remember { mutableStateOf(emojis.first()) }

    // Генерируем новый ТОЛЬКО при пересечении порога (False -> True)
    LaunchedEffect(isBouncing) {
        if (isBouncing) {
            currentEmoji = emojis.random()
        }
    }

    // Логика отрисовки: показываем эмодзи только при Гигашлепе
    // Но если мы уже начали печатать (Typing), эмодзи должен исчезнуть, чтобы не ломать анимацию слияния
    val showEmoji = isBouncing && typingStatus !is SlotTypingStatus.Typing

    LaunchedEffect(typingStatus) {
        if (typingStatus is SlotTypingStatus.Typing) {
            val readyConfig = messageCounterDefaults.readyAnimationConfig

            // Расчет дистанции
            val targetDistanceDp = (containerWidth + messageCounterDefaults.CircleSize) / 4
            val targetDistancePx = with(density) { targetDistanceDp.toPx() }

            // ЭТАП 1: ВСАСЫВАНИЕ (MERGE)
            launch {
                offsetXAnimatable.animateTo(
                    targetValue = when (index) {
                        0 -> targetDistancePx
                        2 -> -targetDistancePx
                        else -> 0f
                    },
                    animationSpec = tween(readyConfig.mergeDuration, easing = readyConfig.easingIn)
                )
            }

            launch {
                if (index != 1) {
                    delay((readyConfig.mergeDuration * 0.8).toLong())
                    alphaAnimatable.animateTo(0f, tween((readyConfig.mergeDuration * 0.2).toInt()))
                }
            }

            scaleAnimatable.animateTo(
                targetValue = if (index == 1) 2.2f else 0.1f,
                animationSpec = tween(readyConfig.mergeDuration, easing = readyConfig.easingIn)
            )

            // ЭТАП 2: ВЫПЛЕВЫВАНИЕ (RELEASE)
            if (index != 1) alphaAnimatable.snapTo(1f)

            launch {
                offsetXAnimatable.animateTo(
                    0f,
                    tween(readyConfig.releaseDuration, easing = readyConfig.easingOut)
                )
            }

            // Масштаб + Отдача (Recoil)
            if (index == 1) {
                // Сдутие
                scaleAnimatable.animateTo(
                    targetValue = 0.7f,
                    animationSpec = tween(
                        (readyConfig.releaseDuration * 0.4).toInt(),
                        easing = LinearOutSlowInEasing
                    )
                )
                // Восстановление
                scaleAnimatable.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
                )
            } else {
                // Боковые
                scaleAnimatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        readyConfig.releaseDuration,
                        easing = readyConfig.easingOut
                    )
                )
            }

            // ЭТАП 3: БЕСШОВНАЯ ОРГАНИЧНАЯ ПУЛЬСАЦИЯ
            // -------------------------------------------------------------

            val waveDelay = index * rarityTypingConfig.waveDelayStep

            if (waveDelay > 0) {
                // Запускаем анимацию альфы параллельно с ожиданием
                launch {
                    alphaAnimatable.animateTo(
                        targetValue = rarityTypingConfig.startAlpha,
                        animationSpec = tween(
                            durationMillis = waveDelay.toInt(),
                            easing = LinearEasing
                        )
                    )
                }
                delay(waveDelay)
            } else {
                // Для первого круга делаем быстрый, но плавный переход (не snap),
                // чтобы глаз не заметил подвоха
                alphaAnimatable.animateTo(
                    targetValue = rarityTypingConfig.startAlpha,
                    animationSpec = tween(150, easing = LinearEasing)
                )
            }

            while (isActive) {

                val totalDuration =
                    (rarityTypingConfig.baseDuration * rarityTypingConfig.durationMultiplier).toInt()
                val inhaleTime = (totalDuration * rarityTypingConfig.inhaleRatio).toInt()
                val exhaleTime = totalDuration - inhaleTime

                // === ВДОХ ===
                launch {
                    alphaAnimatable.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(inhaleTime, easing = LinearEasing)
                    )
                }
                scaleAnimatable.animateTo(
                    targetValue = rarityTypingConfig.targetScale,
                    animationSpec = tween(
                        durationMillis = inhaleTime,
                        easing = FastOutSlowInEasing
                    )
                )

                // === ВЫДОХ ===
                launch {
                    alphaAnimatable.animateTo(
                        targetValue = rarityTypingConfig.minAlpha,
                        animationSpec = tween(exhaleTime, easing = LinearEasing)
                    )
                }
                scaleAnimatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = exhaleTime,
                        easing = LinearOutSlowInEasing
                    )
                )

                // === ПАУЗА ===
                delay(rarityTypingConfig.restDuration)
            }

        } else {
            // Reset
            launch { scaleAnimatable.snapTo(1f) }
            launch { offsetXAnimatable.snapTo(0f) }
            launch { alphaAnimatable.snapTo(1f) }
        }
    }

    // --- СБОРКА ---
    val finalScale = when (typingStatus) {
        is SlotTypingStatus.Typing -> scaleAnimatable.value
        else -> swipeDrivenScale
    }

    val finalAlpha = when (typingStatus) {
        is SlotTypingStatus.Typing -> alphaAnimatable.value
        else -> if (swipeProgress > 0.01f) 1f else 0f
    }

    val finalOffsetX = if (typingStatus is SlotTypingStatus.Typing) offsetXAnimatable.value else 0f

    val isVisible = swipeProgress > 0.01f || typingStatus !is SlotTypingStatus.NotTyping
    val overallAlpha by animateFloatAsState(if (isVisible) 1f else 0f)

    Box(
        modifier = modifier
            .size(messageCounterDefaults.CircleSize)
            .graphicsLayer {
                scaleX = finalScale
                scaleY = finalScale
                alpha = finalAlpha * overallAlpha
                translationX = finalOffsetX
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = showEmoji,
            label = "EmojiMorph",
            transitionSpec = {

                (fadeIn(animationSpec = tween(200)) +
                        scaleIn(initialScale = 0.5f, animationSpec = spring(dampingRatio = 0.6f)))
                    .togetherWith(
                        fadeOut(animationSpec = tween(200)) +
                                scaleOut(targetScale = 0.5f, animationSpec = tween(200))
                    )
            }
        ) { isEmojiVisible ->
            if (isEmojiVisible) {
                Text(
                    text = currentEmoji,
                    style = TextStyle(
                        fontSize = 11.sp,
                        lineHeight = 11.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.wrapContentSize(Alignment.Center)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.textMain, shape = CircleShape)
                )
            }
        }
    }
}
