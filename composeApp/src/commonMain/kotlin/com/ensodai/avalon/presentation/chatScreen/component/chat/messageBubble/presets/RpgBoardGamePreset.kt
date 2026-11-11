package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.presets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.domain.models.chat.RpgMessageBlock
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun RpgBoardgamePreset(
    blocks: List<RpgMessageBlock>,
    textColor: Color,
    isStreaming: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        // Небольшое расстояние между картами, чтобы они ложились на "стол" стопкой
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEachIndexed { index, block ->
            if (block.text.isBlank()) return@forEachIndexed
            val isLastBlock = index == blocks.lastIndex
            val blockText = if (isLastBlock && isStreaming) block.text + "▎" else block.text

            when (block) {
                is RpgMessageBlock.Plain -> BoardgamePlainBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Speech -> BoardgameSpeechBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Action -> BoardgameActionBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Thought -> BoardgameThoughtBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Event -> BoardgameEventBlock(text = blockText, textColor = textColor)
            }
        }
    }
}

@Composable
fun BoardgameCard(
    headerText: String,
    icon: String,
    borderColor: Color,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF191919),
    content: @Composable ColumnScope.() -> Unit
) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor.copy(alpha = 0.35f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$icon  $headerText",
                color = borderColor.copy(alpha = 0.85f),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = (10 * ratio).sp,
                letterSpacing = 0.12.em
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun BoardgamePlainBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    BoardgameCard(
        headerText = "CHRONICLE",
        icon = "📖",
        borderColor = Color(0xFF8E9B9E),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor.copy(alpha = 0.85f),
            fontFamily = FontFamily.Serif,
            fontSize = (14 * ratio).sp,
            lineHeight = (21 * ratio).sp,
        )
    }
}

@Composable
fun BoardgameSpeechBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    BoardgameCard(
        headerText = "DIALOGUE",
        icon = "📜",
        borderColor = Color(0xFFE5C158),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Medium,
            fontSize = (14.5 * ratio).sp,
            lineHeight = (22 * ratio).sp,
        )
    }
}

@Composable
fun BoardgameActionBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    BoardgameCard(
        headerText = "ACTION",
        icon = "⚔️",
        borderColor = Color(0xFFA0AAB5),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor.copy(alpha = 0.75f),
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontSize = (14 * ratio).sp,
            lineHeight = (20 * ratio).sp,
        )
    }
}

@Composable
fun BoardgameThoughtBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    BoardgameCard(
        headerText = "INSIGHT",
        icon = "🕯️",
        borderColor = Color(0xFFB388FF),
        modifier = modifier
    ) {
        Text(
            text = text.removeSurrounding("(", ")"),
            color = textColor.copy(alpha = 0.6f),
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            fontSize = (13.5 * ratio).sp,
            lineHeight = (19 * ratio).sp,
        )
    }
}

@Composable
fun BoardgameEventBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    BoardgameCard(
        headerText = "WORLD EVENT",
        icon = "🧭",
        borderColor = Color(0xFFCD7F32),
        backgroundColor = Color(0xFF211D1A),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor.copy(alpha = 0.8f),
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (12.5 * ratio).sp,
            lineHeight = (18 * ratio).sp,
        )
    }
}