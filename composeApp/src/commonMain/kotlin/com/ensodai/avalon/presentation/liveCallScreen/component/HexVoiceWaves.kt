package com.ensodai.avalon.presentation.liveCallScreen.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.theme.AvalonTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// ---------------------------------------------------------------------------
// LiquidNeonHexagon
// ---------------------------------------------------------------------------

/**
 * Деформирующийся жидкий неоновый гексагон.
 *
 * @param userVolume    нормализованная громкость микрофона (0..1)
 * @param aiVolume      нормализованная громкость AI (0..1)
 * @param isAiSpeaking  true когда AI доминирует
 * @param isInterrupted true в фазе INTERRUPTED — AI замолчал и слушает
 * @param isRejected    true когда модель отклонила запрос (красная вспышка)
 */
@Composable
fun LiquidNeonHexagon(
    userVolume: Float,
    aiVolume: Float,
    isAiSpeaking: Boolean,
    isInterrupted: Boolean = false,
    isRejected: Boolean = false,
    rejectTriggerCount: Int = 0
) {
    val colors = AvalonTheme.colors

    // -----------------------------------------------------------------------
    // 1. Двухступенчатое сглаживание громкостей.
    //    Первый spring — быстрый "трекер", убирает шипящие артефакты.
    //    Второй spring — медленный "ленивый" отклик = жидкость.
    // -----------------------------------------------------------------------
    val fastAi by animateFloatAsState(
        targetValue   = aiVolume.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
        label = "fastAi"
    )
    val smoothAi by animateFloatAsState(
        targetValue   = fastAi,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessLow),
        label = "smoothAi"
    )

    val fastUser by animateFloatAsState(
        targetValue   = userVolume.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium),
        label = "fastUser"
    )
    val smoothUser by animateFloatAsState(
        targetValue   = fastUser,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "smoothUser"
    )

    // -----------------------------------------------------------------------
    // 2. Медленное "дыхание" в тишине.
    // -----------------------------------------------------------------------
    val breathe = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        breathe.animateTo(
            targetValue   = 1f,
            animationSpec = infiniteRepeatable(
                animation  = tween(durationMillis = 3200),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    // -----------------------------------------------------------------------
    // 3. Органическое микро-движение граней.
    //    Каждая из 6 граней имеет свой independent "живой" осциллятор
    //    с разными периодами. Это создаёт impression breathing organism,
    //    а не просто синхронного масштабирования.
    // -----------------------------------------------------------------------
    val organicPhases = remember { Array(6) { Animatable(0f) } }
    // Периоды намеренно нечётные (простые числа * базовый период)
    val organicPeriods = remember { intArrayOf(2300, 3100, 2700, 3500, 2100, 2900) }

    LaunchedEffect(Unit) {
        organicPhases.forEachIndexed { i, anim ->
            // kotlinx.coroutines.launch внутри LaunchedEffect не поддерживается напрямую,
            // используем отдельный LaunchedEffect на каждую грань — но это было бы
            // 6 LaunchedEffect. Вместо этого — используем один tween с разным стартовым
            // значением, что даёт рассинхронизацию через RepeatMode.Reverse.
            anim.snapTo((i * 0.17f) % 1f) // начальный offset
            anim.animateTo(
                targetValue   = 1f,
                animationSpec = infiniteRepeatable(
                    animation  = tween(durationMillis = organicPeriods[i]),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    // -----------------------------------------------------------------------
    // 4. Плавный переход цвета между состояниями:
    //    aiBlend:         0 = Cyan (user), 1 = Purple (AI)
    //    interruptBlend:  0 = normal, 1 = Orange (interrupted)
    //    rejectedBlend:   0 = normal, 1 = Red (rejected) — быстрая вспышка
    //    Все — через tween/spring, поэтому цвет никогда не прыгает.
    // -----------------------------------------------------------------------
    val aiBlend by animateFloatAsState(
        targetValue   = if (!isInterrupted && !isRejected && (isAiSpeaking || smoothAi >= smoothUser)) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "aiBlend"
    )
    val interruptBlend by animateFloatAsState(
        targetValue   = if (isInterrupted) 1f else 0f,
        animationSpec = tween(durationMillis = 350),
        label = "interruptBlend"
    )

    // REJECTED: импульс запускается по событию и плавно проходит через текущую форму,
    // без snapTo в красное состояние.
    val rejectedAnim = remember { Animatable(0f) }
    val rejectShakePhase = remember { Animatable(1f) }
    LaunchedEffect(rejectTriggerCount) {
        if (rejectTriggerCount <= 0) return@LaunchedEffect

        rejectedAnim.stop()
        rejectShakePhase.stop()

        rejectShakePhase.snapTo(0f)
        rejectedAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
        )
        rejectShakePhase.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 480, easing = LinearEasing)
        )
        rejectedAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing)
        )
    }
    val rejectedBlend = rejectedAnim.value
    val rejectShakeBlend = 1f - rejectShakePhase.value

    // Цвет: normal → (interrupt) → (rejected override)
    val baseGlowColor = lerp(colors.neonAccent, colors.accentSecondary, aiBlend)
    val afterInterrupt = lerp(baseGlowColor, colors.warningAccent, interruptBlend)
    val glowColor = lerp(afterInterrupt, colors.error, rejectedBlend)

    val baseCoreColor = lerp(colors.textMain, colors.neonAccent, 0.2f + 0.15f * aiBlend)
    val aiCoreColor = lerp(colors.textMain, colors.accentSecondary, 0.24f)
    val interruptedCoreColor = lerp(colors.textMain, colors.warningAccent, 0.2f)
    val rejectedCoreColor = lerp(colors.textMain, colors.error, 0.18f)
    val mixedBaseCore = lerp(baseCoreColor, aiCoreColor, aiBlend)
    val afterInterruptCore = lerp(mixedBaseCore, interruptedCoreColor, interruptBlend)
    val coreColor = lerp(afterInterruptCore, rejectedCoreColor, rejectedBlend)

    // -----------------------------------------------------------------------
    // 5. Амплитуда деформации.
    //    INTERRUPTED: деформация принудительно схлопывается к 0 —
    //    гексагон "замерзает" в правильной форме.
    //    Переход через animateFloatAsState гарантирует плавный морфинг.
    // -----------------------------------------------------------------------
    val isSilent       = maxOf(smoothAi, smoothUser) < 0.05f
    val dominantVolume = maxOf(smoothAi, smoothUser)
    val deformMax      = 0.28f

    // Коэффициент подавления деформации: 0 = нормально, 1 = полная заморозка
    val freezeBlend by animateFloatAsState(
        targetValue   = if (isInterrupted) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "freeze"
    )
    val rejectFreezeBlend by animateFloatAsState(
        targetValue = rejectedBlend * 0.7f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow),
        label = "rejectFreeze"
    )

    // alternating (звезда) — только когда пользователь явно доминирует и не INTERRUPTED
    val alternatingBlend by animateFloatAsState(
        targetValue   = if (!isSilent && !isInterrupted && smoothUser > smoothAi * 1.3f) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "altBlend"
    )
    val rejectCompressBlend by animateFloatAsState(
        targetValue = rejectedBlend,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow),
        label = "rejectCompress"
    )

    // -----------------------------------------------------------------------
    // 6. Яркость свечения.
    //    INTERRUPTED: падает до узкого дыхания 0.20..0.32 —
    //    гексагон "ждёт" в оранжевом полусвете.
    // -----------------------------------------------------------------------
    val interruptedGlow = 0.22f + 0.10f * breathe.value
    val normalGlow      = if (isSilent) 0.20f + 0.08f * breathe.value
                          else 0.25f + 0.75f * dominantVolume
    val glowTarget      = normalGlow + (interruptedGlow - normalGlow) * interruptBlend + 0.22f * rejectedBlend
    val glowBrightness by animateFloatAsState(
        targetValue   = glowTarget,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        label = "glow"
    )

    Canvas(modifier = Modifier.size(180.dp)) {
        val r = size.minDimension / 2f * 0.68f
        val totalFreezeBlend = (freezeBlend + rejectFreezeBlend).coerceIn(0f, 1f)
        val shakeX = 7.dp.toPx() * sin(rejectShakeBlend * PI.toFloat() * 8f) * rejectShakeBlend
        val hexScale = 1f - 0.12f * rejectCompressBlend

        // Деформация каждой грани = основная амплитуда + органическое микро-движение.
        // freezeBlend плавно подавляет всю деформацию при INTERRUPTED → форма "замерзает".
        val faceDeforms = FloatArray(6) { i ->
            val base    = if (isSilent) 0.025f * breathe.value
                          else deformMax * dominantVolume
            val organic = base * 0.15f * (organicPhases[i].value * 2f - 1f)
            val sign    = if (i % 2 != 0) (1f - 2f * alternatingBlend) else 1f
            (base + organic) * sign * (1f - totalFreezeBlend)
        }

        val path = buildLiquidHexPathPerFace(
            cx      = center.x,
            cy      = center.y,
            radius  = r,
            deforms = faceDeforms
        )

        withTransform({
            translate(left = shakeX)
            scale(scaleX = hexScale, scaleY = hexScale, pivot = center)
        }) {
            // Слой 1: Ambient Glow
            drawNeonLayer(
                path     = path,
                color    = glowColor.copy(alpha = (0.28f * glowBrightness).coerceIn(0f, 1f)),
                strokePx = 14.dp.toPx(),
                blurPx   = 18.dp.toPx()
            )

            // Слой 2: Inner Glow
            drawNeonLayer(
                path     = path,
                color    = glowColor.copy(alpha = (0.65f * glowBrightness).coerceIn(0f, 1f)),
                strokePx = 4.dp.toPx(),
                blurPx   = 5.dp.toPx()
            )

            // Слой 3: Core — тонкий яркий провод
            drawPath(
                path  = path,
                color = coreColor.copy(alpha = (0.90f * glowBrightness).coerceIn(0f, 1f)),
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Деформированный гексагон: у каждой из 6 граней — своё смещение.
//
// Алгоритм:
//   • Вычисляем 6 вершин правильного гексагона.
//   • Для каждой грани i: контрольная точка Безье = середина грани,
//     сдвинутая на deforms[i] * radius по вектору нормали грани (из центра).
//   • deforms[i] > 0 → грань выгибается наружу.
//   • deforms[i] < 0 → грань вгибается внутрь.
//   Независимые значения на каждую грань — источник "живой органики".
// ---------------------------------------------------------------------------
private fun buildLiquidHexPathPerFace(
    cx: Float,
    cy: Float,
    radius: Float,
    deforms: FloatArray // размер 6
): Path {
    val startAngle = 0.0  // 0° — плоская сторона снизу
    val vertices   = Array(6) { i ->
        val angle = startAngle + i * PI / 3.0
        Pair(
            (cx + radius * cos(angle)).toFloat(),
            (cy + radius * sin(angle)).toFloat()
        )
    }

    return Path().apply {
        moveTo(vertices[0].first, vertices[0].second)

        for (i in 0 until 6) {
            val next = vertices[(i + 1) % 6]

            val midX = (vertices[i].first  + next.first)  / 2f
            val midY = (vertices[i].second + next.second) / 2f

            val dx  = midX - cx
            val dy  = midY - cy
            val len = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
            val nx  = dx / len
            val ny  = dy / len

            // Контрольная точка Безье со своим смещением для этой грани
            val ctrlX = midX + nx * deforms[i] * radius
            val ctrlY = midY + ny * deforms[i] * radius

            quadraticTo(ctrlX, ctrlY, next.first, next.second)
        }
        close()
    }
}

// ---------------------------------------------------------------------------
// Неоновое свечение контура — multiplatform portable.
//
// Вместо BlurMaskFilter (android-only) рисуем N концентрических stroke-слоёв
// с экспоненциально убывающей прозрачностью и нарастающей толщиной.
// Визуально неотличимо от gaussian blur на контуре: чем дальше от центральной
// линии, тем меньше яркость — именно так работает настоящий неон.
// ---------------------------------------------------------------------------
private fun DrawScope.drawNeonLayer(
    path: Path,
    color: Color,
    strokePx: Float,
    blurPx: Float
) {
    val steps   = 6
    val baseAlpha = color.alpha

    for (i in steps downTo 1) {
        val t       = i.toFloat() / steps          // 1.0 .. 1/steps
        val width   = strokePx + blurPx * (1f - t) // тонкий у центра, толстый снаружи
        val alpha   = baseAlpha * (t * t)           // квадратичное затухание
        drawPath(
            path  = path,
            color = color.copy(alpha = alpha.coerceIn(0f, 1f)),
            style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
