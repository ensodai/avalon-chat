package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.slotMessageCounter

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.swipeableMessageBubble.SwipeToActionState
import com.ensodai.avalon.presentation.chatScreen.viewState.SlotTypingStatus
import com.ensodai.avalon.theme.AvalonTheme
import kotlinx.coroutines.delay


/**
 * Основной Composable-компонент, отображающий счетчик сообщений с продвинутой анимацией
 * перехода в режим индикатора печати.
 *
 * Этот компонент является "дирижером", который собирает воедино все состояния, конфигурации
 * и дочерние компоненты (`CircleIndicator`), чтобы создать сложный визуальный эффект.
 *
 * ## Основные состояния и логика:
 *
 * 1.  **Режим счетчика (начальное состояние):**
 *     - Отображается текст вида "1/10".
 *     - Прозрачность текста (`textAlpha`) уменьшается по мере свайпа, плавно переходя
 *       в режим индикаторов.
 *
 * 2.  **Режим индикаторов (во время свайпа и в состояниях `Ready`/`Typing`):**
 *     - Текст становится полностью прозрачным.
 *     - Отображается `Row` с тремя компонентами `CircleIndicator`.
 *     - Логика появления и анимации каждого круга делегируется `CircleIndicator`.
 *
 * ## Управление анимациями:
 *
 * -   **Анимация ширины:** Ширина контейнера (`width`) анимируется в зависимости от
 *     смещения свайпа (`swipeToActionState.offset`), создавая эффект растягивания.
 * -   **Анимация кругов:**
 *     - `swipeProgresses`: Высчитывается прогресс появления для каждого круга на основе
 *       конфигурации `CircleAppearanceConfigs`.
 *     - `rarityTypingScales`: Запускается и управляется бесконечная анимация пульсации
 *       для состояния `Typing` через `rememberRarityTypingAnimation`.
 * -   **Анимация обводки:**
 *     - Управляется через `LaunchedEffect`, который следит за `typingStatus`.
 *     - Имеет полный жизненный цикл: плавное появление в `Ready`, удержание в `Typing`
 *       и плавное исчезновение в `NotTyping`.
 *     - Использует кастомный модификатор `animatedBorder`, параметры для которого
 *       (цвет, свечение, градиент) берутся из `rarityConfig`.
 *
 * ## Структура и зависимости:
 *
 * -   Получает все необходимые данные (индексы, состояние свайпа, редкость, статус) извне.
 * -   Извлекает соответствующую конфигурацию анимаций `rarityConfig` из `MessageCounterDefaults`.
 * -   Передает каждому `CircleIndicator` его персональные параметры для анимации.
 * -   Условно применяет модификатор `animatedBorder` в зависимости от наличия `borderConfig`.
 *
 * @param modifier Модификатор для корневого `Box` компонента.
 * @param activeMessageIndex Индекс текущего активного сообщения.
 * @param allMessageCount Общее количество сообщений.
 * @param swipeToActionState Состояние свайпа, предоставляющее смещение и конфигурацию.
 * @param messageRarity "Редкость" сообщения, определяющая, какой набор анимаций использовать.
 * @param typingStatus Глобальное состояние (свайп, готовность, печать), управляющее
 *                     ключевыми анимационными переходами.
 */
