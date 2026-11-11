package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.shared.avalon.gifts.models.GiftEffectType
import kotlin.math.*
import kotlin.random.Random

// ─── Цвета ───────────────────────────────────────────────────────────────────

private val BURST_COLORS = listOf(
    Color(0xFFFFCC3C),
    Color(0xFF50C8FF),
    Color(0xFFC850FF),
    Color(0xFF50FFA0),
    Color(0xFFFF648C),
)
private val colors = BURST_COLORS.shuffled()

// ─── Модели ───────────────────────────────────────────────────────────────────

private data class TrailPoint(val x: Float, val y: Float)

private class Rocket(
    val sx: Float, val sy: Float,
    val tx: Float, val ty: Float,
    val color: Color,
    val delayMs: Long,
    val durationMs: Long,
    val driftAmplitude: Float,
    val driftFrequency: Float,
) {
    var elapsed = 0L
    var exploded = false
    val trail = ArrayDeque<TrailPoint>(24)
    var x = sx; var y = sy
}

private class Spark(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    val tailLength: Float,
    val lifeMs: Long,
    var elapsed: Long = 0L,
    val bright: Boolean,
)

private class Ring(
    val maxRadius: Float,
    val speed: Float,
    val peakAlpha: Float,
    val delayMs: Long,
    var radius: Float = 0f,
)

private class Burst(
    val x: Float, val y: Float,
    val color: Color,
    val sparks: List<Spark>,
    val rings: List<Ring>,
    val lifeMs: Long = 1500L,
    var elapsed: Long = 0L,
)

// ─── Снимок для Canvas (только immutable данные) ──────────────────────────────

private data class RocketSnapshot(
    val x: Float, val y: Float,
    val trail: List<TrailPoint>,
    val color: Color,
    val raw: Float,
)

private data class BurstSnapshot(
    val x: Float, val y: Float,
    val color: Color,
    val elapsed: Long,
    val lifeMs: Long,
    val rings: List<RingSnapshot>,
    val sparks: List<SparkSnapshot>,
)

private data class RingSnapshot(
    val radius: Float, val maxRadius: Float,
    val peakAlpha: Float, val delayMs: Long,
    val elapsed: Long,
)

private data class SparkSnapshot(
    val x: Float, val y: Float,
    val vx: Float, val vy: Float,
    val tailLength: Float,
    val elapsed: Long, val lifeMs: Long,
    val bright: Boolean,
)

private data class FrameSnapshot(
    val bgAlpha: Float,
    val rockets: List<RocketSnapshot>,
    val bursts: List<BurstSnapshot>,
)

// ─── Easing ───────────────────────────────────────────────────────────────────

private fun rocketEase(t: Float): Float =
    if (t < 0.7f) (t / 0.7f) * 0.85f
    else 0.85f + ((t - 0.7f) / 0.3f) * 0.15f

// ─── Фабрики ──────────────────────────────────────────────────────────────────

private fun createRocket(
    w: Float, h: Float,
    delayMs: Long,
    txRatio: Float, tyRatio: Float,
    color: Color,
): Rocket = Rocket(
    sx = w * (0.35f + Random.nextFloat() * 0.30f),
    sy = h + 8f,
    tx = w * txRatio,
    ty = h * tyRatio,
    color = color,
    delayMs = delayMs,
    durationMs = 480L + Random.nextLong(0L, 120L), // фикс: nextLong вместо nextInt()*100
    driftAmplitude = (Random.nextFloat() - 0.5f) * 16f,
    driftFrequency = 2.5f + Random.nextFloat() * 1.5f,
)

private fun createBurst(x: Float, y: Float, color: Color): Burst {
    val sparks = buildList(90) {
        repeat(90) { i ->
            val angle = (i / 90f) * 2f * PI.toFloat() + (Random.nextFloat() - 0.5f) * 0.2f
            val speed = 0.5f + Random.nextFloat() * 1.6f
            add(Spark(
                x = x, y = y,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed,
                tailLength = 8f + Random.nextFloat() * 24f,
                lifeMs = 650L + Random.nextLong(0L, 750L), // фикс: nextLong
                bright = Random.nextFloat() > 0.55f,
            ))
        }
    }
    return Burst(
        x = x, y = y, color = color, sparks = sparks,
        rings = listOf(
            Ring(maxRadius = 52f,  speed = 0.19f, peakAlpha = 0.80f, delayMs = 0L),
            Ring(maxRadius = 88f,  speed = 0.14f, peakAlpha = 0.45f, delayMs = 55L),
            Ring(maxRadius = 125f, speed = 0.10f, peakAlpha = 0.25f, delayMs = 120L),
        )
    )
}

