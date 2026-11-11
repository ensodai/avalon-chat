package com.ensodai.avalon.presentation.chatScreen.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.AlignLeft
import com.composables.icons.lucide.Box
import com.composables.icons.lucide.BrainCircuit
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageCircle
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.Zap
import com.mikepenz.markdown.annotator.AnnotatorSettings
import com.mikepenz.markdown.annotator.annotatorSettings
import com.mikepenz.markdown.annotator.buildMarkdownAnnotatedString
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownText
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.MarkdownTypography
import com.ensodai.avalon.theme.AvalonTheme
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode

/**
 * # MarkdownRPGRendering
 *
 * Специализированная конфигурация рендеринга Markdown для RPG-чатов.
 * Превращает стандартные элементы разметки в стильные визуальные блоки, улучшая UX ролевой игры.
 *
 * ## Поддерживаемые элементы:
 * - **`**Жирный текст**` (Impact Block):**
 *   Выделяется в отдельный блок с зеленой рамкой. Используется для криков, важных событий или громких звуков.
 * - **`*Курсив*` (Action Block):**
 *   Выделяется в блок с мягким серым фоном. Используется для описания действий персонажа (*достал меч*, *улыбнулся*).
 * - **`> Цитата` (Thought Block):**
 *   Блок с фиолетовой полосой. Используется для мыслей персонажа или телепатии.
 * - **`` `Код` `` (System Block):**
 *   Темный блок с моноширинным шрифтом. Используется для системных сообщений, статов или логов.
 * - **`— Прямая речь` (Speech Block):**
 *   Текст, начинающийся с длинного тире (—) или дефиса (- ), выделяется как прямая речь.
 * - **`- Список` (List Block):**
 *   Стилизованный список с кастомными буллетами.
 */
object MarkdownRPGRendering {
    private val SpeechIcon = Lucide.MessageCircle // Нейтральный кружок сообщения

    private val ActionIcon = Lucide.Zap // Энергия/Действие (универсально)
    private val ImpactIcon = Lucide.Star // Звезда = событие, важность
    private val ThoughtIcon = Lucide.BrainCircuit // Или просто Brain
    private val SystemIcon = Lucide.Box // Просто блок данных

    @Composable
    private fun speechTint(): Color = AvalonTheme.colors.textMain

    @Composable
    private fun actionTint(): Color = AvalonTheme.colors.successAccent

    @Composable
    private fun actionBg(): Color = AvalonTheme.colors.successAccent.copy(alpha = 0.22f)

    @Composable
    private fun impactTint(): Color = AvalonTheme.colors.premiumAccent

    @Composable
    private fun impactBg(): Color = AvalonTheme.colors.warningAccent.copy(alpha = 0.22f)

    @Composable
    private fun thoughtTint(): Color = AvalonTheme.colors.accentSecondary

    @Composable
    private fun thoughtBg(): Color = AvalonTheme.colors.accentSecondary.copy(alpha = 0.22f)

    @Composable
    private fun systemTint(): Color = AvalonTheme.colors.onlineStatus

    @Composable
    private fun systemBg(): Color = AvalonTheme.colors.onlineStatus.copy(alpha = 0.22f)

    @Composable
    fun createTypography(baseStyle: TextStyle): MarkdownTypography {
        val systemTint = systemTint()
        val codeBackground = AvalonTheme.colors.overlaySurface
        return markdownTypography(
            text = baseStyle,
            paragraph = baseStyle,
            // Сбрасываем дефолтные стили, мы рисуем свои блоки
            quote = baseStyle.copy(fontStyle = FontStyle.Normal),
            inlineCode = baseStyle.copy(
                color = systemTint,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                background = codeBackground
            ),
            code = baseStyle.copy(
                color = systemTint,
                fontFamily = FontFamily.Monospace,
                background = Color.Transparent
            )
        )
    }

    // ============================================================================================
    // 🧩 КОМПОНЕНТЫ (Логика)
    // ============================================================================================

    @Composable
    fun createComponents(baseStyle: TextStyle): MarkdownComponents {
        return markdownComponents(
            // Мысли
            blockQuote = {
                ThoughtBlock(it.content, it.node, baseStyle)
            },
            // Системные данные
            codeBlock = {
                SystemBlock(it.content, it.node, baseStyle)
            },
            // Основной текст (Речь, Действия, События)
            paragraph = { model ->
                ParagraphSplitter(
                    content = model.content,
                    node = model.node,
                    typography = model.typography,
                    annotatorSettings = annotatorSettings()
                )
            }
        )
    }

