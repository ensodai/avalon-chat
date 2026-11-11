package com.ensodai.avalon.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.launch

/**
 * Стандартный модификатор для перехвата жеста "свайп назад" на уровне всего экрана (AvalonNavHost).
 *
 * 🔥 АРХИТЕКТУРА УМНОГО ПЕРЕХВАТА:
 * Модификатор динамически выбирает фазу перехвата ([PointerEventPass]) в зависимости от того,
 * где пользователь коснулся экрана:
 *
 * 1. "Диктатор" (PointerEventPass.Initial): Если палец опустился в слепой зоне у левого края
 *    (до 30dp), мы воруем жест сразу на фазе туннелирования. Никакие вложенные горизонтальные
 *    скроллы или пейджеры даже не узнают о касании. Это защищает системный жест навигации.
 *
 * 2. "Вежливый" (PointerEventPass.Main): Если палец коснулся центра экрана, мы позволяем
 *    дочерним элементам (например, `LazyRow`) обработать жест первыми. Если они его проигнорируют,
 *    модификатор заберет свайп себе.
 *
 * @param screenWidth Физическая ширина экрана (для расчетов прогресса и краевой зоны).
 * @param enabled Включен ли жест (обычно `false`, если экран единственный в стеке).
 * @param swipeState Реактивное состояние жеста, прокидываемое для обновления UI (теней, блокировок).
 * @param offsetAnimatable Аниматор сдвига текущего экрана.
 * @param onPopRequest Коллбэк, вызываемый при успешном завершении жеста закрытия.
 */
fun Modifier.swipeBack(
    screenWidth: Float,
    enabled: Boolean,
    swipeState: MutableState<SwipeBackState>,
    offsetAnimatable: Animatable<Float, *>,
    onPopRequest: (duration: Int) -> Unit
): Modifier = if (!enabled) this else this.pointerInput(screenWidth) {
    // Рассчитываем размер краевой зоны в пикселях на основе константы (30dp)
    val edgeZonePx = screenWidth * (AvalonTransitions.EDGE_SWIPE_ZONE_DP / 360f)

    handleAvalonGesture(
        screenWidth = { screenWidth },
        offsetAnimatable = offsetAnimatable,
        swipeState = swipeState,
        determinePass = { downX ->
            // Умный выбор фазы перехвата в зависимости от координаты X при касании
            if (downX <= edgeZonePx) PointerEventPass.Initial else PointerEventPass.Main
        },
        shouldIntercept = { true }, // Стандартный хост-перехватчик активируется при любом смещении
        onPopRequest = onPopRequest
    )
}

/**
 * Кастомный модификатор свайпа для использования ВНУТРИ экранов со сложным скроллом.
 *
 * Позволяет делегировать закрытие экрана определенному дочернему элементу (например,
 * первой странице в `HorizontalPager` или полноэкранной галерее при достижении левого края).
 *
 * В отличие от `swipeBack`, этот модификатор:
 * 1. Берет зависимости напрямую из `LocalAvalonSwipeContext`.
 * 2. ВСЕГДА работает в режиме "Диктатора" (`PointerEventPass.Initial`), так как применяется
 *    к конкретному элементу, который уже решил, что хочет отдать жест родительской навигации.
 * 3. Позволяет программисту самому решать, при каком условии начать тянуть экран (`shouldIntercept`).
 *
 * @param enabled Флаг ручного отключения модификатора.
 * @param shouldIntercept Функция-предикат: если возвращает `true`, жест переходит к навигации
 *        (экран начинает тянуться). Например: `if (pagerState.currentPage == 0)`.
 */
fun Modifier.avalonCustomSwipe(
    enabled: Boolean = true,
    shouldIntercept: (dragDeltaX: Float) -> Boolean
): Modifier = composed {
    // Безопасно получаем контекст. Если мы не внутри AvalonNavHost — просто ничего не делаем.
    val swipeContext = LocalAvalonSwipeContext.current ?: return@composed this

    val platformMetadata: com.ensodai.avalon.domain.model.PlatformMetadata = org.koin.compose.koinInject()
    val isWeb = platformMetadata.platform == "Web"

    // Блокируем логику, если:
    // 1) отключено вручную, 2) мы в вебе, 3) мы в корне стека (некуда делать pop), 4) экран временно заблокирован
    if (!enabled || isWeb || !swipeContext.canPop || !swipeContext.entry.isSwipeEnabled) return@composed this

    this.pointerInput(swipeContext.screenWidth, swipeContext.entry) {
        handleAvalonGesture(
            screenWidth = { swipeContext.screenWidth },
            offsetAnimatable = swipeContext.entry.offset,
            swipeState = swipeContext.entry.swipeState,
            // Custom Swipe всегда безусловно перехватывает ивенты на первой фазе,
            // так как условие перехвата делегировано в shouldIntercept.
            determinePass = { PointerEventPass.Initial },
            shouldIntercept = shouldIntercept,
            onPopRequest = swipeContext.popAction
        )
    }
}

/**
 * Локализованный модификатор свайпа назад для конкретной панели (например, правой).
 */
fun Modifier.paneSwipeBack(
    enabled: Boolean,
    onPopRequest: (duration: Int) -> Unit
): Modifier = composed {
    if (!enabled) return@composed this

    var paneWidthPx by remember { mutableFloatStateOf(0f) }
    val swipeState = remember { mutableStateOf(SwipeBackState()) }
    val offsetAnimatable = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    
    this
       .onSizeChanged { paneWidthPx = it.width.toFloat() } // Читаем ширину при компоновке
        // Смещаем UI на вычисленный оффсет
       .graphicsLayer { translationX = offsetAnimatable.value } 
       .pointerInput(enabled) { // Убрали paneWidthPx из ключей, чтобы избежать крашей при анимации
            awaitPointerEventScope {
                // Пустой скоуп просто для гарантии, что жест читает актуальные данные внутри себя
                // Но так как handleAvalonGesture - это suspend функция, мы её вызываем прямо в pointerInput
            }
            
            // Вызываем жест. Внутри он будет использовать актуальное значение paneWidthPx.
            handleAvalonGesture(
                screenWidth = { paneWidthPx }, // Будет взято актуальное значение благодаря замыканию
                offsetAnimatable = offsetAnimatable,
                swipeState = swipeState,
                determinePass = { downX ->
                    if (!enabled || paneWidthPx == 0f) return@handleAvalonGesture PointerEventPass.Main
                    val edgeZonePx = paneWidthPx * (AvalonTransitions.EDGE_SWIPE_ZONE_DP.toFloat() / 360f)
                    if (downX <= edgeZonePx) PointerEventPass.Initial else PointerEventPass.Main
                },
                shouldIntercept = { enabled && paneWidthPx > 0f },
                onPopRequest = { duration -> 
                    onPopRequest(duration)
                    scope.launch { offsetAnimatable.snapTo(0f) } 
                }
            )
        }
}
