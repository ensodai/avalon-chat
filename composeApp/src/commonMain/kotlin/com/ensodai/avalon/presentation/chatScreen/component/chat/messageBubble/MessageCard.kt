package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble


import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.rotate
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Lucide
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.domain.audio.TtsPlayerState
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.AudioBubbleSection
import coil3.compose.AsyncImage
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.avalon.domain.models.MessageContentModel
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.shared.avalon.chat.models.ImagePayload
import com.ensodai.shared.avalon.chat.models.AudioPayload
import com.ensodai.shared.avalon.chat.models.MessageStatus
import com.ensodai.shared.avalon.chat.models.StickerGiftPayload
import com.ensodai.shared.avalon.chat.models.SystemEventPayload
import com.ensodai.shared.avalon.chat.models.TextPayload
import com.ensodai.avalon.presentation.chatScreen.component.chat.ActionButton
import com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.remark.RemarkMessageItem
import com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.remark.RemarkPopUp
import com.ensodai.avalon.presentation.chatScreen.component.explosionEffect.ReactionsLayout
import com.ensodai.avalon.presentation.commonComponents.BaseItemAlphaAnimation
import com.ensodai.avalon.presentation.commonComponents.ShimmerAsyncImage
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.models.BubbleFill
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.utils.L
import kotlin.uuid.Uuid

import com.ensodai.avalon.presentation.chatScreen.viewState.MessageBodyUiItem
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import com.ensodai.avalon.domain.models.chat.RpgMessageBlock

