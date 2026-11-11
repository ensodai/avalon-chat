package com.ensodai.avalon.presentation.commonComponents.cardMonolith

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ensodai.shared.avalon.character.spec.AvalonCardCustomization
import com.ensodai.shared.avalon.character.spec.CardAccentToken
import com.ensodai.shared.avalon.character.spec.CardBgPattern
import com.ensodai.shared.avalon.character.spec.CardFallbackGlyph
import com.ensodai.shared.avalon.character.spec.CardLayout
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager

fun CardAccentToken.toComposeColor(): Color {
    return runCatching {
        val cleanHex = hexColor.removePrefix("#")
        val colorInt = cleanHex.toLong(16) or 0x00000000FF000000L
        Color(colorInt.toInt())
    }.getOrDefault(Color(0xFF00E5FF))
}

@Composable
fun AvalonMonolithCard(
    characterName: String,
    modifier: Modifier = Modifier,
    nickname: String? = null,
    creatorNick: String? = null,
    shortDescription: String = "",
    tags: List<String> = emptyList(),
    avatarModel: Any? = null,
    hasLorebook: Boolean = false,
    versionText: String = "v1.0",
    specVersion: String = "V3",
    tokenCount: Int = 1240,
    altGreetingsCount: Int = 0,
    customization: AvalonCardCustomization = AvalonCardCustomization()
) {
    val colors = AvalonTheme.colors
    val accentColor = customization.accentToken.toComposeColor()
    val cardBgColor = Color(0xFF090A0E)

    Card(
        modifier = modifier
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. Слой фона: паттерн или сплошной арт
            when (customization.layout) {
                CardLayout.FULL_BLEED -> {
                    if (avatarModel != null) {
                        AsyncImage(
                            model = avatarModel,
                            contentDescription = characterName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Затемняющий градиент (Scrim) для читаемости контента
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colorStops = arrayOf(
                                            0.0f to Color(0x66090A0F),
                                            0.35f to Color.Transparent,
                                            0.55f to Color(0xD90C0D12),
                                            0.80f to Color(0xFF0C0D12),
                                            1.0f to Color(0xFF0C0D12)
                                        )
                                    )
                                )
                        )
                    } else {
                        RenderPatternBackground(customization.bgPattern, accentColor)
                    }
                }
                CardLayout.MONOLITH_FRAME, CardLayout.HEX_CORE -> {
                    RenderPatternBackground(customization.bgPattern, accentColor)
                }
            }

            // 2. Основная компоновка зон (Header, Center, Info, Footer)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // --- ЗОНА 1: HEADER ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Слева: Логотип бренда (шестиугольник лежит на ребре)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "⬡",
                            color = accentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = customization.brandingText.uppercase(),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        )
                    }

                    // Справа: Версия и индикаторы (без рамочек, минималистично)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = versionText,
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        if (hasLorebook) {
                            Text(
                                text = "LB",
                                color = accentColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // --- ЗОНА 2: ВИЗУАЛЬНЫЙ ЦЕНТР ---
                when (customization.layout) {
                    CardLayout.FULL_BLEED -> {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    CardLayout.MONOLITH_FRAME -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(0.75.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                                    .background(Color(0xFF0C0D12)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (avatarModel != null) {
                                    AsyncImage(
                                        model = avatarModel,
                                        contentDescription = characterName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    RenderGlyphFallback(customization.fallbackGlyph, characterName, accentColor)
                                }
                            }
                        }
                    }
                    CardLayout.HEX_CORE -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Внешнее акцентное свечение (flat-top)
                            Box(
                                modifier = Modifier
                                    .size(152.dp)
                                    .clip(HexagonShape)
                                    .background(accentColor.copy(alpha = 0.12f))
                            )

                            // Центральный гексагон
                            Box(
                                modifier = Modifier
                                    .size(136.dp)
                                    .clip(HexagonShape)
                                    .background(Color(0xFF111319))
                                    .border(1.2.dp, accentColor.copy(alpha = 0.8f), HexagonShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (avatarModel != null) {
                                    AsyncImage(
                                        model = avatarModel,
                                        contentDescription = characterName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    RenderGlyphFallback(customization.fallbackGlyph, characterName, accentColor)
                                }
                            }
                        }
                    }
                }

                // --- ЗОНА 3 & 4: ИНФОБЛОК И ФУТЕР ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Имя + Автор
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = characterName.ifBlank { "Без имени" },
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            lineHeight = 20.sp,
                            overflow = TextOverflow.Ellipsis
                        )

                        val subtitle = buildString {
                            if (!nickname.isNullOrBlank()) {
                                append("// $nickname ")
                            }
                            if (!creatorNick.isNullOrBlank()) {
                                append("@$creatorNick")
                            }
                        }

                        if (subtitle.isNotBlank()) {
                            Text(
                                text = subtitle,
                                color = accentColor.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Краткое описание
                    if (shortDescription.isNotBlank()) {
                        Text(
                            text = shortDescription,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 15.sp
                        )
                    }

                    // Теги (до 3 штук)
                    if (tags.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tags.take(3).forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.White.copy(alpha = 0.05f)
                                ) {
                                    Text(
                                        text = tag,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Разделитель
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.75.dp)
                            .background(Color.White.copy(alpha = 0.06f))
                    )

                    // ЗОНА 4: FOOTER (Телеметрия + Бейдж протокола без рамочки)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Телеметрия
                        if (customization.showTelemetry) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                val tokText = if (tokenCount >= 1000) "${tokenCount / 1000}k" else "$tokenCount"
                                Text(
                                    text = "⚡ ${tokText}t",
                                    color = Color.White.copy(alpha = 0.45f),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "•",
                                    color = Color.White.copy(alpha = 0.25f),
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = "💬 ${altGreetingsCount}a",
                                    color = Color.White.copy(alpha = 0.45f),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(1.dp))
                        }

                        // Бейдж протокола справа снизу (чистый текст без рамки)
                        val isV3 = specVersion.contains("3", ignoreCase = true)
                        Text(
                            text = "SPEC ${if (isV3) "V3" else "V2"}",
                            color = if (isV3) accentColor else Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderPatternBackground(pattern: CardBgPattern, accentColor: Color) {
    when (pattern) {
        CardBgPattern.HEX_GRID -> {
            HexGridPattern(
                color = accentColor.copy(alpha = 0.04f),
                hexRadius = 26f
            )
        }
        CardBgPattern.SCANLINES -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0A0B0F),
                                Color(0xFF10121A),
                                Color(0xFF0A0B0F)
                            )
                        )
                    )
            )
        }
        CardBgPattern.MATTE_OBSIDIAN -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF090A0E))
            )
        }
    }
}

@Composable
private fun RenderGlyphFallback(
    glyph: CardFallbackGlyph,
    characterName: String,
    accentColor: Color
) {
    when (glyph) {
        CardFallbackGlyph.INITIALS -> {
            val initials = characterName.trim().take(2).uppercase().ifBlank { "AA" }
            Text(
                text = initials,
                color = accentColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        CardFallbackGlyph.AI_CORE -> {
            Icon(
                imageVector = IconsManager.Psychology,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(44.dp)
            )
        }
        CardFallbackGlyph.TERMINAL -> {
            Icon(
                imageVector = IconsManager.Code,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(44.dp)
            )
        }
        CardFallbackGlyph.BOOK -> {
            Icon(
                imageVector = IconsManager.TipsAndUpdates,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(44.dp)
            )
        }
        CardFallbackGlyph.SWORD -> {
            Icon(
                imageVector = IconsManager.SelfImprovement,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(44.dp)
            )
        }
    }
}