    // ============================================================================================
    // 🛠️ UNIVERSAL AESTHETIC BLOCK
    // ============================================================================================

    /**
     * Единый стиль для ВСЕГО.
     * Отличается только "весом" (наличием фона) и цветом.
     */
    @Composable
    private fun AestheticBlock(
        accentColor: Color,
        bgColor: Color,
        icon: ImageVector,
        label: String?, // Лейбл теперь опционален! Если null - не рисуем.
        isTransparent: Boolean = false, // Для прямой речи, чтобы она была легкой
        content: @Composable () -> Unit
    ) {
        val background = if (isTransparent) Color.Transparent else bgColor.copy(alpha = 0.15f)

        Row(
            modifier = Modifier
                .padding(vertical = 3.dp) // Чуть плотнее
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp)) // Менее круглые, более "книжные" углы
                .background(background)
                .height(IntrinsicSize.Min)
        ) {
            // 1. Цветной маркер слева (Тонкий и элегантный)
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(accentColor.copy(alpha = 0.8f))
            )

            // 2. Контент
            Column(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .weight(1f)
            ) {
                // Заголовок (Иконка + Текст)
                // Рисуем, только если есть label, ИЛИ если это не прозрачный блок
                // Это позволяет речи быть чище, а действиям - заметнее
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )

                    if (label != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label.uppercase(),
                            style = TextStyle(
                                color = accentColor.copy(alpha = 0.5f), // Очень ненавязчивый лейбл
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }

                // Сам текст
                content()
            }
        }
    }

    // ============================================================================================
    // 🛠️ РЕНДЕРЕРЫ
    // ============================================================================================

    @Composable
    private fun ParagraphSplitter(
        content: String,
        node: ASTNode,
        typography: MarkdownTypography,
        annotatorSettings: AnnotatorSettings
    ) {
        val speechTint = speechTint()
        val actionTint = actionTint()
        val actionBg = actionBg()
        val impactTint = impactTint()
        val impactBg = impactBg()
        val chunks = parseChildrenToChunks(content, node.children)

        Column {
            chunks.forEachIndexed { index, chunk ->
                when (chunk) {
                    // --- 1. ПРЯМАЯ РЕЧЬ ---
                    // В блоке, но БЕЗ фона (isTransparent = true).
                    // Это создает структуру, но не перегружает.
                    is RenderChunk.SpeechBlock -> {
                        AestheticBlock(
                            accentColor = speechTint,
                            bgColor = Color.Transparent,
                            icon = SpeechIcon,
                            label = null, // Убираем слово "MESSAGE", иконки достаточно
                            isTransparent = true
                        ) {
                            val styledText = buildAnnotatedString {
                                pushStyle(
                                    typography.paragraph.toSpanStyle().copy(
                                        color = speechTint,
                                        fontSize = 15.sp // Чуть крупнее для читаемости
                                    )
                                )
                                buildMarkdownAnnotatedString(
                                    content,
                                    chunk.nodes,
                                    annotatorSettings
                                )
                                pop()
                            }
                            MarkdownText(content = styledText, node = node, style = typography.paragraph)
                        }
                    }

                    // --- 2. ДЕЙСТВИЕ (*курсив*) ---
                    // Легкий фон. Слово "Action" можно убрать или оставить.
                    // Давай оставим, но мелким.
                    is RenderChunk.ActionBlock -> {
                        AestheticBlock(
                            accentColor = actionTint,
                            bgColor = actionBg,
                            icon = ActionIcon,
                            label = "ACTION" // Можно заменить на null, если хочешь только иконку
                        ) {
                            val styledText = buildAnnotatedString {
                                pushStyle(
                                    typography.paragraph.toSpanStyle().copy(
                                        color = speechTint,
                                        fontStyle = FontStyle.Italic
                                    )
                                )
                                buildMarkdownAnnotatedString(
                                    content,
                                    chunk.node.children,
                                    annotatorSettings
                                )
                                pop()
                            }
                            MarkdownText(content = styledText, node = chunk.node, style = typography.paragraph)
                        }
                    }

                    // --- 3. СОБЫТИЕ/ВАЖНО (**жирный**) ---
                    // Вместо ALERT - просто EVENT.
                    is RenderChunk.ImpactBlock -> {
                        AestheticBlock(
                            accentColor = impactTint,
                            bgColor = impactBg,
                            icon = ImpactIcon,
                            label = "EVENT"
                        ) {
                            val styledText = buildAnnotatedString {
                                pushStyle(
                                    typography.paragraph.toSpanStyle().copy(
                                        color = speechTint,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                )
                                buildMarkdownAnnotatedString(
                                    content,
                                    chunk.node.children,
                                    annotatorSettings
                                )
                                pop()
                            }
                            MarkdownText(content = styledText, node = chunk.node, style = typography.paragraph)
                        }
                    }

                    // --- 4. ПРОСТОЙ ТЕКСТ ---
                    is RenderChunk.TextChunk -> {
                        if (chunk.nodes.isNotEmpty()) {
                            AestheticBlock(
                                accentColor = speechTint.copy(alpha = 0.3f),
                                bgColor = Color.Transparent,
                                icon = Lucide.AlignLeft,
                                label = null,
                                isTransparent = true
                            ) {
                                val styledText = buildAnnotatedString {
                                    pushStyle(typography.paragraph.toSpanStyle())
                                    buildMarkdownAnnotatedString(
                                        content,
                                        chunk.nodes,
                                        annotatorSettings
                                    )
                                    pop()
                                }
                                MarkdownText(content = styledText, node = node, style = typography.paragraph)
                            }
                        }
                    }
                }

                // Минимальный отступ, так как у блоков есть padding внутри
                if (index < chunks.lastIndex) {
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }

    // --- Thoughts (Мысли) ---
    @Composable
    private fun ThoughtBlock(
        content: String,
        node: ASTNode,
        baseStyle: TextStyle
    ) {
        val thoughtTint = thoughtTint()
        AestheticBlock(
            accentColor = thoughtTint,
            bgColor = thoughtBg(),
            icon = ThoughtIcon,
            label = "THOUGHT"
        ) {
            MarkdownText(
                content = content,
                node = node,
                style = baseStyle.copy(
                    color = thoughtTint,
                    fontStyle = FontStyle.Italic
                )
            )
        }
    }

    // --- System (Тех. данные) ---
    @Composable
    private fun SystemBlock(
        content: String,
        node: ASTNode,
        baseStyle: TextStyle
    ) {
        val systemTint = systemTint()
        AestheticBlock(
            accentColor = systemTint,
            bgColor = systemBg(),
            icon = SystemIcon,
            label = "INFO" // Универсально: Инфо, а не Система
        ) {
            MarkdownText(
                content = content,
                node = node,
                style = baseStyle.copy(
                    color = systemTint,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            )
        }
    }
}
// ============================================================================================
// ⚙️ ЛОГИКА ПАРСИНГА (Chunks)
// ============================================================================================

private sealed class RenderChunk {
    data class TextChunk(val nodes: List<ASTNode>) : RenderChunk()
    data class ImpactBlock(val node: ASTNode) : RenderChunk()
    data class ActionBlock(val node: ASTNode) : RenderChunk()
    data class SpeechBlock(val nodes: List<ASTNode>) : RenderChunk()
}

private fun parseChildrenToChunks(content: String, children: List<ASTNode>): List<RenderChunk> {
    val chunks = mutableListOf<RenderChunk>()
    val currentTextNodes = mutableListOf<ASTNode>()

    fun flushText() {
        if (currentTextNodes.isNotEmpty()) {
            // Find first node with meaningful text (ignoring leading whitespace nodes)
            val firstMeaningfulNode = currentTextNodes.firstOrNull {
                it.getTextInNode(content).toString().isNotBlank()
            }
            val text = firstMeaningfulNode?.getTextInNode(content)?.toString()?.trimStart() ?: ""

            if (text.startsWith("—") || text.startsWith("- ")) {
                chunks.add(RenderChunk.SpeechBlock(currentTextNodes.toList()))
            } else {
                chunks.add(RenderChunk.TextChunk(currentTextNodes.toList()))
            }
            currentTextNodes.clear()
        }
    }

    children.forEach { child ->
        when (child.type) {
            MarkdownElementTypes.STRONG -> {
                flushText()
                chunks.add(RenderChunk.ImpactBlock(child))
            }

            MarkdownElementTypes.EMPH -> {
                flushText()
                chunks.add(RenderChunk.ActionBlock(child))
            }

            else -> {
                currentTextNodes.add(child)
            }
        }
    }
    flushText()
    return chunks
}