@Composable
fun SlotMessageCounter(
    modifier: Modifier,
    activeMessageIndex: Int,
    allMessageCount: Int,
    swipeToActionState: SwipeToActionState,
    messageRarity: Rarity,
    typingStatus: SlotTypingStatus
) {
    val colors = AvalonTheme.colors
    val messageCounterDefaults = remember(colors) { MessageCounterDefaults(colors) }

    val rarityConfig = messageCounterDefaults.RarityAnimations[messageRarity]
        ?: messageCounterDefaults.RarityAnimations.getValue(Rarity.COMMON)

    val borderDrawProgress = remember { Animatable(0f) }
    val borderConfig = rarityConfig.borderAnimation

    LaunchedEffect(typingStatus, borderConfig) {
        val animSettings = rarityConfig.readyAnimation
        when (typingStatus) {
            is SlotTypingStatus.Ready -> borderDrawProgress.snapTo(0f)
            is SlotTypingStatus.Typing -> {
                if (borderConfig != null) {
                    if (borderDrawProgress.value != 1f) {
                        delay(animSettings.mergeDuration.toLong()) // Если нужно
                        borderDrawProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                animSettings.releaseDuration,
                                easing = animSettings.borderEasing
                            )
                        )
                    } else borderDrawProgress.snapTo(1f)
                } else borderDrawProgress.snapTo(0f)
            }

            is SlotTypingStatus.NotTyping -> {
                if (borderDrawProgress.value != 0f) borderDrawProgress.animateTo(0f, tween(300))
            }
        }
    }

    val width = calculateAnimatedWidth(swipeToActionState.offset, messageCounterDefaults)
    val textAlpha = calculateTextAlpha(swipeToActionState)
    val hasGigashlepTriggered = rememberGigashlepState(swipeToActionState)

    val inflationPadding by animateDpAsState(
        targetValue = if (hasGigashlepTriggered) 1.5.dp else 6.dp,
        animationSpec = spring(
            dampingRatio = 0.45f, // Чуть меньше, чтобы прыгало сильнее
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "inflationPadding"
    )

    val pressureCornerRadius by animateDpAsState(
        targetValue = if (hasGigashlepTriggered) 6.dp else messageCounterDefaults.CornerRadius,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pressureRadius"
    )

    val swipeProgresses = messageCounterDefaults.CircleAppearanceConfigs.map { config ->
        calculateSwipeProgress(
            swipeToActionState.offset,
            swipeToActionState.config.midSwipeThreshold,
            config
        )
    }

    Box(
        modifier = modifier
            // Сначала задаем жесткий размер контейнера. Больше этого он не станет.
            .size(width = width, height = messageCounterDefaults.BaseHeight)
            // Внутренний паддинг, который создает эффект "дыхания" размером
            .padding(
                start = inflationPadding.coerceAtLeast(0.dp),
                end = inflationPadding.coerceAtLeast(0.dp)
            )
            .graphicsLayer {
                // Оставляем это для правильного роста вверх и влево
                transformOrigin = TransformOrigin(1f, 1f)
            }
            // Применяем форму (Clip должен быть ПОСЛЕ паддинга, чтобы резать уже уменьшенный бокс)
            .clip(RoundedCornerShape(pressureCornerRadius))
            // Применяем бордер и фон
            .let { baseModifier ->
                if (borderConfig != null) {
                    baseModifier.animatedBorder(
                        progress = borderDrawProgress.value,
                        cornerRadius = pressureCornerRadius, // Анимируем радиус бордера тоже
                        borderWidth = borderConfig.borderWidth,
                        borderColor = borderConfig.borderColor,
                        glowRadius = borderConfig.glowRadius,
                        glowColor = borderConfig.glowColor,
                        headColor = borderConfig.headColor
                    )
                } else {
                    baseModifier
                }
            }
            .background(colors.cardBackground.copy(alpha = if (hasGigashlepTriggered) 0.9f else 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$activeMessageIndex/$allMessageCount",
            color = colors.neonAccent.copy(alpha = textAlpha),
            modifier = Modifier.alpha(textAlpha)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .alpha(1f - textAlpha),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(messageCounterDefaults.CircleAppearanceConfigs.size) { index ->
                CircleIndicator(
                    index = index,
                    containerWidth = width - (inflationPadding * 2),
                    swipeProgress = swipeProgresses[index],
                    isBouncing = hasGigashlepTriggered,
                    typingStatus = typingStatus,
                    rarityTypingConfig = rarityConfig.typing,
                    messageCounterDefaults = messageCounterDefaults,
                )
            }
        }
    }
}
