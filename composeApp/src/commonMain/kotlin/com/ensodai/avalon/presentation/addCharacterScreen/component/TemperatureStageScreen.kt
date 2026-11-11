package com.ensodai.avalon.presentation.addCharacterScreen.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.neural_03
import com.ensodai.avalon.generated.resources.neural_03_info
import com.ensodai.avalon.generated.resources.neural_07
import com.ensodai.avalon.generated.resources.neural_07_info
import com.ensodai.avalon.generated.resources.neural_entropy
import com.ensodai.avalon.generated.resources.neural_entropy_info
import com.ensodai.avalon.generated.resources.neural_more_07
import com.ensodai.avalon.generated.resources.neural_more_07_info
import com.ensodai.avalon.theme.AvalonTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun TemperatureStageScreen(
    sliderPosition: Float,
    onChangeTemperature: (Float) -> Unit,
) {
    val colors = AvalonTheme.colors
    val currentColor = entropyTintAt(sliderPosition)

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.align(Alignment.Start),
            text = stringResource(Res.string.neural_entropy),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = colors.textMain
            ),
            textAlign = TextAlign.Start
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            modifier = Modifier.align(Alignment.Start),
            text = stringResource(Res.string.neural_entropy_info),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary
            ),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            InteractiveOscilloscope(
                temperature = sliderPosition,
                activeColor = currentColor,
                onValueChange = { onChangeTemperature(it) }
            )

            Text(
                text = getTemperatureTitle(sliderPosition),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = currentColor,
                    letterSpacing = 2.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = getTemperatureDescription(sliderPosition),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            ),
        )
    }
}

