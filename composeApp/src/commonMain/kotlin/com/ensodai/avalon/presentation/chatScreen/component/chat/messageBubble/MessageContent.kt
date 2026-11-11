package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.avalon.theme.AvalonTheme
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor as markdownPalette
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.markdownPadding
import com.mikepenz.markdown.model.rememberMarkdownState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.runtime.key
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.delay
import androidx.compose.animation.animateContentSize
import com.ensodai.avalon.presentation.chatScreen.viewState.MessageBodyUiItem
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import com.ensodai.avalon.domain.models.chat.RpgMessageBlock
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.presets.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth

@Composable
fun MessageContent(
    modifier: Modifier,
    role: MessageRole,
    textItem: MessageBodyUiItem.Text,
    chatRoleplayPreset: ChatRoleplayPreset,
    shouldShowAnimatedText: Boolean,
    isDefaultFocusRequester: Boolean = true,
    isEnableEditMessageMode: Boolean,
    selectedMessagesText: String?,
    isSelected: Boolean,
    minLines: Int,
    onEditMessageText: (String) -> Unit,
    onOpenSettingsScreen: () -> Unit,
) {
    val message = textItem.rawText
    val isStreaming = textItem.isStreaming

    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0.0f at 0
                1.0f at 500
                1.0f at 1000
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val colors = AvalonTheme.colors
    val textColor = when (role) {
        MessageRole.USER -> colors.userBubbleTextColor
        MessageRole.SYSTEM -> colors.textSecondary
        MessageRole.MODEL -> colors.botBubbleTextColor
    }
    val paddedMessage = remember(message) {
        message.preprocessMarkdownNewlines() + "\u00A0" + "\u00A0" + "\u00A0" + "\u00A0"
    }
    val bubbleFontSize = colors.bubbleFontSize.sp
    val commonTextStyle = remember(textColor, bubbleFontSize) {
        TextStyle(
            color = textColor,
            fontSize = bubbleFontSize,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            lineHeight = (colors.bubbleFontSize * 1.47f).sp,
            textAlign = TextAlign.Start,
        )
    }
    val markdownPaddingValues = markdownPadding(
        block = 8.dp,
        list = 8.dp
    )
    val markdownTypographyValues = markdownTypography(
        text = commonTextStyle,
        paragraph = commonTextStyle
    )
    val markdownColorValues = markdownPalette(text = commonTextStyle.color)

    var smoothedText by remember { mutableStateOf("") }

    LaunchedEffect(message, isStreaming) {
        if (!isStreaming) {
            smoothedText = message
            return@LaunchedEffect
        }
        
        if (smoothedText.isEmpty() || !message.startsWith(smoothedText)) {
            smoothedText = message
        }

        while (smoothedText.length < message.length) {
            val diff = message.length - smoothedText.length
            val charsToAdd = when {
                diff > 100 -> 12
                diff > 50 -> 6
                diff > 20 -> 3
                diff > 10 -> 2
                else -> 1
            }
            val delayTime = when {
                diff > 50 -> 10L
                diff > 20 -> 15L
                diff > 10 -> 20L
                else -> 25L
            }
            val nextLength = (smoothedText.length + charsToAdd).coerceAtMost(message.length)
            smoothedText = message.substring(0, nextLength)
            delay(delayTime)
        }
    }

    Column(modifier = modifier.animateContentSize()) {
        if (isEnableEditMessageMode && isSelected) {
            var internalText by remember { mutableStateOf(selectedMessagesText?.trimEnd() ?: "") }
            BasicTextField(
                value = internalText,
                onValueChange = { internalText = it },
                modifier = Modifier
                    .wrapContentSize()
                    .focusRequester(focusRequester),
                textStyle = commonTextStyle,
                minLines = minLines,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .padding(
                                start = 8.dp,
                                top = 8.dp,
                                bottom = 8.dp,
                                end = 8.3.dp
                            )
                    ) {
                        innerTextField()
                    }
                }
            )
            LaunchedEffect(isEnableEditMessageMode, isSelected, isDefaultFocusRequester) {
                if (isEnableEditMessageMode && isSelected && isDefaultFocusRequester) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                } else {
                    focusManager.clearFocus()
                }
            }
            LaunchedEffect(internalText) {
                val trimmedText = internalText.trimEnd()
                if (trimmedText != selectedMessagesText) {
                    onEditMessageText(trimmedText)
                }
            }
        } else {
            if (message.isNotEmpty()) {
                // Если нужно чуть отступить от края баббла, если есть картинки
                Spacer(Modifier.size(1.dp))

                val currentBlocks = remember(textItem.parsedBlocks, smoothedText, isStreaming) {
                    if (isStreaming) {
                        getTrimmedBlocks(textItem.parsedBlocks, smoothedText.length)
                    } else {
                        textItem.parsedBlocks
                    }
                }

                when (chatRoleplayPreset) {
                    ChatRoleplayPreset.STANDARD_BUBBLE -> {
                        StandardBubbleContent(
                            smoothedText = smoothedText,
                            paddedMessage = paddedMessage,
                            isStreaming = isStreaming,
                            cursorAlpha = cursorAlpha,
                            commonTextStyle = commonTextStyle,
                            markdownPaddingValues = markdownPaddingValues,
                            markdownTypographyValues = markdownTypographyValues,
                            markdownColorValues = markdownColorValues,
                            colors = colors
                        )
                    }
                    ChatRoleplayPreset.ROLEPLAY_CLASSIC -> {
                        RoleplayClassicPreset(
                            blocks = currentBlocks,
                            textColor = textColor,
                            isStreaming = isStreaming
                        )
                    }
                    ChatRoleplayPreset.VISUAL_NOVEL -> {
                        VisualNovelPreset(
                            blocks = currentBlocks,
                            textColor = textColor,
                            isStreaming = isStreaming
                        )
                    }
                    ChatRoleplayPreset.CYBERPUNK_LOG -> {
                        CyberpunkLogPreset(
                            blocks = currentBlocks,
                            textColor = textColor,
                            isStreaming = isStreaming
                        )
                    }
                    ChatRoleplayPreset.CINEMATIC_SCRIPT -> {
                        CinematicScriptPreset(
                            blocks = currentBlocks,
                            textColor = textColor,
                            isStreaming = isStreaming
                        )
                    }
                    ChatRoleplayPreset.RPG_BOARD_GAME -> {
                        RpgBoardgamePreset(
                            blocks = currentBlocks,
                            textColor = textColor,
                            isStreaming = isStreaming
                        )
                    }
                    ChatRoleplayPreset.MINIMAL_CARD -> {
                        MinimalCardPreset(
                            blocks = currentBlocks,
                            textColor = textColor,
                            isStreaming = isStreaming
                        )
                    }
                    ChatRoleplayPreset.INTIMATE_GLOW -> {
                        IntimateGlowPreset(
                            blocks = currentBlocks,
                            textColor = textColor,
                            isStreaming = isStreaming
                        )
                    }
                }
            }
        }
    }
}

