package com.ensodai.avalon.presentation.addCharacterScreen.component.loader

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.theme.AvalonTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun NeuralFabricationLoader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(), // Занимает весь экран
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. АНИМИРОВАННОЕ ЯДРО (Cyber-Spinner)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            CyberRings()

            // Процент загрузки внутри кольца
            HackerPercentageCounter()
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 2. СИСТЕМНЫЙ ЛОГ (Пишущая машинка)
        SystemLogMessages()
    }
}

@Composable
fun CyberRings() {
    val colors = AvalonTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "rings")

    // Вращение внешнего кольца (медленно)
    val angle1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "ring1"
    )

    // Вращение внутреннего кольца (быстро, в другую сторону)
    val angle2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing)
        ),
        label = "ring2"
    )

    // Пульсация прозрачности (Glow effect)
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 4.dp.toPx()
        val radius = size.minDimension / 2 - strokeWidth

        // Внешнее кольцо (состоит из 3 сегментов)
        rotate(angle1) {
            drawArc(
                color = colors.neonAccent.copy(alpha = alpha),
                startAngle = 0f,
                sweepAngle = 200f, // Неполный круг
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)
            )
            // Декоративный штрих напротив
            drawArc(
                color = colors.neonAccent.copy(alpha = 0.3f),
                startAngle = 220f,
                sweepAngle = 40f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)
            )
        }

        // Внутреннее кольцо (поменьше, крутится быстрее)
        val innerRadius = radius * 0.7f
        rotate(angle2) {
            drawArc(
                color = colors.textMain.copy(alpha = 0.8f),
                startAngle = 0f,
                sweepAngle = 280f,
                useCenter = false,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Butt), // Тонкое
                size = Size(innerRadius * 2, innerRadius * 2),
                topLeft = Offset(
                    (size.width - innerRadius * 2) / 2,
                    (size.height - innerRadius * 2) / 2
                )
            )
        }
    }
}

@Composable
fun HackerPercentageCounter() {
    val colors = AvalonTheme.colors
    // Имитация загрузки от 0 до 100
    // Animatable дает нам полный контроль
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Нелинейная загрузка (как в реальности)
        // Быстро до 40%
        progress.animateTo(40f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
        // Затуп на "сложных вычислениях"
        delay(300)
        // Рывок до 85%
        progress.animateTo(85f, animationSpec = tween(1500, easing = LinearOutSlowInEasing))
        // Финальная догрузка
        progress.animateTo(100f, animationSpec = tween(500, easing = LinearEasing))
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${progress.value.toInt()}%",
            style = TextStyle(
                fontFamily = FontFamily.Monospace, // Моноширинный шрифт (важно для техно!)
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMain,
                letterSpacing = 2.sp
            )
        )
        Text(
            text = "UPLOAD",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = colors.neonAccent,
                letterSpacing = 1.sp
            )
        )
    }
}

@Composable
fun SystemLogMessages() {
    val colors = AvalonTheme.colors
    // Список техно-фраз
    val messages = listOf(
        "INITIALIZING NEURAL LINK...",
        "COMPILING MEMORY SHARDS...",
        "ENCRYPTING PERSONALITY MATRIX...",
        "SYNCHRONIZING WITH SERVER...",
        "FINALIZING CONSTRUCT..."
    )

    var currentMessageIndex by remember { mutableIntStateOf(0) }

    // Переключение сообщений
    LaunchedEffect(Unit) {
        while (currentMessageIndex < messages.size - 1) {
            delay(800) // Каждые 800мс меняем этап
            currentMessageIndex++
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Мигающий курсор >
        Text(
            text = "> ${messages[currentMessageIndex]}",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = colors.textSecondary,
                letterSpacing = 1.sp
            )
        )

        // Декоративный ID сессии снизу
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "SESSION_ID: ${Random.nextLong(100000, 999999)}XF",
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = colors.textSecondary.copy(alpha = 0.65f)
            )
        )
    }
}
