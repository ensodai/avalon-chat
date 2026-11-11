package com.ensodai.avalon.presentation.commonComponents.cardMonolith

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun HexGridPattern(
    color: Color,
    modifier: Modifier = Modifier,
    hexRadius: Float = 28f,
    strokeWidth: Float = 0.8f
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val hexWidth = hexRadius * sqrt(3f)
        val hexHeight = hexRadius * 2f
        val horizSpacing = hexWidth
        val vertSpacing = hexHeight * 0.75f

        var rowIndex = 0
        var y = 0f
        while (y < height + hexHeight) {
            val xOffset = if (rowIndex % 2 == 1) horizSpacing * 0.5f else 0f
            var x = -horizSpacing + xOffset
            while (x < width + horizSpacing) {
                val path = Path().apply {
                    var angle = 0.0 // flat-top hex
                    moveTo(
                        (x + hexRadius * cos(angle)).toFloat(),
                        (y + hexRadius * sin(angle)).toFloat()
                    )
                    for (i in 1..6) {
                        angle += PI / 3.0
                        lineTo(
                            (x + hexRadius * cos(angle)).toFloat(),
                            (y + hexRadius * sin(angle)).toFloat()
                        )
                    }
                    close()
                }
                drawPath(path, color = color, style = Stroke(width = strokeWidth))
                x += horizSpacing
            }
            y += vertSpacing
            rowIndex++
        }
    }
}
