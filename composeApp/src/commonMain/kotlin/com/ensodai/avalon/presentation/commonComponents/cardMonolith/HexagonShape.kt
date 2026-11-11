package com.ensodai.avalon.presentation.commonComponents.cardMonolith

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object HexagonShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val radius = size.minDimension / 2f
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        val path = Path().apply {
            var angle = 0.0 // лежит на горизонтальном ребре (flat-top)
            moveTo(
                (centerX + radius * cos(angle)).toFloat(),
                (centerY + radius * sin(angle)).toFloat()
            )
            for (i in 1..6) {
                angle += PI / 3.0
                lineTo(
                    (centerX + radius * cos(angle)).toFloat(),
                    (centerY + radius * sin(angle)).toFloat()
                )
            }
            close()
        }
        return Outline.Generic(path)
    }
}