@Composable
fun MessageCard(
    role: MessageRole,
    bodyItems: List<MessageBodyUiItem>,
    status: MessageStatus,
    bubbleStyle: BubbleStyle,
    shouldShowAnimatedText: Boolean,
    isEnableSelectedMessagesMode: Boolean,
    isEnableEditMessageMode: Boolean,
    selectedMessagesText: String?,
    selectedSystemInstructionText: String? = null,
    systemInstructionText: String? = null,
    isSelected: Boolean,
    rarity: Rarity,
    minLines: Int,
    isDefaultFocusRequester: Boolean,
    userReaction: String?,
    characterReaction: String?,
    userAvatarUrl: String?,
    characterAvatarUrl: String?,
    onEditMessageText: (String) -> Unit,
    onChangeEditedSystemInstruction: (String) -> Unit = {},
    onComplaintIconClick: () -> Unit,
    onOpenSettingsScreen: () -> Unit,
    onReactionClick: (String) -> Unit,
    onImageClick: (Uuid) -> Unit = {},
    actionsConfig: MessageActionsConfig = MessageActionsConfig.None,
    currentPlayingContentId: Uuid? = null,
    ttsPlayerState: TtsPlayerState = TtsPlayerState.Idle,
    voice: VoiceModel? = null,
    onPlaySpeech: (Uuid, VoiceModel?) -> Unit = { _, _ -> },
    onPauseSpeech: () -> Unit = {},
    generatingTtsMessageIds: Set<Uuid> = emptySet(),
    isStreaming: Boolean = false,
    chatRoleplayPreset: ChatRoleplayPreset,
    showTtsButton: Boolean = true
) {
    val isOutgoing = role == MessageRole.USER
    val hasReactions = userReaction != null || characterReaction != null
    val paddingAnimationSpec = spring<Dp>(dampingRatio = 0.82f, stiffness = 800f)
    val minWidthValue = when {
        (userReaction != null && characterReaction != null) && (userReaction == characterReaction) -> 92.dp

        isOutgoing && userReaction == null && characterReaction == null -> 92.dp
        !isOutgoing && userReaction == null && characterReaction == null -> 72.dp
        isOutgoing && userReaction != null && characterReaction != null -> 152.dp
        !isOutgoing && userReaction != null && characterReaction != null -> 92.dp
        else -> 92.dp
    }

    val minBubbleWidth by animateDpAsState(
        targetValue = if (hasReactions) minWidthValue else 0.dp,
        label = "BubbleWidthExpansion"
    )
    val animatedPaddingBottom by animateDpAsState(
        targetValue = if (hasReactions && !isEnableEditMessageMode) 36.dp else 0.dp,
        animationSpec = paddingAnimationSpec,
        label = "BubbleExpansion"
    )

    val reactionAlignment = if (isOutgoing) Alignment.BottomStart else Alignment.BottomEnd
    val reactionOffset =
        if (isOutgoing) Modifier.offset(x = 8.dp, y = (-8).dp) else Modifier.offset(
            x = (-8).dp,
            y = (-8).dp
        )
    val bubblePadding = remember(role, chatRoleplayPreset) {
        if (chatRoleplayPreset != ChatRoleplayPreset.STANDARD_BUBBLE && role != MessageRole.SYSTEM) {
            if (role == MessageRole.USER) {
                PaddingValues(start = 48.dp, end = 12.dp)
            } else {
                PaddingValues(start = 12.dp, end = 24.dp)
            }
        } else {
            when {
                role == MessageRole.USER -> PaddingValues(start = 70.dp, end = 12.dp)
                role == MessageRole.MODEL -> PaddingValues(start = 12.dp, end = 70.dp)
                else -> PaddingValues(start = 12.dp, end = 70.dp)
            }
        }
    }

    val colors = AvalonTheme.colors
    val glowModifier = when {
        chatRoleplayPreset != ChatRoleplayPreset.STANDARD_BUBBLE && role != MessageRole.SYSTEM -> Modifier
        bubbleStyle.glowConfig != null && !isSelected -> {
            Modifier.border(1.dp, bubbleStyle.glowConfig.glowColor, bubbleStyle.shape)
        }

        colors.botBubbleBorder != Color.Transparent && role == MessageRole.MODEL -> {
            Modifier.border(0.5.dp, colors.botBubbleBorder, bubbleStyle.shape)
        }
        
        colors.userBubbleBorder != Color.Transparent && role == MessageRole.USER -> {
            Modifier.border(0.5.dp, colors.userBubbleBorder, bubbleStyle.shape)
        }

        else -> Modifier
    }

    val isStickerGiftOnly = remember(bodyItems) {
        bodyItems.size == 1 && bodyItems.first() is MessageBodyUiItem.StickerGift
    }
    val textItem = remember(bodyItems) {
        bodyItems.filterIsInstance<MessageBodyUiItem.Text>().firstOrNull()
    }
    val displayAsAudio = remember(bodyItems) {
        bodyItems.any { it is MessageBodyUiItem.Audio }
    }
    
    val showExternalPlayButton = role == MessageRole.MODEL && textItem != null && !displayAsAudio && !isEnableEditMessageMode && showTtsButton

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = bubbleStyle.alignment
    ) {
        // Если это только гифт — не показываем пузырёк, показываем только MessageGift
        if (isStickerGiftOnly) {
            val giftItem = bodyItems.first() as? MessageBodyUiItem.StickerGift
            Box {
                MessageGift(
                    url = giftItem?.url,
                    modifier = Modifier
                        .size(140.dp)
                )

                // СТАТУС (ГАЛОЧКИ) - только для исходящих сообщений
                if (role == MessageRole.USER && !isEnableEditMessageMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 6.dp, bottom = 4.dp)
                    ) {
                        MessageStatusIndicator(status = status, onClick = {})
                    }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.Bottom) {
                RemarkPopUp(
                    isVisible = role == MessageRole.USER && systemInstructionText != null && !isEnableEditMessageMode,
                    text = systemInstructionText.orEmpty()
                )

                Box(
                    modifier = Modifier.weight(1f, fill = false),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .padding(bubblePadding)
                            .then(glowModifier)
                    ) {
                        Box(
                            modifier = Modifier
                                .bubbleFillBackground(
                                    fill = bubbleStyle.backgroundFill,
                                    shape = bubbleStyle.shape
                                )
                                .clip(bubbleStyle.shape)
                        ) {
                            Spacer(modifier = Modifier.width(minBubbleWidth))

                            Column(
                                modifier = Modifier.padding(
                                    bottom = animatedPaddingBottom.coerceAtLeast(
                                        0.dp
                                    )
                                )
                            ) {
                                MessageCardBody(
                                    bodyItems = bodyItems,
                                    role = role,
                                    shouldShowAnimatedText = shouldShowAnimatedText,
                                    isEnableEditMessageMode = isEnableEditMessageMode,
                                    isDefaultFocusRequester = isDefaultFocusRequester,
                                    selectedMessagesText = selectedMessagesText,
                                    isSelected = isSelected,
                                    minLines = minLines,
                                    onEditMessageText = onEditMessageText,
                                    onOpenSettingsScreen = onOpenSettingsScreen,
                                    onImageClick = onImageClick,
                                    currentPlayingContentId = currentPlayingContentId,
                                    ttsPlayerState = ttsPlayerState,
                                    voice = voice,
                                    onPlaySpeech = onPlaySpeech,
                                    onPauseSpeech = onPauseSpeech,
                                    textContentId = textItem?.textId,
                                    isStreaming = isStreaming,
                                    chatRoleplayPreset = chatRoleplayPreset
                                )
                            }

                            // СТАТУС (ГАЛОЧКИ)
                            if (role == MessageRole.USER && !isEnableEditMessageMode) {
                                Box(
                                    modifier = Modifier.align(Alignment.BottomEnd)
                                        .padding(end = 6.dp, bottom = 4.dp)
                                ) {
                                    MessageStatusIndicator(status = status, onClick = {})
                                }
                            }
                        }

                        WarningIcon(
                            modifier = Modifier
                                .size(24.dp)
                                .offset(x = 28.dp, y = 2.dp)
                                .align(Alignment.TopEnd),
                            isEnabled = isEnableSelectedMessagesMode && role == MessageRole.MODEL,
                            onClick = { onComplaintIconClick() }
                        )

                        if (!isEnableEditMessageMode) {
                            ReactionsLayout(
                                modifier = Modifier
                                    .align(reactionAlignment)
                                    .then(reactionOffset),
                                userReaction = userReaction,
                                characterReaction = characterReaction,
                                userAvatarUrl = userAvatarUrl,
                                characterAvatarUrl = characterAvatarUrl,
                                onReactionClick = onReactionClick,
                            )
                        }
                    }

                    if (showExternalPlayButton && textItem != null) {
                        val isCurrentLoading = currentPlayingContentId == textItem.textId && ttsPlayerState is TtsPlayerState.Loading
                        
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 4.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.textMain.copy(alpha = 0.05f))
                                .border(1.dp, colors.neonAccent.copy(alpha = 0.2f), CircleShape)
                                .clickable {
                                    onPlaySpeech(textItem.textId, voice)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCurrentLoading) {
                                val infiniteTransition = rememberInfiniteTransition(label = "TtsLoader")
                                val rotation by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 360f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1200, easing = LinearEasing),
                                        repeatMode = RepeatMode.Restart
                                    ),
                                    label = "TtsLoaderRotation"
                                )
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .rotate(rotation),
                                    color = colors.neonAccent,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Lucide.Play,
                                    contentDescription = "Play",
                                    tint = colors.neonAccent,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .offset(x = 1.dp) // Оптическое центрирование
                                )
                            }
                        }
                    }
                }
            }
        }
        RemarkMessageItem(
            isVisible = isEnableEditMessageMode && isSelected && systemInstructionText != null,
            selectedSystemInstructionText = selectedSystemInstructionText,
            onChangeEditedSystemInstruction = { onChangeEditedSystemInstruction(it) }
        )

        when (actionsConfig) {
            is MessageActionsConfig.None -> Unit
            is MessageActionsConfig.SingleAction -> {
                Spacer(modifier = Modifier.height(8.dp))
                ActionButton(
                    onActionButtonClick = actionsConfig.onClick,
                    actionButtonColor = actionsConfig.actionColor ?: bubbleStyle.actionButtonColor,
                    actionButtonText = actionsConfig.text
                )
            }
            is MessageActionsConfig.DoubleAction -> {
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    ActionButton(
                        onActionButtonClick = actionsConfig.primaryClick,
                        actionButtonColor = actionsConfig.primaryColor ?: bubbleStyle.actionButtonColor,
                        actionButtonText = actionsConfig.primaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ActionButton(
                        onActionButtonClick = actionsConfig.secondaryClick,
                        actionButtonColor = actionsConfig.secondaryColor ?: bubbleStyle.actionButtonColor,
                        actionButtonText = actionsConfig.secondaryText
                    )
                }
            }
            is MessageActionsConfig.FeedbackAction -> {
                Spacer(modifier = Modifier.height(8.dp))
                OutreachFeedbackButtons(
                    onSendOutreachFeedback = { isPositive ->
                        if (isPositive) actionsConfig.onPositiveClick() else actionsConfig.onNegativeClick()
                    }
                )
            }
        }
    }
}