private fun getTrimmedBlocks(blocks: List<RpgMessageBlock>, limit: Int): List<RpgMessageBlock> {
    var accumulatedLength = 0
    val result = mutableListOf<RpgMessageBlock>()
    for (block in blocks) {
        if (accumulatedLength >= limit) break
        val remainingLength = limit - accumulatedLength
        if (block.text.length <= remainingLength) {
            result.add(block)
            accumulatedLength += block.text.length
        } else {
            val trimmedText = block.text.take(remainingLength)
            val trimmedBlock = when (block) {
                is RpgMessageBlock.Plain -> RpgMessageBlock.Plain(trimmedText)
                is RpgMessageBlock.Action -> RpgMessageBlock.Action(trimmedText)
                is RpgMessageBlock.Speech -> RpgMessageBlock.Speech(trimmedText)
                is RpgMessageBlock.Thought -> RpgMessageBlock.Thought(trimmedText)
                is RpgMessageBlock.Event -> RpgMessageBlock.Event(trimmedText)
            }
            result.add(trimmedBlock)
            break
        }
    }
    return result
}

@Composable
private fun StandardBubbleContent(
    smoothedText: String,
    paddedMessage: String,
    isStreaming: Boolean,
    cursorAlpha: Float,
    commonTextStyle: TextStyle,
    markdownPaddingValues: com.mikepenz.markdown.model.MarkdownPadding,
    markdownTypographyValues: com.mikepenz.markdown.model.MarkdownTypography,
    markdownColorValues: com.mikepenz.markdown.model.MarkdownColors,
    colors: com.ensodai.avalon.theme.AvalonColors
) {
    if (isStreaming) {
        val displayText = remember(smoothedText, cursorAlpha) {
            buildAnnotatedString {
                append(smoothedText)
                withStyle(
                    SpanStyle(
                        color = colors.neonAccent.copy(alpha = cursorAlpha)
                    )
                ) {
                    append("▎")
                }
            }
        }
        Text(
            text = displayText,
            style = commonTextStyle,
            modifier = Modifier
                .widthIn(min = 54.dp)
                .padding(start = 10.dp, end = 10.dp, bottom = 8.dp),
        )
    } else {
        key(paddedMessage) {
            val markdownState = rememberMarkdownState(
                content = paddedMessage,
                immediate = true
            )

            Markdown(
                markdownState = markdownState,
                modifier = Modifier
                    .widthIn(min = 54.dp)
                    .padding(start = 10.dp, end = 10.dp, bottom = 8.dp),
                padding = markdownPaddingValues,
                typography = markdownTypographyValues,
                colors = markdownColorValues,
            )
        }
    }
}

private fun String.preprocessMarkdownNewlines(): String {
    val lines = this.lines()
    var inCodeBlock = false
    return lines.joinToString("\n") { line ->
        val trimmed = line.trim()
        if (trimmed.startsWith("```")) {
            inCodeBlock = !inCodeBlock
            line
        } else if (inCodeBlock) {
            line
        } else {
            if (line.isEmpty() || line.endsWith("  ") || line.endsWith("\\") || trimmed.startsWith("#") || trimmed == "---" || trimmed == "___" || trimmed == "***") {
                line
            } else {
                line + "  "
            }
        }
    }
}
