package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.presets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ensodai.avalon.domain.models.chat.RpgMessageBlock
import com.ensodai.avalon.theme.AvalonTheme

@Composable
fun CinematicScriptPreset(
    blocks: List<RpgMessageBlock>,
    textColor: Color,
    isStreaming: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        blocks.forEachIndexed { index, block ->
            if (block.text.isBlank()) return@forEachIndexed
            val isLastBlock = index == blocks.lastIndex
            val blockText = if (isLastBlock && isStreaming) block.text + "▎" else block.text

            val prevBlock = blocks.getOrNull(index - 1)

            val spacing = when {
                prevBlock == null -> 0.dp
                prevBlock is RpgMessageBlock.Event || block is RpgMessageBlock.Event -> 18.dp
                else -> 14.dp
            }

            if (spacing > 0.dp) Spacer(modifier = Modifier.height(spacing))

            when (block) {
                is RpgMessageBlock.Plain -> CinematicPlainBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Speech -> CinematicSpeechBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Action -> CinematicActionBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Thought -> CinematicThoughtBlock(text = blockText, textColor = textColor)
                is RpgMessageBlock.Event -> CinematicEventBlock(text = blockText, textColor = textColor)
            }
        }
    }
}

@Composable
fun CinematicPlainBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "• SCENE NARRATIVE",
            color = textColor.copy(alpha = 0.35f),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = (9 * ratio).sp,
            letterSpacing = 0.12.em
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = text,
            color = textColor.copy(alpha = 0.85f),
            fontFamily = FontFamily.SansSerif,
            fontSize = (14 * ratio).sp,
            lineHeight = (21 * ratio).sp,
        )
    }
}

@Composable
fun CinematicSpeechBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "■ DIALOGUE LINE",
            color = textColor.copy(alpha = 0.45f),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = (9 * ratio).sp,
            letterSpacing = 0.12.em
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = text,
            color = textColor,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = (15 * ratio).sp,
            lineHeight = (22 * ratio).sp,
        )
    }
}

@Composable
fun CinematicActionBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "▶ ACTION CUE",
            color = textColor.copy(alpha = 0.35f),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = (9 * ratio).sp,
            letterSpacing = 0.12.em
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = text,
            color = textColor.copy(alpha = 0.75f),
            fontFamily = FontFamily.SansSerif,
            fontStyle = FontStyle.Italic,
            fontSize = (14 * ratio).sp,
            lineHeight = (20 * ratio).sp,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
fun CinematicThoughtBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "░ V.O. (INTERNAL MONOLOGUE)",
            color = textColor.copy(alpha = 0.35f),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = (9 * ratio).sp,
            letterSpacing = 0.12.em
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = text.removeSurrounding("(", ")"),
            color = textColor.copy(alpha = 0.6f),
            fontFamily = FontFamily.SansSerif,
            fontStyle = FontStyle.Italic,
            fontSize = (13.5 * ratio).sp,
            lineHeight = (20 * ratio).sp,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
fun CinematicEventBlock(text: String, textColor: Color, modifier: Modifier = Modifier) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(textColor.copy(alpha = 0.04f))
            .border(0.5.dp, textColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                // Исправлена опечатка ENVIROMENT -> ENVIRONMENT
                text = "🎬 SLUGLINE / ENVIRONMENT SHIFT",
                color = textColor.copy(alpha = 0.45f),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = (8.5 * ratio).sp,
                letterSpacing = 0.1.em
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "REC",
                    color = textColor.copy(alpha = 0.45f),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = (8.5 * ratio).sp,
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = text, // Сохраняем исходный регистр текста, который передает парсер
            color = textColor.copy(alpha = 0.75f),
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (11.5 * ratio).sp,
            lineHeight = (17 * ratio).sp,
        )
    }
}