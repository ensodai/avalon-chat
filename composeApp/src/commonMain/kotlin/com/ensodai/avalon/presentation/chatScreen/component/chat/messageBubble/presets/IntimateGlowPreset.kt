package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.presets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.domain.models.chat.RpgMessageBlock
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun IntimateGlowPreset(
    blocks: List<RpgMessageBlock>,
    textColor: Color,
    isStreaming: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        // Мягкий зазор между карточками
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEachIndexed { index, block ->
            if (block.text.isBlank()) return@forEachIndexed
            val isLastBlock = index == blocks.lastIndex
            val blockText = if (isLastBlock && isStreaming) block.text + "▎" else block.text

            when (block) {
                is RpgMessageBlock.Plain -> IntimatePlainBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Speech -> IntimateSpeechBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Action -> IntimateActionBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Thought -> IntimateThoughtBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Event -> IntimateEventBlock(text = blockText, textColor = textColor)
            }
        }
    }
}

// Базовый шаблон (Wrapper) для романтической карточки с градиентной рамкой
@Composable
fun RomanticGlowCard(
    borderColors: List<Color>,
    modifier: Modifier = Modifier,
    // Глубокий виноградно-черный фон (создает ощущение тепла и полумрака)
    backgroundColor: Color = Color(0xFF141118),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            // Плавный градиент на границе карточки
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(colors = borderColors),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        content()
    }
}

// 1. Повествование — Перелив от глубокого синего к небесному (Спокойное описание сцены)
@Composable
fun IntimatePlainBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    RomanticGlowCard(
        borderColors = listOf(
            Color(0xFF3F51B5).copy(alpha = 0.4f), // Глубокий синий
            Color(0xFF90CAF9).copy(alpha = 0.4f)  // Небесно-голубой
        ),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor.copy(alpha = 0.85f),
            fontFamily = FontFamily.Serif,
            fontSize = (14.5 * ratio).sp,
            lineHeight = (22 * ratio).sp,
        )
    }
}

// 2. Реплика (Диалог) — Перелив от небесного к нежно-розовому (Теплота голоса и близость)
@Composable
fun IntimateSpeechBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    RomanticGlowCard(
        borderColors = listOf(
            Color(0xFF90CAF9).copy(alpha = 0.5f), // Небесно-голубой
            Color(0xFFF48FB1).copy(alpha = 0.5f)  // Нежно-розовый
        ),
        modifier = modifier
    ) {
        Text(
            text = buildAnnotatedString {
                // Изящные розовые кавычки
                withStyle(SpanStyle(color = Color(0xFFF48FB1).copy(alpha = 0.4f))) { append("« ") }
                append(text)
                withStyle(SpanStyle(color = Color(0xFFF48FB1).copy(alpha = 0.4f))) { append(" »") }
            },
            color = textColor,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
            fontSize = (15 * ratio).sp,
            lineHeight = (23 * ratio).sp,
        )
    }
}

// 3. Действия — Перелив от розового к лавандовому (Касания, взгляды, физическая близость)
@Composable
fun IntimateActionBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    RomanticGlowCard(
        borderColors = listOf(
            Color(0xFFF48FB1).copy(alpha = 0.45f), // Нежно-розовый
            Color(0xFFCE93D8).copy(alpha = 0.45f)  // Лавандовый
        ),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor.copy(alpha = 0.75f),
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic, // Курсив для описания чувственных жестов
            fontSize = (14 * ratio).sp,
            lineHeight = (21 * ratio).sp,
        )
    }
}

// 4. Мысли — Перелив от лавандового к глубокому индиго (Глубокие чувства, осознание любви)
@Composable
fun IntimateThoughtBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    RomanticGlowCard(
        borderColors = listOf(
            Color(0xFFCE93D8).copy(alpha = 0.35f), // Лавандовый
            Color(0xFF5C6BC0).copy(alpha = 0.35f)  // Индиго
        ),
        modifier = modifier
    ) {
        Text(
            text = buildAnnotatedString {
                // Мягкие фиолетовые скобки
                withStyle(SpanStyle(color = Color(0xFFCE93D8).copy(alpha = 0.25f))) { append("( ") }
                append(text.removeSurrounding("(", ")"))
                withStyle(SpanStyle(color = Color(0xFFCE93D8).copy(alpha = 0.25f))) { append(" )") }
            },
            color = textColor.copy(alpha = 0.6f),
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontSize = (13.5 * ratio).sp,
            lineHeight = (20 * ratio).sp,
        )
    }
}

// 5. Атмосфера сцены (Событие) — Мягкий градиентный фон внутри самой карточки (как туман или закат)
@Composable
fun IntimateEventBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    RomanticGlowCard(
        borderColors = listOf(
            Color(0xFFF48FB1).copy(alpha = 0.2f),
            Color(0xFF5C6BC0).copy(alpha = 0.2f)
        ),
        // Сама подложка плавно переливается, создавая ощущение тепла
        backgroundColor = Color(0xFF1F1824),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor.copy(alpha = 0.75f),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
            fontSize = (13 * ratio).sp,
            lineHeight = (19 * ratio).sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}