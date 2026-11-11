package com.ensodai.avalon.presentation.chatScreen.component.explosionEffect

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import kotlin.math.pow
import kotlin.random.Random

class Particle(
    val config: ExplosionConfig,
    private val textLayout: TextLayoutResult
) {
    private val maxV = randomInRange(config.verticalRange)
    private val maxH = randomInRange(config.horizontalRange)
    private val velocity = 4f * maxV
    private val acceleration = -2f * velocity

    private var dx = 0f
    private var dy = 0f
    private var alpha = 0f
    private var scale = 1f
    private var rotation = 0f

    private val startScale = randomInRange(config.startScaleRange)
    private val endScale = config.endScale
    private val rotationSpeed = randomInRange(config.rotationSpeedRange)
    private val startDelay = randomInRange(config.startDelayRange)
    private val initialX = randomInRange(-10f, 10f)
    private val initialY = randomInRange(-10f, 10f)
    private val fadeInPct = config.fadeInPct
    private val fadeOutStart = config.fadeOutStartPct

    // Берем импульс из конфига
    private val impulse = config.initialImpulse

    fun update(progress: Float) {
        // Линейный прогресс (реальное время 0..1)
        val linearProgress = (progress - startDelay).coerceIn(0f, 1f)

        if (progress < startDelay) {
            alpha = 0f
            return
        }

        // === 1. ИСКАЖЕНИЕ ВРЕМЕНИ (IMPULSE) ===
        val physicsProgress = linearProgress.pow(1f / impulse)

        // === 2. ФИЗИКА (ДВИЖЕНИЕ) ===
        val t = physicsProgress * 1.4f
        val verticalDisplacement = (t * velocity + 0.5 * acceleration * t.pow(2)).toFloat()

        dx = initialX + (maxH * physicsProgress)
        dy = initialY - verticalDisplacement

        // === 3. РАЗМЕР (SCALE) ===
        scale = startScale + (endScale - startScale) * physicsProgress

        // === 4. ПРОЗРАЧНОСТЬ (ALPHA) ===
        alpha = when {
            linearProgress < fadeInPct -> linearProgress / fadeInPct
            linearProgress > fadeOutStart -> {
                val fade = (linearProgress - fadeOutStart) / (1f - fadeOutStart)
                1f - fade
            }

            else -> 1f
        }

        rotation += rotationSpeed
    }

    // ИЗМЕНЕНИЕ: Убрали аргумент textLayout, используем this.textLayout
    fun draw(drawScope: DrawScope, origin: Offset) {
        if (alpha <= 0f || scale <= 0f) return

        val center = Offset(origin.x + dx, origin.y + dy)
        val topLeft = Offset(
            x = center.x - textLayout.size.width / 2f,
            y = center.y - textLayout.size.height / 2f
        )

        drawScope.withTransform({
            rotate(degrees = rotation, pivot = center)
            scale(scaleX = scale, scaleY = scale, pivot = center)
        }) {
            drawText(
                textLayoutResult = textLayout,
                topLeft = topLeft,
                alpha = alpha
            )
        }
    }

    companion object {
        private val random = Random.Default
        fun randomInRange(range: ClosedFloatingPointRange<Float>) =
            range.start + random.nextFloat() * (range.endInclusive - range.start)

        fun randomInRange(min: Float, max: Float) = min + random.nextFloat() * (max - min)
    }
}