/**
 * InteractiveOscilloscope — Тактильный визуализатор энтропии.
 *
 * @param temperature Текущее значение энтропии (0f..1f).
 * @param activeColor Цвет активной волны (меняется от синего к фиолетовому снаружи).
 * @param onValueChange Колбэк изменения значения (вызывается при таче и драге).
 *
 * Особенности реализации:
 * - Использует кастомный Canvas для отрисовки "живой" волны.
 * - Шум генерируется через Math.random() с кубической интерполяцией силы.
 * - Обрабатывает жесты через awaitEachGesture для мгновенного отклика.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InteractiveOscilloscope(
    modifier: Modifier = Modifier,
    temperature: Float,
    activeColor: Color,
    onValueChange: (Float) -> Unit
) {
    val colors = AvalonTheme.colors
    // Состояние нажатия (для анимаций масштаба и энергии волны)
    var isTouched by remember { mutableStateOf(false) }

    // Haptics: Вибрация для тактильной отдачи
    val haptic = LocalHapticFeedback.current
    // Запоминаем последнее значение, чтобы вибрировать только при сдвиге на 5% (трещотка)
    var lastHapticValue by remember { mutableFloatStateOf(temperature) }

    // Плавная смена цвета при изменении входящего activeColor
    val animatedColor by animateColorAsState(targetValue = activeColor, label = "color")

    // --- 1. АНИМАЦИЯ РАЗМЕРА (ИМПАКТ) ---
    // Карточка "дышит" при нажатии. Используем Spring (пружину) для органичности.
    val scaleY by animateFloatAsState(
        targetValue = if (isTouched) 1.10f else 1f, // +10% высоты при таче
        animationSpec = spring(DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "scaleY"
    )
    val scaleX by animateFloatAsState(
        targetValue = if (isTouched) 1.02f else 1f, // +2% ширины
        animationSpec = spring(DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "scaleX"
    )

    // --- 2. ФИЗИКА ВОЛНЫ ---

    // runningPhase: Текущее смещение волны по X.
    // Используем ручной интегратор вместо infiniteTransition, чтобы избежать скачков при смене скорости.
    var runningPhase by remember { mutableFloatStateOf(0f) }

    // touchEnergy: Множитель энергии.
    // 1.0f в покое, 1.5f при нажатии. Влияет на амплитуду и частоту.
    val touchEnergy by animateFloatAsState(
        targetValue = if (isTouched) 1.5f else 1.0f,
        animationSpec = tween(500, easing = LinearOutSlowInEasing),
        label = "touchEnergy"
    )

    // Игровой цикл (Game Loop): Обновляем фазу каждый кадр
    LaunchedEffect(Unit) {
        val baseSpeed = 0.08f // Базовая скорость прокрутка волны
        while (true) {
            withFrameNanos {
                // Интегрируем скорость: phase = phase + (speed * multiplier)
                runningPhase += baseSpeed * touchEnergy
            }
        }
    }

    // --- 3. UI КОНТЕЙНЕР ---
    Box(
        modifier = modifier
            // graphicsLayer: Масштабирование на GPU (производительность!)
            .graphicsLayer {
                this.scaleX = scaleX
                this.scaleY = scaleY
            }
            // Фон и Форма задаются здесь, чтобы они растягивались вместе с graphicsLayer
            .background(colors.cardBackground, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            // Обработка жестов (Low-level Input)
            .pointerInput(Unit) {
                awaitEachGesture {
                    // Ждем первого касания (Down)
                    val down = awaitFirstDown()
                    isTouched = true

                    // Моментальный прыжок значения к пальцу
                    val newValue = (down.position.x / size.width).coerceIn(0f, 1f)
                    onValueChange(newValue)

                    // Тяжелая вибрация на старт ("Взял рычаг")
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    // Цикл Drag (пока палец не поднят)
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull()
                        if (change != null) {
                            // Рассчитываем новое значение (0..1)
                            val dragValue = (change.position.x / size.width).coerceIn(0f, 1f)
                            onValueChange(dragValue)

                            // Вибрация-трещотка (Ratchet effect) каждые 0.05
                            if (kotlin.math.abs(dragValue - lastHapticValue) > 0.05f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                lastHapticValue = dragValue
                            }
                            change.consume() // Поглощаем ивент
                        }
                    } while (event.changes.any { it.pressed })

                    isTouched = false // Палец поднят
                }
            }
    ) {
        // --- 4. ОТРИСОВКА (CANVAS) ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2

            // A. Фоновая сетка (Grid)
            // Едва заметные линии для ориентации в пространстве
            val tickColor = colors.textMain.copy(alpha = 0.1f)
            // Вертикаль
            drawLine(
                tickColor,
                Offset(width / 2, 10.dp.toPx()),
                Offset(width / 2, height - 10.dp.toPx()),
                1.dp.toPx()
            )
            // Горизонталь
            drawLine(tickColor, Offset(0f, centerY), Offset(width, centerY), 1.dp.toPx())

            // B. Луч Курсора (Scanner Beam)
            val cursorX = width * temperature

            // Луч становится цветным и толстым при нажатии, иначе - тонкий белый "призрак"
            drawLine(
                color = if (isTouched) animatedColor else colors.textMain.copy(alpha = 0.3f),
                start = Offset(cursorX, 0f),
                end = Offset(cursorX, height),
                strokeWidth = if (isTouched) 1.5.dp.toPx() else 1.dp.toPx()
            )

            // C. Генерация Волны (Math Magic)

            // Амплитуда: Зависит от Температуры (30dp) + База (10dp) * Энергию нажатия
            val baseAmplitude = (10.dp.toPx() + (temperature * 30.dp.toPx())) * touchEnergy

            // Частота: Растет с температурой
            val frequency = (1f + (temperature * 3f)) * touchEnergy

            // Фактор Шума (Глитч):
            // Используем куб (t*t*t), чтобы на низких значениях линия была чистой (Premium),
            // а на высоких (Chaos) - жестко ломалась.
            val noiseFactor = (temperature * temperature * temperature * 40.dp.toPx()) +
                    (if (isTouched) 15.dp.toPx() else 0f) // При касании добавляем шум

            val currentPhase = runningPhase

            val path = Path()
            path.moveTo(0f, centerY)
            val step = 5 // Шаг отрисовки (меньше = плавнее, но дороже)

            for (x in 0..width.toInt() step step) {
                val normalizedX = x / width

                // Envelope (Огибающая): Sin-функция, чтобы волна затухала к краям экрана (0 -> 1 -> 0)
                val envelope = sin(normalizedX * PI).toFloat()

                // 1. Гладкая основа (Синусоида)
                val smoothWave = sin((normalizedX * 6f * frequency) + currentPhase) * baseAmplitude

                // 2. Рваный шум (Jagged Noise) - создает эффект "электричества"
                val jaggedNoise = (Random.nextFloat() - 0.5f) * noiseFactor

                // Складываем сигналы
                val y = centerY + ((smoothWave + jaggedNoise) * envelope)
                path.lineTo(x.toFloat(), y)
            }

            // Отрисовка: Glow (Свечение) - широкая полупрозрачная линия
            drawPath(
                path,
                animatedColor.copy(alpha = 0.6f),
                style = Stroke(8.dp.toPx(), cap = StrokeCap.Round)
            )
            // Отрисовка: Core (Ядро) - тонкая белая линия
            drawPath(
                path,
                colors.textMain.copy(alpha = 0.9f),
                style = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
            )

            // D. Индикаторы-треугольники (Overlay UI)
            // Визуальные "захваты", показывающие, что это слайдер
            val triangleSize = 8.dp.toPx()
            val indicatorColor = colors.textMain.copy(alpha = if (isTouched) 1f else 0.5f)

            // Верхний треугольник (V)
            val topTriangle = Path().apply {
                moveTo(cursorX - triangleSize, 0f)
                lineTo(cursorX + triangleSize, 0f)
                lineTo(cursorX, triangleSize)
                close()
            }
            drawPath(topTriangle, indicatorColor)

            // Нижний треугольник (^)
            val bottomTriangle = Path().apply {
                moveTo(cursorX - triangleSize, height)
                lineTo(cursorX + triangleSize, height)
                lineTo(cursorX, height - triangleSize)
                close()
            }
            drawPath(bottomTriangle, indicatorColor)
        }
    }
}

/**
 * Умная функция смешивания цветов.
 * 0.0 -> Синий
 * 0.5 -> Циан
 * 1.0 -> Фиолетовый
 */