private fun Modifier.bubbleFillBackground(
    fill: BubbleFill,
    shape: Shape
): Modifier {
    return when (fill) {
        is BubbleFill.Solid -> background(color = fill.color, shape = shape)
        is BubbleFill.Gradient -> background(
            brush = Brush.linearGradient(
                colors = listOf(fill.startColor, fill.endColor)
            ),
            shape = shape
        )
    }
}

@Composable
private fun MessageCardBody(
    bodyItems: List<MessageBodyUiItem>,
    role: MessageRole,
    shouldShowAnimatedText: Boolean,
    isEnableEditMessageMode: Boolean,
    isDefaultFocusRequester: Boolean,
    selectedMessagesText: String?,
    isSelected: Boolean,
    minLines: Int,
    onEditMessageText: (String) -> Unit,
    onOpenSettingsScreen: () -> Unit,
    onImageClick: (Uuid) -> Unit,
    currentPlayingContentId: Uuid?,
    ttsPlayerState: TtsPlayerState,
    voice: VoiceModel?,
    onPlaySpeech: (Uuid, VoiceModel?) -> Unit,
    onPauseSpeech: () -> Unit,
    textContentId: Uuid?,
    isStreaming: Boolean = false,
    chatRoleplayPreset: ChatRoleplayPreset
) {
    val hasAudio = remember(bodyItems) { bodyItems.any { it is MessageBodyUiItem.Audio } }
    var isTextExpanded by remember { mutableStateOf(false) }
    val colors = AvalonTheme.colors

    bodyItems.forEach { item ->
        when (item) {
            is MessageBodyUiItem.Text -> {
                if (hasAudio) return@forEach // Если уже есть аудио, рисуем ВМЕСТО текстового баббла голосовое сообщение (текст скрывается)
                val shouldShowSpoiler = hasAudio && item.rawText.length > 120
                
                Column {
                    MessageContent(
                        modifier = Modifier,
                        role = role,
                        textItem = item,
                        chatRoleplayPreset = chatRoleplayPreset,
                        shouldShowAnimatedText = shouldShowAnimatedText,
                        isEnableEditMessageMode = isEnableEditMessageMode,
                        isDefaultFocusRequester = isDefaultFocusRequester,
                        selectedMessagesText = selectedMessagesText,
                        isSelected = isSelected,
                        minLines = minLines,
                        onEditMessageText = onEditMessageText,
                        onOpenSettingsScreen = onOpenSettingsScreen,
                    )

                    if (shouldShowSpoiler) {
                        Row(
                            modifier = Modifier
                                .clickable { isTextExpanded = !isTextExpanded }
                                .padding(start = 10.dp, end = 10.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isTextExpanded) "Свернуть описание" else "Показать полностью",
                                color = colors.neonAccent,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isTextExpanded) IconsManager.KeyboardArrowUp else IconsManager.KeyboardArrowDown,
                                contentDescription = null,
                                tint = colors.neonAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            is MessageBodyUiItem.Audio -> {
                Column {
                    AudioBubbleSection(
                        contentId = textContentId ?: item.audioId,
                        audioPayload = item.payload,
                        currentPlayingContentId = currentPlayingContentId,
                        ttsPlayerState = ttsPlayerState,
                        voice = voice,
                        onPlaySpeech = onPlaySpeech,
                        onPauseSpeech = onPauseSpeech,
                        showExpandButton = textContentId != null,
                        isTextExpanded = isTextExpanded,
                        onExpandToggle = { isTextExpanded = !isTextExpanded }
                    )

                    val textItem = remember(bodyItems) {
                        bodyItems.filterIsInstance<MessageBodyUiItem.Text>().firstOrNull()
                    }

                    if (textItem != null) {
                        AnimatedVisibility(
                            visible = isTextExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, start = 12.dp, end = 12.dp, bottom = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(0.5.dp)
                                        .background(colors.textMain.copy(alpha = 0.08f))
                                ).let { }
                                Spacer(modifier = Modifier.height(8.dp))
                                MessageContent(
                                    modifier = Modifier,
                                    role = role,
                                    textItem = textItem,
                                    chatRoleplayPreset = chatRoleplayPreset,
                                    shouldShowAnimatedText = false,
                                    isEnableEditMessageMode = isEnableEditMessageMode,
                                    isDefaultFocusRequester = isDefaultFocusRequester,
                                    selectedMessagesText = selectedMessagesText,
                                    isSelected = isSelected,
                                    minLines = minLines,
                                    onEditMessageText = onEditMessageText,
                                    onOpenSettingsScreen = onOpenSettingsScreen,
                                )
                            }
                        }
                    }
                }
            }

            is MessageBodyUiItem.Image -> {
                MessageImage(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    messageContentId = item.imageId,
                    onImageClick = onImageClick,
                    imageContent = item.payload
                )
            }

            is MessageBodyUiItem.SystemEvent -> {
                val systemInfo = resolveSystemMessageContent(item.systemEventType, item.systemEventId)
                val systemTextItem = remember(systemInfo) {
                    MessageBodyUiItem.Text(
                        textId = item.systemEventId,
                        rawText = systemInfo.text,
                        parsedBlocks = listOf(RpgMessageBlock.Plain(systemInfo.text)),
                        isStreaming = false
                    )
                }
                MessageContent(
                    modifier = Modifier,
                    role = role,
                    textItem = systemTextItem,
                    chatRoleplayPreset = ChatRoleplayPreset.STANDARD_BUBBLE,
                    shouldShowAnimatedText = false,
                    isEnableEditMessageMode = false,
                    isDefaultFocusRequester = false,
                    selectedMessagesText = null,
                    isSelected = false,
                    minLines = 1,
                    onEditMessageText = {},
                    onOpenSettingsScreen = onOpenSettingsScreen
                )
            }

            is MessageBodyUiItem.StickerGift -> Unit
        }
    }
}

@Composable
fun MessageImage(
    modifier: Modifier = Modifier,
    onImageClick: (Uuid) -> Unit = {},
    messageContentId: Uuid,
    imageContent: ImagePayload
) {
    ShimmerAsyncImage(
        modifier = modifier
            // animateContentSize() здесь можно оставить, но по факту он больше
            // не нужен, так как размер прыгать перестанет!
            .clickable { onImageClick(messageContentId) },
        model = imageContent.url,
        blurhash = imageContent.blurhash,
        seed = imageContent.url,
        imageWidth = imageContent.width,
        imageHeight = imageContent.height,
        contentScale = ContentScale.Crop
    )
}

@Composable
fun MessageGift(
    url: String?,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = url,
        contentDescription = "Gift",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}


@Composable
private fun WarningIcon(
    modifier: Modifier,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val colors = AvalonTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val customRipple = ripple(
        bounded = false,
        radius = 24.dp,
    )
    Box(modifier) {
        BaseItemAlphaAnimation(isVisible = isEnabled) {
            Icon(
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = customRipple,
                        onClick = { onClick() }
                    ),
                imageVector = IconsManager.Report,
                contentDescription = "Исключения",
                tint = colors.error
            )
        }
    }
}

