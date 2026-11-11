package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.presets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.domain.models.chat.RpgMessageBlock
import com.ensodai.avalon.theme.AvalonTheme
import androidx.compose.ui.unit.em

@Composable
fun VisualNovelPreset(
    blocks: List<RpgMessageBlock>,
    textColor: Color,
    isStreaming: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        blocks.forEachIndexed { index, block ->
            if (block.text.isBlank()) return@forEachIndexed
            val isLastBlock = index == blocks.lastIndex
            val blockText = if (isLastBlock && isStreaming) block.text + "▎" else block.text

            val prevBlock = blocks.getOrNull(index - 1)

            // Задаем интервалы под игровой стиль
            val spacing = when {
                prevBlock == null -> 0.dp
                prevBlock is RpgMessageBlock.Event || block is RpgMessageBlock.Event -> 16.dp
                prevBlock::class != block::class -> 10.dp
                else -> 6.dp
            }

            if (spacing > 0.dp) Spacer(modifier = Modifier.height(spacing))

            when (block) {
                is RpgMessageBlock.Plain -> VnPlainBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Speech -> VnSpeechBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Action -> VnActionBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Thought -> VnThoughtBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Event -> VnEventBlock(text = blockText, textColor = textColor)
            }
        }
    }
}

// 1. Повествование — ровный, чистый, современный шрифт без засечек.
@Composable
fun VnPlainBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Text(
        modifier = modifier.fillMaxWidth(),
        text = text,
        color = textColor.copy(alpha = 0.85f),
        fontFamily = FontFamily.SansSerif, // Перешли на SansSerif
        fontSize = (15 * ratio).sp,
        lineHeight = (22 * ratio).sp,
    )
}

// 2. Прямая речь — выделяется крупным размером и характерными уголками 「...」
@Composable
fun VnSpeechBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Text(
        modifier = modifier.fillMaxWidth(),
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = textColor.copy(alpha = 0.4f))) { append("「") }
            append(text)
            withStyle(SpanStyle(color = textColor.copy(alpha = 0.4f))) { append("」") }
        },
        color = textColor,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = (16 * ratio).sp,
        lineHeight = (24 * ratio).sp,
    )
}

// 3. Действия — в новеллах действия описываются как закадровый текст.
// Мы сделаем их чуть более приглушенными и наклонными.
@Composable
fun VnActionBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Text(
        modifier = modifier.fillMaxWidth(),
        text = text,
        color = textColor.copy(alpha = 0.7f),
        fontFamily = FontFamily.SansSerif,
        fontStyle = FontStyle.Italic,
        fontSize = (14.5 * ratio).sp,
        lineHeight = (21 * ratio).sp,
    )
}

// 4. Мысли — оформлены в двойные кавычки-уголки 『...』, имитирующие телепатию или внутренний голос.
@Composable
fun VnThoughtBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Text(
        modifier = modifier.fillMaxWidth(),
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = textColor.copy(alpha = 0.35f))) { append("『") } // Без пробела
            append(text.removeSurrounding("(", ")"))
            withStyle(SpanStyle(color = textColor.copy(alpha = 0.35f))) { append("』") } // Без пробела
        },
        color = textColor.copy(alpha = 0.6f),
        fontFamily = FontFamily.SansSerif,
        fontStyle = FontStyle.Italic,
        fontSize = (14.5 * ratio).sp,
        lineHeight = (21 * ratio).sp,
    )
}

// 5. События — оформлены в виде аккуратных центрированных игровых плашек с полупрозрачным фоном.
@Composable
fun VnEventBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .background(textColor.copy(alpha = 0.07f), RoundedCornerShape(8.dp)) // Чуть плотнее фон
                .padding(horizontal = 16.dp, vertical = 6.dp), // Больше воздуха внутри плашки
            text = text, // Убрали принудительный .uppercase() для мягкого lowercase-стиля как на скрине
            color = textColor.copy(alpha = 0.7f), // Чуть выше контраст для читаемости на сером фоне
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * ratio).sp,
            lineHeight = (17 * ratio).sp,
        )
    }
}