package com.ensodai.avalon.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import com.ensodai.avalon.utils.L
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

/**
 * Контекст свайпа, предоставляемый локально для каждого экрана.
 * Служит мостом между движком навигации и UI-компонентами экрана.
 * Позволяет внутренним компонентам (например, горизонтальным пейджерам или каруселям)
 * взаимодействовать с жестом закрытия экрана (перехватывать его или отдавать).
 *
 * @param entry Текущий стейт экрана в навигации.
 * @param screenWidth Физическая ширина экрана в пикселях.
 * @param canPop Флаг, указывающий, можно ли закрыть этот экран (есть ли экраны под ним).
 * @param popAction Коллбэк для инициации закрытия с расчетом динамической длительности анимации.
 */
class AvalonSwipeContext(
    val entry: VisualEntry<*>,
    val screenWidth: Float,
    val canPop: Boolean,
    val popAction: (duration: Int) -> Unit
)

/**
 * CompositionLocal для доступа к [AvalonSwipeContext] из любой точки внутри экрана.
 */
val LocalAvalonSwipeContext = staticCompositionLocalOf<AvalonSwipeContext?> { null }

/**
 * 🔥 Единый движок физики для всех видов свайпов (как от края экрана, так и кастомных изнутри).
 *
 * Главная особенность этого движка — "Биомеханическая физика". Он не слепо доверяет
 * VelocityTracker, а анализирует "намерение" пользователя (Intent), компенсируя:
 * 1. "Дугу большого пальца" (Thumb Arc) — естественный откат пальца назад при свайпе одной рукой.
 * 2. "Остановку перед отрывом" (Stop-and-Lift) — микро-зависание пальца перед тем, как отпустить экран.
 *
 * @param screenWidth Ширина экрана в пикселях (нужна для расчета прогресса).
 * @param offsetAnimatable Аниматор, управляющий физическим сдвигом экрана.
 * @param swipeState Состояние жеста (для передачи в UI, например, для отрисовки тени или блокировки).
 * @param determinePass Функция, определяющая фазу перехвата жеста (Initial для диктатуры на краю, Main для вежливости).
 * @param shouldIntercept Предикат, позволяющий отменить перехват свайпа на основе пройденного расстояния.
 * @param onPopRequest Коллбэк успешного свайпа. Передает длительность для бесшовной передачи параллакса.
 */
