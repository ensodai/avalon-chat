package com.ensodai.avalon.navigation

import androidx.compose.animation.core.tween
import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.NavKey
import com.ensodai.avalon.utils.L
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 🔥 Главный мозг визуальной навигации (Orchestrator / State Machine).
 *
 * Отвечает за процесс Reconciliation (Согласования): берет новый логический стек
 * и вычисляет разницу (diff) с текущим визуальным отображением на экране.
 *
 * Решает три фундаментальные задачи:
 * 1. Рождение: Создает новые экраны и запускает анимацию их выезда слева направо.
 * 2. Жизнь (Распределение ролей): Раздает статусы (кто ACTIVE, кто рисует параллакс UNDERNEATH, а кто STASHED).
 * 3. Смерть (Призраки и Сборка мусора): Позволяет экранам "жить" на экране даже после их удаления
 *    из логического стека, чтобы плавно проиграть анимацию отлета вправо. После завершения
 *    анимации безопасно уничтожает Lifecycle и чистит память от ViewModel.
 */
class VisualStateManager<T : NavKey> {
    /**
     * Реактивный список всех существующих визуальных слоев.
     * Чтение этого списка в AvalonNavHost заставляет UI перерисовываться при добавлении/удалении экранов.
     */
    val entries = mutableStateListOf<VisualEntry<T>>()

    // Монотонно возрастающий счетчик слоев.
    // Гарантирует, что каждый новый открытый экран ВСЕГДА ложится поверх всех предыдущих (и призраков).
    private var zIndexCounter = 0f

    // Уникальный идентификатор для SaveableStateHolder.
    // Защищает от коллизий, если в стек был добавлен один и тот же маршрут дважды через время.
    private var idCounter = 0L

    /**
     * Главная функция синхронизации. Вызывается при КАЖДОМ изменении логического стека
     * или при повороте/изменении размеров экрана.
     *
     * @param logicalStack Актуальный список маршрутов из AvalonNavController.
     * @param screenWidth Физическая ширина экрана (для расчета сдвигов за пределы экрана).
     * @param scope Корутин-скоуп, привязанный к жизненному циклу AvalonNavHost (для запуска анимаций).
     */
    fun sync(logicalStack: List<T>, screenWidth: Float, enableTransitions: Boolean, scope: CoroutineScope) {
        // "Живые" экраны — это все, кроме тех, которые прямо сейчас улетают за край (EXITING).
        val activeEntries = entries.filter { it.state != VisualState.EXITING }
        val newEntries = mutableListOf<VisualEntry<T>>()

        // ====================================================================
        // 🔥 ФАЗА 1: ДЕТЕКТ ВОССТАНОВЛЕНИЯ (ХОЛОДНЫЙ СТАРТ / ПОВОРОТ ЭКРАНА)
        // ====================================================================

        // Детектим поворот: менеджер пуст, а в стеке уже больше одного экрана.
        // Это позволяет нам пропустить анимации для всех экранов, кроме новых.
        val isRestoring = entries.isEmpty() && logicalStack.size > 1

        // Проходим по новому логическому стеку и строим новый визуальный стек
        for (i in logicalStack.indices) {
            val route = logicalStack[i]

            // Пытаемся найти уже существующий слой для этого маршрута
            val existing = if (i < activeEntries.size && activeEntries[i].route == route) {
                activeEntries[i]
            } else null

            if (existing != null) {
                // Экран уже нарисован и живет. Просто переносим его в новый список.
                newEntries.add(existing)
            } else {
                // ====================================================================
                // 🔥 ФАЗА 2: РОЖДЕНИЕ НОВОГО ЭКРАНА
                // ====================================================================

                val isRoot = entries.isEmpty() && i == 0
                val shouldSkipEnterAnimation = !enableTransitions || shouldSkipEnterAnimation(
                    route = route,
                    logicalStack = logicalStack,
                    isRoot = isRoot,
                    isRestoring = isRestoring
                )

                // Сразу ставим 0f, если это корень или восстановление стека после поворота.
                // Это убирает мерцание (фликер), так как Animatable инициализируется на финальной позиции.
                val initialOffset = if (shouldSkipEnterAnimation) 0f else screenWidth

                val newEntry = VisualEntry(
                    route = route,
                    id = "entry_${idCounter++}_${route.hashCode()}",
                    initialZIndex = ++zIndexCounter,
                    initialOffset = initialOffset
                )
                entries.add(newEntry)
                newEntries.add(newEntry)

                // Запускаем анимацию появления ТОЛЬКО если это штатный пуш (не корень и не поворот).
                if (!shouldSkipEnterAnimation) {
                    scope.launch {
                        L.i("Test nav", "launch sync")
                        newEntry.offset.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(
                                durationMillis = AvalonTransitions.BASE_DURATION_MS,
                                easing = TelegramEasings.Default
                            )
                        )
                        // Как только экран доехал, переводим его в ACTIVE
                        if (newEntry.state == VisualState.ENTERING) {
                            newEntry.state = VisualState.ACTIVE
                        }
                    }
                }
            }
        }

        // ====================================================================
        // 🔥 ФАЗА 3: УПРАВЛЕНИЕ ПРИЗРАКАМИ (УДАЛЕНИЕ СТАРЫХ ЭКРАНОВ)
        // ====================================================================

        for (old in activeEntries) {
            // Если экран был в старом списке, но его нет в новом -> логика его удалила (pop)
            if (!newEntries.contains(old)) {
                old.state = VisualState.EXITING
                if (enableTransitions) {
                    scope.launch {
                        // Используем динамический releaseDuration, рассчитанный физическим движком.
                        old.offset.animateTo(
                            targetValue = screenWidth,
                            animationSpec = tween(
                                durationMillis = old.releaseDuration,
                                easing = TelegramEasings.EaseOutQuint
                            )
                        )
                        // Удаляем entry из списка отрисовки после завершения анимации
                        entries.remove(old)
                        old.dispose()
                    }
                } else {
                    entries.remove(old)
                    old.dispose()
                }
            }
        }

        // ====================================================================
        // 🔥 ФАЗА 4: РАСПРЕДЕЛЕНИЕ СТАТУСОВ (ОПТИМИЗАЦИЯ РЕНДЕРА)
        // ====================================================================

        val lastIndex = newEntries.lastIndex
        for (i in newEntries.indices) {
            val entry = newEntries[i]

            // Призраки (EXITING) живут своей жизнью до удаления корутиной.
            if (entry.state == VisualState.EXITING) continue

            if (i == lastIndex) {
                // Самый верхний экран всегда ACTIVE (или ENTERING, если он еще едет).
                if (entry.state != VisualState.ENTERING) entry.state = VisualState.ACTIVE
            } else if (i == lastIndex - 1) {
                // Экран прямо под активным. Участвует в эффекте Параллакса.
                entry.state = VisualState.UNDERNEATH
            } else {
                // Все более глубокие экраны оставляем "прогретыми" в композиции,
                // чтобы pop оставался плавным на любой глубине стека.
                entry.state = VisualState.BACKGROUND
            }
        }
    }

    private fun shouldSkipEnterAnimation(
        route: T,
        logicalStack: List<T>,
        isRoot: Boolean,
        isRestoring: Boolean
    ): Boolean {
        if (isRoot || isRestoring) return true

        return logicalStack.size == 1 && route == Route.MainScreen
    }
}