// ─── Composable ───────────────────────────────────────────────────────────────

@Composable
fun GiftEffectOverlay(
    effectType: GiftEffectType,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (effectType != GiftEffectType.FIREWORKS) return

    var canvasWidth by remember { mutableStateOf(0f) }
    var canvasHeight by remember { mutableStateOf(0f) }

    // Снимок передаётся в Canvas — он State, поэтому Canvas перерисовывается
    var frame by remember { mutableStateOf(FrameSnapshot(0f, emptyList(), emptyList())) }

    val haptic = LocalHapticFeedback.current

    LaunchedEffect(effectType, canvasWidth, canvasHeight) {
        if (canvasWidth == 0f || canvasHeight == 0f) return@LaunchedEffect

        val rockets = mutableListOf<Rocket>()
        val bursts = mutableListOf<Burst>()
        var bgAlpha = 0f
        var bgTarget = 0.2f

        rockets += createRocket(canvasWidth, canvasHeight, 0L,
            0.45f + Random.nextFloat() * 0.10f,
            0.45f + Random.nextFloat() * 0.05f,  // средне-низко
            colors[0])

        // ......бум — пауза подольше, выше
        rockets += createRocket(canvasWidth, canvasHeight, 550L,
            0.30f + Random.nextFloat() * 0.15f,
            0.30f + Random.nextFloat() * 0.05f,  // высоко
            colors[1])

        // ...бум — короткая пауза
        rockets += createRocket(canvasWidth, canvasHeight, 900L,
            0.62f + Random.nextFloat() * 0.12f,
            0.25f + Random.nextFloat() * 0.05f,  // чуть ниже второй
            colors[2])

        // бум — финал почти сразу
        rockets += createRocket(canvasWidth, canvasHeight, 1100L,
            0.40f + Random.nextFloat() * 0.12f,
            0.20f + Random.nextFloat() * 0.04f,  // самая высокая
            colors[3])

        var lastMs = withFrameMillis { it }

        while (true) {
            val now = withFrameMillis { it }
            val dt = (now - lastMs).coerceAtMost(48L)
            lastMs = now

            bgAlpha += (bgTarget - bgAlpha) * 0.07f
//            bgAlpha += bgAlpha * 0.07f

            // Ракеты
            val rocketIter = rockets.iterator()
            while (rocketIter.hasNext()) {
                val r = rocketIter.next()
                r.elapsed += dt
                if (r.elapsed < r.delayMs) continue

                val raw = (r.elapsed - r.delayMs).toFloat() / r.durationMs.toFloat()

                if (raw >= 1f && !r.exploded) {
                    r.exploded = true
                    bursts += createBurst(r.tx, r.ty, r.color)
                    haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                    // гасим фон только после последней ракеты
                    if (rockets.count { !it.exploded } <= 1) bgTarget = 0f
                    rocketIter.remove()
                    continue
                }

                if (!r.exploded) {
                    val t = rocketEase(raw).coerceAtMost(1f)
                    val driftDecay = 1f - raw.pow(2f)
                    r.x = r.sx + (r.tx - r.sx) * t +
                            sin(raw * PI.toFloat() * r.driftFrequency) * r.driftAmplitude * driftDecay
                    r.y = r.sy + (r.ty - r.sy) * t
                    r.trail.add(TrailPoint(r.x, r.y))
                    if (r.trail.size > 20) r.trail.removeAt(0)
                }
            }

            // Взрывы
            val burstIter = bursts.iterator()
            while (burstIter.hasNext()) {
                val b = burstIter.next()
                b.elapsed += dt
                if (b.elapsed >= b.lifeMs) { burstIter.remove(); continue }
                for (rw in b.rings) {
                    if (b.elapsed >= rw.delayMs) rw.radius += rw.speed * dt
                }
                for (s in b.sparks) {
                    s.elapsed += dt
                    if (s.elapsed >= s.lifeMs) continue
                    s.vy += 0.00052f * dt
                    s.vx *= 0.983f; s.vy *= 0.983f
                    s.x += s.vx * dt * 0.54f
                    s.y += s.vy * dt * 0.54f
                }
            }

            // Публикуем снимок → тригерит перерисовку Canvas
            frame = FrameSnapshot(
                bgAlpha = bgAlpha,
                rockets = rockets
                    .filter { !it.exploded && it.elapsed >= it.delayMs }
                    .map { r ->
                        val raw = (r.elapsed - r.delayMs).toFloat() / r.durationMs
                        RocketSnapshot(r.x, r.y, r.trail.toList(), r.color, raw)
                    },
                bursts = bursts.map { b ->
                    BurstSnapshot(
                        x = b.x, y = b.y, color = b.color,
                        elapsed = b.elapsed, lifeMs = b.lifeMs,
                        rings = b.rings.map { rw ->
                            RingSnapshot(rw.radius, rw.maxRadius, rw.peakAlpha, rw.delayMs, b.elapsed)
                        },
                        sparks = b.sparks
                            .filter { it.elapsed < it.lifeMs }
                            .map { s ->
                                SparkSnapshot(s.x, s.y, s.vx, s.vy, s.tailLength, s.elapsed, s.lifeMs, s.bright)
                            },
                    )
                },
            )

            val anyAlive = rockets.isNotEmpty() || bursts.isNotEmpty()
            if (!anyAlive && bgAlpha < 0.005f) break
        }

        frame = FrameSnapshot(0f, emptyList(), emptyList())
        onAnimationComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                onClick = onAnimationComplete,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            )
            .onSizeChanged {
                canvasWidth = it.width.toFloat()
                canvasHeight = it.height.toFloat()
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val f = frame

            if (f.bgAlpha > 0.004f) {
                drawRect(color = Color.Black.copy(alpha = f.bgAlpha))
            }

            for (r in f.rockets) drawRocketSnapshot(r)
            for (b in f.bursts) drawBurstSnapshot(b)
        }
    }
}

