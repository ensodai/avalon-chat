package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.presets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
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
fun CyberpunkLogPreset(
    blocks: List<RpgMessageBlock>,
    textColor: Color,
    isStreaming: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp) // Карточки плотно стыкуются как логи в терминале
    ) {
        blocks.forEachIndexed { index, block ->
            if (block.text.isBlank()) return@forEachIndexed
            val isLastBlock = index == blocks.lastIndex
            val blockText = if (isLastBlock && isStreaming) block.text + "▎" else block.text

            when (block) {
                is RpgMessageBlock.Plain -> CyberpunkPlainBlock(text = blockText)
                is RpgMessageBlock.Speech -> CyberpunkSpeechBlock(text = blockText)
                is RpgMessageBlock.Action -> CyberpunkActionBlock(text = blockText)
                is RpgMessageBlock.Thought -> CyberpunkThoughtBlock(text = blockText)
                is RpgMessageBlock.Event -> CyberpunkEventBlock(text = blockText)
            }
        }
    }
}

// Общий шаблон (Wrapper) для киберпанк-карточки со срезанными углами
@Composable
fun CyberpunkCard(
    headerText: String,
    neonColor: Color,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF0A0A0A), // Ультра-черный фон для неонового контраста
    content: @Composable ColumnScope.() -> Unit
) {
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    Column(
        modifier = modifier
            .fillMaxWidth()
            // Скошенные углы — визитная карточка Sci-Fi дизайна
            .clip(CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = neonColor.copy(alpha = 0.25f), // Мягкое неоновое свечение рамки
                shape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // Системная шапка
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "//$headerText",
                color = neonColor,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = (9 * ratio).sp,
                letterSpacing = 0.1.em
            )
            Text(
                text = "SYS_STATUS: OK",
                color = neonColor.copy(alpha = 0.4f),
                fontFamily = FontFamily.Monospace,
                fontSize = (8 * ratio).sp,
                letterSpacing = 0.05.em
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

// 1. Повествование — Спокойная стальная карточка
@Composable
fun CyberpunkPlainBlock(text: String, modifier: Modifier = Modifier) {
    val steelColor = Color(0xFF9EADBC) // Стальной серый
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    CyberpunkCard(
        headerText = "CORE_LOG // AMBIENT_STATE",
        neonColor = steelColor,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.85f),
            fontFamily = FontFamily.SansSerif,
            fontSize = (14 * ratio).sp,
            lineHeight = (21 * ratio).sp,
        )
    }
}

// 2. Реплика (Диалог) — Яркий бирюзовый неоновый канал с консольной стрелочкой >>
@Composable
fun CyberpunkSpeechBlock(text: String, modifier: Modifier = Modifier) {
    val cyanColor = Color(0xFF00F0FF) // Неоновый бирюзовый
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    CyberpunkCard(
        headerText = "COMMS_FEED // INCOMING_AUDIO",
        neonColor = cyanColor,
        modifier = modifier
    ) {
        Text(
            text = ">> $text", // Консольный маркер ввода-вывода
            color = Color.White,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = (14.5 * ratio).sp,
            lineHeight = (21 * ratio).sp,
        )
    }
}

// 3. Действия — Желтая сигнальная карточка, наклонный шрифт
@Composable
fun CyberpunkActionBlock(text: String, modifier: Modifier = Modifier) {
    val yellowColor = Color(0xFFFCEE09) // Сигнальный желтый (Cyberpunk Yellow)
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    CyberpunkCard(
        headerText = "PHYS_LOG // ACTIVE_TRACKING",
        neonColor = yellowColor,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.8f),
            fontFamily = FontFamily.SansSerif,
            fontStyle = FontStyle.Italic,
            fontSize = (13.5 * ratio).sp,
            lineHeight = (20 * ratio).sp,
        )
    }
}

// 4. Мысли — Розовая/пурпурная карточка нейросети
@Composable
fun CyberpunkThoughtBlock(text: String, modifier: Modifier = Modifier) {
    val magentaColor = Color(0xFFFF0055) // Неоновая маджента
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    CyberpunkCard(
        headerText = "NEURAL_NET // COGNITIVE_DUMP",
        neonColor = magentaColor,
        modifier = modifier
    ) {
        Text(
            text = text.removeSurrounding("(", ")"),
            color = Color.White.copy(alpha = 0.7f),
            fontFamily = FontFamily.SansSerif,
            fontStyle = FontStyle.Italic,
            fontSize = (13.5 * ratio).sp,
            lineHeight = (19 * ratio).sp,
        )
    }
}

// 5. Событие — Зеленый лог системного оповещения, весь текст в верхнем регистре
@Composable
fun CyberpunkEventBlock(text: String, modifier: Modifier = Modifier) {
    val greenColor = Color(0xFF00FF66) // Матричный зеленый
    val ratio = AvalonTheme.colors.bubbleFontSize.toFloat() / 16f
    CyberpunkCard(
        headerText = "ENV_ALERT // SECTOR_SCANNER",
        neonColor = greenColor,
        modifier = modifier
    ) {
        Text(
            text = text.uppercase(),
            color = Color.White.copy(alpha = 0.85f),
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = (11.5 * ratio).sp,
            lineHeight = (17 * ratio).sp,
        )
    }
}