suspend fun PointerInputScope.handleAvalonGesture(
    screenWidth: () -> Float,
    offsetAnimatable: Animatable<Float, *>,
    swipeState: MutableState<SwipeBackState>,
    determinePass: (downX: Float) -> PointerEventPass,
    shouldIntercept: (totalDragX: Float) -> Boolean,
    onPopRequest: (duration: Int) -> Unit
) {
    coroutineScope {
        // "Зона свободы" — дистанция, которую палец должен пройти до начала движения экрана.
        val touchDelayPx = screenWidth() * (AvalonTransitions.TOUCH_DELAY_DP / 360f)

        awaitEachGesture {
            // ВСЕГДА ловим первое касание в Initial, чтобы получить чистые первоначальные координаты
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            var dragStarted = false
            val velocityTracker = VelocityTracker()

            val downX = down.position.x
            val downY = down.position.y
            var currentDragOffset = offsetAnimatable.value

            // 🔥 Переменные для трекинга биомеханики намерений
            val startTime = down.uptimeMillis
            var lastUptimeMillis = startTime
            // Сохраняем максимальный сдвиг вправо, чтобы детектировать, "передумал" ли юзер
            // или его палец просто изогнулся по дуге.
            var maxDragOffset = currentDragOffset

            // Спрашиваем у вызывающей стороны, в какой фазе следить за жестом
            val trackingPass = determinePass(downX)
            velocityTracker.addPosition(down.uptimeMillis, down.position)

            do {
                val event = awaitPointerEvent(pass = trackingPass)
                val change = event.changes.firstOrNull() ?: break

                // Вежливый режим: если мы в Main и вложенный скролл/пейджер съел жест — уступаем.
                if (trackingPass == PointerEventPass.Main && change.isConsumed) break

                val dragDeltaX = change.positionChange().x
                change.positionChange().y
                velocityTracker.addPosition(change.uptimeMillis, change.position)
                lastUptimeMillis = change.uptimeMillis

                if (!dragStarted) {
                    val totalDragX = change.position.x - downX
                    val totalDragY = change.position.y - downY

                    // Жест считается горизонтальным, если X доминирует над Y с запасом (коэфф 1.5)
                    val isHorizontalDrag = abs(totalDragX) > abs(totalDragY) * 1.5f

                    if (totalDragX >= touchDelayPx) {
                        if (isHorizontalDrag && totalDragX > 0) {
                            if (shouldIntercept(totalDragX)) {
                                dragStarted = true
                                swipeState.value =
                                    swipeState.value.copy(phase = SwipeBackPhase.Dragging)
                                currentDragOffset = offsetAnimatable.value
                                maxDragOffset = currentDragOffset
                                change.consume() // Воруем жест у дочерних элементов!
                            } else {
                                break // Отдаем жест внутреннему UI
                            }
                        } else if (abs(totalDragY) > abs(totalDragX) * 1.5f) {
                            break // Это вертикальный скролл
                        }
                    }
                    continue
                }

                if (swipeState.value.phase == SwipeBackPhase.Completing ||
                    swipeState.value.phase == SwipeBackPhase.SnappingBack
                ) break

                // Экран не может уехать влево дальше 0
                currentDragOffset = (currentDragOffset + dragDeltaX).coerceAtLeast(0f)
                // Трекаем пик сдвига для компенсации "Дуги большого пальца"
                maxDragOffset = max(maxDragOffset, currentDragOffset)

                launch {
                    offsetAnimatable.snapTo(currentDragOffset)
                }
                swipeState.value = swipeState.value.copy(offset = currentDragOffset)
                change.consume() // Потребляем сдвиг, чтобы списки под пальцем не дергались

            } while (change.pressed)

            if (!dragStarted) return@awaitEachGesture

            // ====================================================================
            // 🔥 ПСИХОЛОГИЧЕСКАЯ ФИЗИКА И АНАЛИЗ НАМЕРЕНИЙ (INTENT ANALYSIS)
            // ====================================================================

            // Мгновенная скорость (может быть ложно отрицательной из-за микро-рывка при отрыве)
            val instVelocity = velocityTracker.calculateVelocity().x
            val clampedVelocity = instVelocity.coerceIn(
                -AvalonTransitions.MAX_VELOCITY,
                AvalonTransitions.MAX_VELOCITY
            )
            val progress = currentDragOffset / screenWidth()

            // 1. Общая средняя скорость жеста.
            // Защищает от "Stop-and-Lift": когда юзер делал быстрый жест, но замер на 10мс перед отрывом пальца.
            val totalTimeSeconds = (lastUptimeMillis - startTime) / 1000f
            val overallVelocity =
                if (totalTimeSeconds > 0.02f) currentDragOffset / totalTimeSeconds else 0f

            // 2. Вычисляем "откат" из-за дуги большого пальца.
            val pullbackOffset = maxDragOffset - currentDragOffset
            // Прощаем откат до 15% ширины экрана (~50-60dp). Если меньше — это физиология кисти, а не отмена жеста.
            val maxAllowedPullback = screenWidth() * 0.15f
            val isSmallPullback = pullbackOffset < maxAllowedPullback

            // 3. Логика намерений.
            val isFlickLeft = clampedVelocity <= AvalonTransitions.CANCEL_FLING_VELOCITY
            // Отмена считается ОСОЗНАННОЙ, только если юзер реально утащил экран обратно влево (сильный откат).
            val isIntentionalCancel = isFlickLeft && !isSmallPullback

            // Успешный жест закрытия: либо сильный мгновенный рывок, либо в целом быстрое движение (overall > 800f).
            val isHighVelocityFlick =
                clampedVelocity >= AvalonTransitions.FORWARD_FLING_VELOCITY || overallVelocity >= 800f

            // Если не было резких движений, опираемся на прохождение трешхолда (например, 1/3 экрана).
            val isPastThreshold = progress > AvalonTransitions.SWIPE_THRESHOLD

            // Итоговое решение:
            val shouldPop = when {
                isIntentionalCancel -> false // Резкий и длинный возврат влево
                isHighVelocityFlick -> true  // Уверенный бросок вправо
                else -> isPastThreshold      // Медленно перетянули за половину
            }

            L.i("Test nav", "instVel $instVelocity overallVel $overallVelocity pullback $pullbackOffset cancel $isIntentionalCancel highVel $isHighVelocityFlick shouldPop $shouldPop")

            // ====================================================================
            // 🔥 АНИМАЦИИ
            // ====================================================================

            if (shouldPop) {
                swipeState.value = swipeState.value.copy(phase = SwipeBackPhase.Completing)

                // Продолжаем движение тем же Animatable до самого края,
                // а логический pop выполняем только после завершения.
                val effectiveVelocityForAnim = max(abs(clampedVelocity), overallVelocity)
                    .coerceIn(0f, AvalonTransitions.MAX_VELOCITY)

                val duration = AvalonTransitions.calculateTelegramDuration(
                    currentOffset = currentDragOffset,
                    screenWidth = screenWidth(),
                    velocity = effectiveVelocityForAnim,
                    isPopping = true
                )

                launch {
                    offsetAnimatable.animateTo(
                        targetValue = screenWidth(),
                        animationSpec = tween(
                            durationMillis = duration,
                            easing = TelegramEasings.EaseOutQuint
                        )
                    )
                    swipeState.value = swipeState.value.copy(offset = screenWidth())
                    onPopRequest(0)
                }
            } else {
                swipeState.value = swipeState.value.copy(phase = SwipeBackPhase.SnappingBack)

                // При возврате экрана используем обычную скорость, чтобы не было неестественного рывка.
                val duration = AvalonTransitions.calculateTelegramDuration(
                    currentOffset = currentDragOffset,
                    screenWidth = screenWidth(),
                    velocity = clampedVelocity,
                    isPopping = false
                )

                launch {
                    offsetAnimatable.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = duration,
                            easing = TelegramEasings.Default // Фирменная кривая Телеграма
                        )
                    )
                    swipeState.value = SwipeBackState() // Возвращаем в Idle
                }
            }
        }
    }
}
