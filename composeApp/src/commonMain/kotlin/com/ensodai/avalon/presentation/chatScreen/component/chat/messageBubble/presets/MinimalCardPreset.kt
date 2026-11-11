package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.presets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun MinimalCardPreset(
    blocks: List<RpgMessageBlock>,
    textColor: Color,
    isStreaming: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        blocks.forEachIndexed { index, block ->
            if (block.text.isBlank()) return@forEachIndexed
            val isLastBlock = index == blocks.lastIndex
            val blockText = if (isLastBlock && isStreaming) block.text + "▎" else block.text

            when (block) {
                is RpgMessageBlock.Plain -> MinimalPlainBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Speech -> MinimalSpeechBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Action -> MinimalActionBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Thought -> MinimalThoughtBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Event -> MinimalEventBlock(text = blockText, textColor = textColor)
            }
        }
    }
}

// 1. Повествование — Теплый песочный / глиняный тинт (Спокойный нейтральный фон)
@Composable
fun MinimalPlainBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFD7CCC8).copy(alpha = 0.05f)) // Мягкий бежево-песочный оттенок
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            color = textColor.copy(alpha = 0.85f),
            fontFamily = FontFamily.Serif,
            fontSize = (14.5 * ratio).sp,
            lineHeight = (21 * ratio).sp,
        )
    }
}

// 2. Реплика (Диалог) — Ледяной синий / Небесный тинт (Главный акцент)
@Composable
fun MinimalSpeechBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val speechColor = Color(0xFF90CAF9) // Ледяной голубой
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(speechColor.copy(alpha = 0.06f)) // Голубое свечение подложки
            .border(0.5.dp, speechColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)) // Тонкий голубой контур
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = speechColor.copy(alpha = 0.4f))) { append("« ") }
                append(text)
                withStyle(SpanStyle(color = speechColor.copy(alpha = 0.4f))) { append(" »") }
            },
            color = textColor,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
            fontSize = (15 * ratio).sp,
            lineHeight = (22 * ratio).sp,
        )
    }
}

// 3. Действия — Нежный шалфейный / Оливковый тинт (Обозначает движение, природу, физику)
@Composable
fun MinimalActionBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFA5D6A7).copy(alpha = 0.04f)) // Мягкий зеленый шалфей
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Text(
            text = text,
            color = textColor.copy(alpha = 0.72f),
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontSize = (14 * ratio).sp,
            lineHeight = (20 * ratio).sp,
        )
    }
}

// 4. Мысли — Лавандовый / Аметистовый тинт (Ассоциируется с ментальным планом и тайнами)
@Composable
fun MinimalThoughtBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val thoughtColor = Color(0xFFCE93D8) // Нежная лаванда
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(thoughtColor.copy(alpha = 0.035f)) // Свечение лаванды
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = thoughtColor.copy(alpha = 0.25f))) { append("( ") }
                append(text.removeSurrounding("(", ")"))
                withStyle(SpanStyle(color = thoughtColor.copy(alpha = 0.25f))) { append(" )") }
            },
            color = textColor.copy(alpha = 0.58f),
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontSize = (13.5 * ratio).sp,
            lineHeight = (19 * ratio).sp,
        )
    }
}

// 5. Событие — Мягкий янтарный / Терракотовый тинт (Обозначает внешние изменения, время, мир)
@Composable
fun MinimalEventBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFFCC80).copy(alpha = 0.045f)) // Благородный янтарный оттенок
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor.copy(alpha = 0.65f),
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * ratio).sp,
            lineHeight = (17 * ratio).sp,
            textAlign = TextAlign.Center
        )
    }
}