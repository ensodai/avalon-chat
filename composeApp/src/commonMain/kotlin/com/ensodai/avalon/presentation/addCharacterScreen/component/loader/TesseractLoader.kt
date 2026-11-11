package com.ensodai.avalon.presentation.addCharacterScreen.component.loader

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.utils.randomProbability
import kotlinx.coroutines.delay

@Composable
fun TesseractLoader(
    modifier: Modifier = Modifier,
    primaryColor: Color = AvalonTheme.colors.neonAccent,
    secondaryColor: Color = AvalonTheme.colors.textMain,
    glitchText: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(100.dp)
        ) {
            RotatingSquares(primaryColor, secondaryColor)
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (glitchText) {
            GlitchText("SYNTHESIZING CHARACTER")
        }
    }
}

@Composable
fun RotatingSquares(
    color1: Color, color2: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tesseract")

    val angle1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "angle1"
    )

    val angle2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -180f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "angle2"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val sizePx = size.minDimension
        val stroke = 3.dp.toPx()
        val corner = 8.dp.toPx()

        val center = Offset(size.width / 2, size.height / 2)

        val squareSize = Size(sizePx * 0.6f * scale, sizePx * 0.6f * scale)
        val topLeft = Offset(center.x - squareSize.width / 2, center.y - squareSize.height / 2)

        // Квадрат 1 (Cyan)
        rotate(angle1) {
            drawRoundRect(
                color = color1.copy(alpha = 0.8f),
                topLeft = topLeft,
                size = squareSize,
                cornerRadius = CornerRadius(corner, corner),
                style = Stroke(width = stroke)
            )
        }

        // Квадрат 2 (White) - чуть меньше и внутри
        rotate(angle2) {
            drawRoundRect(
                color = color2.copy(alpha = 0.6f),
                topLeft = topLeft, // Используем те же координаты, но другой угол
                size = squareSize,
                cornerRadius = CornerRadius(corner, corner),
                style = Stroke(width = stroke) // Можно сделать тоньше: stroke * 0.5f
            )
        }
    }
}

@Composable
fun GlitchText(text: String) {
    val colors = AvalonTheme.colors
    // Эффект "подбора символов"
    val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_#<>"
    var displayText by remember { mutableStateOf(text) }

    LaunchedEffect(text) {
        // Каждые 100мс меняем случайные буквы, постепенно "стабилизируя" текст
        // Но для простого лоадера можно просто менять 1-2 символа рандомно
        while (true) {
            val glitchCount = 1 // Сколько символов глитчит
            val builder = StringBuilder(text)
            repeat(glitchCount) {
                val index = (text.indices).random()
                if (text[index] != ' ') { // Пробелы не трогаем
                    builder[index] = characters.random()
                }
            }
            displayText = builder.toString()
            delay(80) // Скорость глитча

            // Иногда показываем чистый текст на мгновение
            if (randomProbability() > 0.7) {
                displayText = text
                delay(300)
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = displayText,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMain,
                letterSpacing = 3.sp
            )
        )

        // Маленькая подпись снизу
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "AI CORE ACTIVE",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = colors.textSecondary,
                letterSpacing = 1.sp
            )
        )
    }
}