@Composable
fun entropyTintAt(position: Float): Color {
    val colors = AvalonTheme.colors
    return when {
        position <= 0.5f -> {
            // Lerp между Logic и Balance
            val fraction = position / 0.5f
            lerp(colors.infoAccent, colors.neonAccent, fraction)
        }

        else -> {
            // Lerp между Balance и Chaos
            val fraction = (position - 0.5f) / 0.5f
            lerp(colors.neonAccent, colors.accentSecondary, fraction)
        }
    }
}

/**
 * Кастомный слайдер в стиле Techno.
 */
@Composable
fun TechnoSlider(
    value: Float,
    onValueChange: (Float) -> Unit
) {
    val colors = AvalonTheme.colors
    val haptic = LocalHapticFeedback.current
    // Чтобы вибрация не срабатывала слишком часто, запоминаем прошлый шаг
    var lastHapticValue by remember { mutableFloatStateOf(value) }

    Slider(
        value = value,
        onValueChange = {
            onValueChange(it)
            // Вибрация каждые 0.1 шага
            if (kotlin.math.abs(it - lastHapticValue) > 0.1f) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                lastHapticValue = it
            }
        },
        valueRange = 0f..1f,
        colors = SliderColors(
            thumbColor = colors.textMain,
            activeTrackColor = colors.textMain,
            activeTickColor = Color.Transparent,
            inactiveTrackColor = colors.neutralButton,
            inactiveTickColor = Color.Transparent,
            disabledThumbColor = colors.textSecondary,
            disabledActiveTrackColor = colors.textSecondary.copy(alpha = 0.5f),
            disabledActiveTickColor = Color.Transparent,
            disabledInactiveTrackColor = colors.surfaceHigh,
            disabledInactiveTickColor = Color.Transparent
        ),
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
fun getTemperatureTitle(value: Float): String {
    return when {
        value < 0.3f -> stringResource(Res.string.neural_03)
        value < 0.7f -> stringResource(Res.string.neural_07)
        else -> stringResource(Res.string.neural_more_07)
    }
}

@Composable
fun getTemperatureDescription(value: Float): String {
    return when {
        value < 0.3f -> stringResource(Res.string.neural_03_info)
        value < 0.7f -> stringResource(Res.string.neural_07_info)
        else -> stringResource(Res.string.neural_more_07_info)
    }
}
