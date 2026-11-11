package com.ensodai.avalon.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavKey

/**
 * 🔥 Главный визуальный движок навигации (Orchestrator).
 *
 * Мост между строгим логическим стеком ([AvalonNavController]) и плавными визуальными
 * анимациями ([VisualStateManager]).
 *
 * Особенности:
 * 1. Сохраняет UI-стейт (скроллы, инпуты) через [SaveableStateHolder] даже для скрытых экранов.
 * 2. Управляет "Призраками" (экранами, удаленными из логики, но еще доигрывающими анимацию выхода).
 * 3. Рассчитывает сложный параллакс и затенение (scrim) на лету.
 * 4. Внедряет независимый [Lifecycle] и [ViewModelStore] в каждый экран.
 *    Это делает навигатор production-ready для работы с Koin ViewModel и StateFlow.
 *
 * @param controller Логический навигатор.
 * @param screenContent Фабрика Composable-экранов на основе маршрута [T].
 */
@Composable
fun <T : NavKey> AvalonNavHost(
    controller: AvalonNavController<T>,
    groupStackTransformer: ((List<T>) -> List<T>)? = null,
    isRouteSwipeable: (route: T) -> Boolean = { true },
    enableTransitions: Boolean = true,
    screenContent: @Composable (route: T) -> Unit
) {
    // Хранитель состояния (позволяет скроллу в LazyColumn не сбрасываться, когда экран уходит вниз стека)
    val saveableStateHolder = rememberSaveableStateHolder()
    var screenWidthPx by remember { mutableIntStateOf(0) }

    // Визуальный стейт-менеджер. Живет в remember, при повороте (восстановлении) создастся заново пустым.
    val manager = remember { VisualStateManager<T>() }
    // Читаем логический стек как State (рекомпозиция при добавлении/удалении)
    val logicalStackRaw = controller.backStack.toList()
    val logicalStack = remember(logicalStackRaw, groupStackTransformer) {
        groupStackTransformer?.invoke(logicalStackRaw) ?: logicalStackRaw
    }
    val animationScope = rememberCoroutineScope()

    // Интеграция с системной кнопкой "Назад" (и жестами Android 14+ Predictive Back)
    AvalonBackHandler(enabled = controller.canPop) {
        controller.pop()
    }

    // 🔥 СИНХРОНИЗАЦИЯ: Логика -> Визуал
    // Срабатывает каждый раз, когда меняется стек или ширина экрана.
    LaunchedEffect(logicalStack, screenWidthPx) {
        if (screenWidthPx > 0) {
            manager.sync(logicalStack, screenWidthPx.toFloat(), enableTransitions, animationScope)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { screenWidthPx = it.width }
    ) {
        if (screenWidthPx == 0) return@Box

        // Отрисовываем слои строго по их zIndex (от старых к новым)
        val sortedEntries = manager.entries.sortedBy { it.zIndex }

        sortedEntries.forEach { entry ->
            // Оптимизация: глубоко запрятанные экраны даже не доходят до фазы Композиции
            if (entry.state == VisualState.STASHED) return@forEach

            val translationX: Float
            val scrimAlpha: Float

            if (entry.state == VisualState.EXITING) {
                // 1. "ПРИЗРАКИ" (Экраны, которые уже удалены из стека, но летят вправо)
                // Они летят сами по себе, никто не может сдвинуть их параллаксом. Затенение убираем.
                translationX = entry.offset.value
                scrimAlpha = 0f
            } else {
                // 2. ЖИВЫЕ ЭКРАНЫ
                // Ищем ВСЕ видимые слои, которые находятся над текущим экраном
                val screensAbove = sortedEntries.filter {
                    it.zIndex > entry.zIndex && it.state != VisualState.STASHED
                }

                if (screensAbove.isNotEmpty()) {
                    // 🔥 ЭСТАФЕТА ПАРАЛЛАКСА (Brilliant UI Trick)
                    // Берем тот слой, который сильнее всего перекрывает экран (наименьший offset, т.е. самый левый).
                    // Это решает проблему множественных анимаций: например, старый экран улетает вправо,
                    // а новый УЖЕ выезжает из-под него. minOf плавно передает "управление" параллаксом
                    // нижним экранам без скачков.
                    val offsetAbove = screensAbove.minOf { it.offset.value }

                    // Максимальный сдвиг влево (PARALLAX_FACTOR), который плавно возвращается к 0
                    // по мере того, как верхний экран открывает нижний.
                    translationX = -screenWidthPx * AvalonTransitions.PARALLAX_FACTOR +
                            offsetAbove * AvalonTransitions.PARALLAX_FACTOR

                    // Затенение экрана под верхним слоем (макс 15% черного)
                    val progress = (offsetAbove / screenWidthPx).coerceIn(0f, 1f)
                    scrimAlpha = 0.15f * (1f - progress)
                } else {
                    // Мы самый верхний экран — рисуемся без параллакса и тени
                    translationX = entry.offset.value
                    scrimAlpha = 0f
                }
            }

            // Оборачиваем экран в провайдер состояния для сохранения скроллов/инпутов
            saveableStateHolder.SaveableStateProvider(key = entry.id) {

                // Создаем контекст жестов, доступный всему дереву этого экрана
                val swipeContext = remember(entry, screenWidthPx, controller.canPop) {
                    AvalonSwipeContext(
                        entry = entry,
                        screenWidth = screenWidthPx.toFloat(),
                        canPop = controller.canPop,
                        popAction = { duration ->
                            // Передаем динамическую длительность анимации отрыва в VisualEntry
                            entry.releaseDuration = duration
                            controller.pop()
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // 🔥 Выполняем сдвиг на фазе Отрисовки (Draw), избегая Рекомпозиции!
                        .graphicsLayer { this.translationX = translationX }
                        .drawWithContent {
                            drawContent()
                            // Отрисовка затемнения (scrim) прямо поверх контента
                            if (scrimAlpha > 0f) {
                                drawRect(Color.Black, alpha = scrimAlpha)
                            }
                        }
                        // Запрещаем кликам "проваливаться" сквозь прозрачные участки экрана на нижние слои
                        .preventTouchBleed()
                        .then(
                            // Вешаем слушатель системного свайпа ТОЛЬКО на верхний или въезжающий экран
                            if (entry.state == VisualState.ACTIVE || entry.state == VisualState.ENTERING) {
                                Modifier.swipeBack(
                                    screenWidth = screenWidthPx.toFloat(),
                                    enabled = controller.canPop && entry.isSwipeEnabled && isRouteSwipeable(entry.route),
                                    swipeState = entry.swipeState,
                                    offsetAnimatable = entry.offset,
                                    onPopRequest = { duration ->
                                        entry.releaseDuration = duration
                                        controller.pop()
                                    }
                                )
                            } else Modifier
                        )
                ) {
                    // Предоставляем контекст свайпа дочерним элементам и рендерим контент
                    CompositionLocalProvider(
                        LocalAvalonSwipeContext provides swipeContext,
                        LocalLifecycleOwner provides entry,
                        LocalViewModelStoreOwner provides entry
                    ) {
                        screenContent(entry.route)
                    }
                }
            }
        }
    }
}
