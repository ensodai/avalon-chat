package com.ensodai.avalon.presentation.commonComponents

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Рисует правильный гексагон в центре DrawScope.
 * angle = 0° — плоская сторона снизу (flat-top).
 */
fun DrawScope.drawHexagonPath(
    color: Color,
    style: androidx.compose.ui.graphics.drawscope.DrawStyle = Stroke()
) {
    val center = this.center
    val strokeWidth = (style as? Stroke)?.width ?: 0f
    val radius = (size.minDimension - strokeWidth) / 2

    val path = Path().apply {
        var angle = 0.0
        moveTo(
            center.x + (radius * cos(angle)).toFloat(),
            center.y + (radius * sin(angle)).toFloat()
        )
        for (i in 1..6) {
            angle += PI / 3
            lineTo(
                center.x + (radius * cos(angle)).toFloat(),
                center.y + (radius * sin(angle)).toFloat()
            )
        }
        close()
    }
    drawPath(path = path, color = color, style = style)
}
