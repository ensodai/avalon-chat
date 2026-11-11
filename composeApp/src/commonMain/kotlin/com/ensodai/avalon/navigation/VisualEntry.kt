package com.ensodai.avalon.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation3.runtime.NavKey

/**
 * 🔥 Ядро визуального состояния одного экрана в навигационной иерархии.
 *
 * Аннотация `@Stable` критически важна: она говорит компилятору Compose, что
 * инстанс этого класса не изменится (ссылка останется той же), а значит Compose
 * может смело пропускать рекомпозицию хоста, даже если внутри меняются `offset` или `state`.
 * Вся магия параллакса и анимаций работает в фазе Draw, минуя перестройку дерева UI.
 *
 * @param route Логический ключ (роут) экрана.
 * @param id Уникальный строковый идентификатор (для SaveableStateHolder).
 * @param initialZIndex Начальный Z-индекс (чем больше, тем выше экран).
 * @param initialOffset Стартовый сдвиг (0f для корня, `screenWidth` для выезжающих).
 */
@Stable
class VisualEntry<T : NavKey>(
    val route: T,
    val id: String,
    initialZIndex: Float,
    initialOffset: Float = 0f
) : LifecycleOwner, ViewModelStoreOwner {

    // 1. Создаем реестр жизненного цикла
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    // 2. Создаем хранилище для Koin ViewModel
    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store

    // 3. Умный стейт: при смене визуального состояния меняем системный Lifecycle
    private var _state by mutableStateOf(if (initialOffset == 0f) VisualState.ACTIVE else VisualState.ENTERING)
    var state: VisualState
        get() = _state
        set(value) {
            if (_state != value) {
                _state = value
                updateLifecycle(value)
            }
        }

    var zIndex by mutableFloatStateOf(initialZIndex)

    /**
     * Главный движитель экрана.
     * Animatable работает на уровне корутин и обновляется с частотой обновления экрана (60-120fps).
     */
    val offset = Animatable(initialOffset)

    /**
     * Динамически рассчитанная длительность "смертельной" анимации.
     * Передается из движка жестов, чтобы гарантировать идеальный, непрерывный импульс.
     */
    var releaseDuration = AvalonTransitions.BASE_DURATION_MS

    /** Состояние свайпа для передачи в дочерние UI-компоненты. */
    val swipeState = mutableStateOf(SwipeBackState())

    /**
     * Флаг блокировки. Если `false`, этот экран невозможно стянуть пальцем.
     * Управляется изнутри экрана через [AvalonSwipeLockEffect].
     */
    var isSwipeEnabled by mutableStateOf(true)

    init {
        // Задаем стартовый статус при рождении
        updateLifecycle(_state)
    }

    private fun updateLifecycle(visualState: VisualState) {
        val targetLifecycle = when (visualState) {
            // Экран полностью наверху и активен
            VisualState.ACTIVE -> Lifecycle.State.RESUMED

            // Экран едет, либо находится прямо под нами (участвует в параллаксе),
            // либо улетает (Призрак). Он должен рисоваться и получать данные!
            VisualState.ENTERING,
            VisualState.UNDERNEATH,
            VisualState.EXITING -> Lifecycle.State.STARTED

            // Глубинный экран остается в дереве, чтобы pop на любой глубине
            // не приводил к внезапному появлению слоя посреди анимации.
            VisualState.BACKGROUND -> Lifecycle.State.CREATED

            // Экран глубоко в стеке, невидим. Ставим на паузу все collectAsStateWithLifecycle.
            VisualState.STASHED -> Lifecycle.State.CREATED
        }
        lifecycleRegistry.currentState = targetLifecycle
    }

    /**
     * 🔥 Сборка мусора и отмена корутин.
     * Вызывается ТОЛЬКО когда экран полностью улетел за границу (анимация закрытия завершена)
     * и удален из массива отрисовки менеджером состояний.
     */
    fun dispose() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        store.clear() // Убивает ViewModel и вызывает onCleared()
    }
}