// ─── Отрисовка ────────────────────────────────────────────────────────────────

private fun DrawScope.drawRocketSnapshot(r: RocketSnapshot) {
    if (r.trail.size < 2) return
    val speedBoost = 1f - r.raw * 0.5f
    for (i in 1 until r.trail.size) {
        val tr = i.toFloat() / r.trail.size
        drawLine(
            color = r.color.copy(alpha = tr * 0.72f),
            start = Offset(r.trail[i - 1].x, r.trail[i - 1].y),
            end = Offset(r.trail[i].x, r.trail[i].y),
            strokeWidth = (tr * 2.6f * speedBoost).coerceAtLeast(0.5f),
            cap = StrokeCap.Round,
        )
    }
    val hi = 0.55f + (1f - r.raw) * 0.45f
    drawCircle(color = r.color.copy(alpha = hi * 0.7f), radius = 6f, center = Offset(r.x, r.y))
    drawCircle(color = Color.White.copy(alpha = hi), radius = 2.5f, center = Offset(r.x, r.y))
}

private fun DrawScope.drawBurstSnapshot(b: BurstSnapshot) {
    val center = Offset(b.x, b.y)

    // Кольца
    for (rw in b.rings) {
        if (rw.elapsed < rw.delayMs || rw.radius <= 0f) continue
        val fade = (1f - rw.radius / rw.maxRadius).coerceIn(0f, 1f)
        if (fade <= 0f) continue
        drawCircle(
            color = b.color.copy(alpha = rw.peakAlpha * fade * 0.6f),
            radius = rw.radius, center = center,
            style = Stroke(width = (10f * fade + 2f).coerceAtLeast(1f)),
        )
        drawCircle(
            color = Color.White.copy(alpha = rw.peakAlpha * fade * 0.2f),
            radius = rw.radius, center = center,
            style = Stroke(width = 2f * fade),
        )
    }

    // Вспышка
    val flashT = (b.elapsed.toFloat() / 150f).coerceIn(0f, 1f)
    if (flashT < 1f) {
        val fA = (1f - flashT) * 0.88f
        val fR = flashT * 52f + 5f
        drawCircle(color = Color.White.copy(alpha = fA), radius = fR * 0.4f, center = center)
        drawCircle(color = b.color.copy(alpha = fA * 0.55f), radius = fR, center = center)
    }

    // Искры
    for (s in b.sparks) {
        val sr = s.elapsed.toFloat() / s.lifeMs
        if (sr >= 1f) continue
        val fade = (1f - sr).pow(1.35f)
        drawLine(
            color = b.color.copy(alpha = fade * if (s.bright) 0.88f else 0.55f),
            start = Offset(s.x - s.vx * s.tailLength * 0.32f, s.y - s.vy * s.tailLength * 0.32f),
            end = Offset(s.x, s.y),
            strokeWidth = if (s.bright) 1.5f * (1f - sr * 0.45f) else 0.9f,
            cap = StrokeCap.Round,
        )
        if (sr < 0.3f && s.bright) {
            drawCircle(
                color = Color.White.copy(alpha = fade * 0.75f),
                radius = 1.6f * (1f - sr),
                center = Offset(s.x, s.y),
            )
        }
    }
}
