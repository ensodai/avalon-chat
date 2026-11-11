package com.ensodai.avalon.presentation.commonComponents.snackbar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.theme.AvalonTheme
import kotlinx.coroutines.delay

@Composable
fun TechnoUndoSnackbar(
    message: String,
    onUndo: () -> Unit,
    onDismiss: () -> Unit,
    durationMillis: Int = 4000 // 4 секунды на раздумья
) {
    val colors = AvalonTheme.colors

    // Состояние таймера (от 1.0 до 0.0)
    var progress by remember { mutableFloatStateOf(1f) }

    // Запускаем таймер
    LaunchedEffect(Unit) {
        val steps = 100
        val stepTime = durationMillis / steps.toLong()
        for (i in 0..steps) {
            progress = 1f - (i / steps.toFloat())
            delay(stepTime)
        }
        onUndo() // Время вышло, скрываем, действие фиксируется
    }

    // Внешний вид
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp), // Стандартная высота, чтобы пальцем попадать
        shape = RoundedCornerShape(12.dp),
        color = colors.overlaySurface,
        border = BorderStroke(1.dp, colors.overlayBorder),
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. ПОЛОСКА ТАЙМЕРА (Снизу)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.BottomCenter),
                color = colors.neonAccent,
                trackColor = Color.Transparent,
                strokeCap = StrokeCap.Square,
            )

            // 2. КОНТЕНТ
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Текст сообщения
                Text(
                    text = message,
                    color = colors.textMain,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                // Кнопка ОТМЕНА (Текст + Иконка возврата)
                TextButton(
                    onClick = onUndo,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colors.neonAccent
                    )
                ) {
                    Text(
                        text = "ОТМЕНА",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