class MessageBubbleWithTailShape(
    private val cornerRadius: Dp = 16.dp,
    private val tailSize: Dp = 8.dp,
    private val isOutgoing: Boolean
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = createBubblePath(size, density)
        return Outline.Generic(path)
    }

    private fun createBubblePath(size: Size, density: Density): Path {
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }
        val tailSizePx = with(density) { tailSize.toPx() }
        val w = size.width
        val h = size.height
        val r = cornerRadiusPx
        val tailHorizontalScale = 1f
        val tailVerticalScale = 2.5f
        val adjustedTailSizeX = tailSizePx * tailHorizontalScale
        val adjustedTailSizeY = tailSizePx * tailVerticalScale

        return Path().apply {
            if (isOutgoing) {
                moveTo(r, 0f)
                lineTo(w - r, 0f)
                arcTo(Rect(w - 2 * r, 0f, w, 2 * r), 270f, 90f, false)
                lineTo(w, h - adjustedTailSizeY)
                quadraticTo(
                    x1 = w,
                    y1 = h,
                    x2 = w + adjustedTailSizeX,
                    y2 = h
                )
                lineTo(r, h)
                arcTo(Rect(0f, h - 2 * r, 2 * r, h), 90f, 90f, false)
                lineTo(0f, r)
                arcTo(Rect(0f, 0f, 2 * r, 2 * r), 180f, 90f, false)
                close()
            } else {
                moveTo(0f, r)
                arcTo(Rect(0f, 0f, 2 * r, 2 * r), 180f, 90f, false)
                lineTo(w - r, 0f)
                arcTo(Rect(w - 2 * r, 0f, w, 2 * r), 270f, 90f, false)
                lineTo(w, h - r)
                arcTo(Rect(w - 2 * r, h - 2 * r, w, h), 0f, 90f, false)
                lineTo(adjustedTailSizeX, h)
                lineTo(-adjustedTailSizeX, h)
                quadraticTo(
                    x1 = 0f,
                    y1 = h,
                    x2 = 0f,
                    y2 = h - adjustedTailSizeY
                )
                close()
            }
        }
    }
}

@Composable
fun OutreachFeedbackButtons(
    onSendOutreachFeedback: (isPositive: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val colors = AvalonTheme.colors
        
        // Кнопка 👍 Интересно
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.textMain.copy(alpha = 0.08f))
                .border(1.dp, colors.neonAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .clickable { onSendOutreachFeedback(true) }
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "👍 Интересно",
                color = colors.textMain,
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Кнопка 👎 Нет
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.textMain.copy(alpha = 0.08f))
                .border(1.dp, colors.error.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .clickable { onSendOutreachFeedback(false) }
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "👎 Нет",
                color = colors.textMain,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
