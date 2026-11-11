package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.swipeableMessageBubble

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.pow

@Stable
class SwipeToActionState(
    private val coroutineScope: CoroutineScope,
    val config: SwipeToActionConfig
) {
    // Оптимизация: прямой доступ для чтения без рекомпозиций
    private val _offset = mutableFloatStateOf(0f)
    val offset: Float get() = _offset.floatValue

    private var animationJob: Job? = null

    // === ТВОЯ МАТЕМАТИКА СОПРОТИВЛЕНИЯ (БАЗА) ===
    fun dispatchDragDelta(delta: Float) {
        val currentOffset = _offset.floatValue

        val currentResistance = if (delta < 0) {
            // Движение влево
            if (currentOffset > config.maxSwipeOffset) {
                val linearProgress = (currentOffset / config.maxSwipeOffset).coerceIn(0f, 1f)
                // Твоя формула pow
                val easedProgress = linearProgress.pow(config.resistanceCurvePower)
                // Твой lerp
                lerp(config.resistanceAtStart, config.resistanceAtLimit, easedProgress)
            } else {
                // Оверсвайп
                config.overswipeResistance
            }
        } else {
            // Движение вправо - без сопротивления
            1.0f
        }

        val newOffset = currentOffset + delta * currentResistance
        // Мгновенное обновление (нет ANR, нет лагов)
        _offset.floatValue = newOffset.coerceAtMost(0f)
    }

    // === ТВОЯ ЛОГИКА ЗАВЕРШЕНИЯ (БАЗА) ===
    fun settle(onSwiped: () -> Unit, isActionEnabled: Boolean, stayAtMaxOffset: Boolean) {
        animationJob?.cancel()

        // Определяем, был ли свайп успешным (как в оригинальном коде)
        val isSwiped = isActionEnabled && _offset.floatValue < config.midSwipeThreshold

        // Если успех -> летим в maxSwipeOffset для ВСЕХ режимов (баббл висит оттянутым, пока идет генерация)
        val targetValue = if (isSwiped) config.maxSwipeOffset else 0f

        animationJob = coroutineScope.launch {
            Animatable(_offset.floatValue).animateTo(
                targetValue = targetValue,
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ) {
                _offset.floatValue = value
            }
        }

        // Вызываем колбэк ПОСЛЕ запуска джоба, чтобы если onSwiped вызовет animateReset(),
        // он успешно отменил этот джоб и вернул баббл в 0.
        if (isSwiped) {
            onSwiped()
        }
    }

    // Остановка анимации при касании (чтобы ловить пальцем)
    fun stopAnimation() {
        animationJob?.cancel()
    }

    // Сброс в 0 (для внешнего вызова)
    fun animateReset() {
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            Animatable(_offset.floatValue).animateTo(0f) {
                _offset.floatValue = value
            }
        }
    }

    private fun lerp(start: Float, stop: Float, fraction: Float): Float {
        return (1 - fraction) * start + fraction * stop
    }
}
// БАЗА
//@Stable
//class SwipeToActionState(
//    private val coroutineScope: CoroutineScope,
//    // Теперь класс знает о своей конфигурации
//    val config: SwipeToActionConfig
//) {
//    internal val animatable = Animatable(0f)
//
//    var offset by mutableFloatStateOf(0f)
//        private set
//
//    /**
//     * Обновляет сдвиг во время перетаскивания, применяя логику сопротивления.
//     * ЭТОТ МЕТОД ТЕПЕРЬ ПОЛНЫЙ И РАБОЧИЙ.
//     */
//    fun dispatchDragDelta(delta: Float) {
//        val currentResistance = if (delta < 0) {
//            // Движение влево
//            if (offset > config.swipeLimit) {
//                val linearProgress = (offset / config.swipeLimit).coerceIn(0f, 1f)
//                val easedProgress = linearProgress.pow(config.resistanceCurvePower)
//                // lerp - стандартная функция для линейной интерполяции
//                lerp(config.resistanceAtStart, config.resistanceAtLimit, easedProgress)
//            } else {
//                // Оверсвайп
//                config.overswipeResistance
//            }
//        } else {
//            // Движение вправо - без сопротивления
//            1.0f
//        }
//        val newOffset = offset + delta * currentResistance
//        offset = newOffset.coerceAtMost(0f)
//    }
//
//    /**
//     * Запускает анимацию для возврата или завершения свайпа.
//     * @param isActionEnabled Если false, свайп всегда вернется в исходное состояние.
//     * @param onSwiped Колбэк, который будет вызван, если свайп успешен.
//     */
//    fun settle(isActionEnabled: Boolean, onSwiped: () -> Unit) {
//        // Определяем, был ли свайп успешным, ТОЛЬКО ЕСЛИ действие разрешено
//        val isSwiped = isActionEnabled && offset < config.midSwipeThreshold
//
//        // Если свайп успешен, он анимируется до конца.
//        // В противном случае (включая случай isActionEnabled = false), он вернется в начало.
//        val targetValue = if (isSwiped) config.swipeLimit else 0f
//
//        coroutineScope.launch {
//            animatable.snapTo(offset)
//            animatable.animateTo(targetValue)
//        }
//
//        // Вызываем колбэк только в случае успешного свайпа
//        if (isSwiped) {
//            onSwiped()
//        }
//    }
//
//    /**
//     * Принудительно сбрасывает состояние в 0 без анимации.
//     */
//    fun reset() {
//        offset = 0f
//        coroutineScope.launch {
//            animatable.snapTo(0f)
//        }
//    }
//
//    fun animateReset() {
//        coroutineScope.launch {
//            animatable.snapTo(offset)
//            animatable.animateTo(targetValue = 0f)
//        }
//    }
//
//    // Внутренний метод для синхронизации, который мы будем использовать в "умном" remember
//    internal fun updateOffset(newOffset: Float) {
//        offset = newOffset
//    }
//}