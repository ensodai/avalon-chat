package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.presets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.theme.AvalonTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import com.ensodai.avalon.domain.models.chat.RpgMessageBlock

@Composable
fun RoleplayClassicPreset(
    blocks: List<RpgMessageBlock>,
    textColor: Color,
    isStreaming: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), // Увеличили боковые отступы контейнера для «книжного» формата
    ) {
        blocks.forEachIndexed { index, block ->
            if (block.text.isBlank()) return@forEachIndexed
            val isLastBlock = index == blocks.lastIndex
            val blockText = if (isLastBlock && isStreaming) block.text + "▎" else block.text

            val prevBlock = blocks.getOrNull(index - 1)

            // Настраиваем чистые и понятные интервалы между блоками
            val spacing = when {
                prevBlock == null -> 0.dp
                // Если переходим к событию (смене сцены) или уходим от него — даем больше воздуха
                prevBlock is RpgMessageBlock.Event || block is RpgMessageBlock.Event -> 16.dp
                // Между разными типами блоков (например, действие -> речь) делаем аккуратный отступ
                prevBlock::class != block::class -> 12.dp
                else -> 8.dp
            }

            if (spacing > 0.dp) Spacer(modifier = Modifier.height(spacing))

            when (block) {
                is RpgMessageBlock.Plain -> PlainBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Action -> ActionBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Thought -> ThoughtBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Speech -> SpeechBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Event -> EventBlock(text = blockText, textColor = textColor)
            }
        }
    }
}

// 1. Повествование (Простой текст) — теперь это основное «тело» чтения. Шрифт прямой, легкий для глаз.
@Composable
fun PlainBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Text(
        modifier = modifier.fillMaxWidth(),
        text = text,
        color = textColor.copy(alpha = 0.85f), // Слегка приглушен, чтобы уступить первое место прямой речи
        fontFamily = FontFamily.Serif,
        fontStyle = FontStyle.Normal,
        fontSize = (15 * ratio).sp,
        lineHeight = (22 * ratio).sp,
    )
}

// 2. Прямая речь — выделяется максимальной яркостью и изящными кавычками.
@Composable
fun SpeechBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Text(
        modifier = modifier.fillMaxWidth(),
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = textColor.copy(alpha = 0.35f))) { append("«") }
            append(text)
            withStyle(SpanStyle(color = textColor.copy(alpha = 0.35f))) { append("»") }
        },
        color = textColor, // 100% яркость
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Medium,
        fontSize = (15.5 * ratio).sp,
        lineHeight = (23 * ratio).sp,
    )
}

// 3. Действия — курсив с очень тонкой, едва заметной полоской слева. Отступ минимальный, чтобы не ломать ось чтения.
@Composable
fun ActionBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(1.5.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(1.dp))
                .background(textColor.copy(alpha = 0.2f))
        )
        Spacer(modifier = Modifier.width(8.dp)) // Небольшой аккуратный зазор
        Text(
            text = text,
            color = textColor.copy(alpha = 0.7f),
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontSize = (14.5 * ratio).sp,
            lineHeight = (21 * ratio).sp,
            modifier = Modifier.weight(1f)
        )
    }
}

// 4. Мысли — оформлены в каноничные скобки вместо списочных точек (буллитов). Выглядят тихо и интимно.
@Composable
fun ThoughtBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Text(
        modifier = modifier.fillMaxWidth(), // Убрали .padding(start = 8.dp)
        text = buildAnnotatedString {
            // Мягкий полупрозрачный шеврон обозначает начало мысли
            withStyle(SpanStyle(color = textColor.copy(alpha = 0.25f))) { append("‹ ") }
            append(text.removeSurrounding("(", ")"))
            // И такой же шеврон в конце
            withStyle(SpanStyle(color = textColor.copy(alpha = 0.25f))) { append(" ›") }
        },
        color = textColor.copy(alpha = 0.55f),
        fontFamily = FontFamily.Serif,
        fontStyle = FontStyle.Italic,
        fontSize = (14 * ratio).sp,
        lineHeight = (21 * ratio).sp,
    )
}

// 5. События (Окружение) — вместо центрирования мы выравниваем текст по левому краю,
// но визуально отделяем его от сцены короткой изящной линией сверху.
@Composable
fun EventBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Короткая разделительная линия сверху (занимает 25% ширины экрана)
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(0.25f),
            thickness = 1.dp,
            color = textColor.copy(alpha = 0.15f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = text,
            color = textColor.copy(alpha = 0.5f),
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Medium,
            fontSize = (13.5 * ratio).sp,
            lineHeight = (20 * ratio).sp,
        )
    }
}