package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.SystemEventType
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.avalon.domain.models.MessageContainerModel
import com.ensodai.avalon.domain.models.ChatMessageModel
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.shared.avalon.chat.models.MessageStatus
import com.ensodai.shared.avalon.chat.models.SystemEventPayload
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.some_message_error
import com.ensodai.avalon.generated.resources.sys_btn_ai_settings
import com.ensodai.avalon.generated.resources.system_message_blocked
import com.ensodai.avalon.generated.resources.system_message_custom_context_length_exceeded
import com.ensodai.avalon.generated.resources.system_message_custom_guardrails_restricted
import com.ensodai.avalon.generated.resources.system_message_custom_insufficient_credits
import com.ensodai.avalon.generated.resources.system_message_custom_key_invalid
import com.ensodai.avalon.generated.resources.system_message_custom_model_missing
import com.ensodai.avalon.generated.resources.system_message_custom_model_not_found
import com.ensodai.avalon.generated.resources.system_message_custom_provider_unavailable
import com.ensodai.avalon.generated.resources.system_message_custom_rate_limit
import com.ensodai.avalon.generated.resources.system_message_image_person_blocked
import com.ensodai.avalon.generated.resources.system_message_image_safety_blocked
import com.ensodai.avalon.generated.resources.system_message_quota_exceeded
import com.ensodai.avalon.generated.resources.system_message_server_error
import com.ensodai.avalon.generated.resources.system_message_user_banned
import com.ensodai.avalon.generated.resources.unsupported_system_message
import com.ensodai.avalon.presentation.chatScreen.component.chat.ReactionPickerContent
import com.ensodai.avalon.presentation.chatScreen.component.chat.SmartReactionPositionProvider
import com.ensodai.avalon.presentation.commonComponents.toastNotify
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.models.BubbleFill
import com.ensodai.avalon.theme.models.BubbleShapeType
import com.ensodai.avalon.theme.IconsManager
import com.ensodai.avalon.utils.EMPTY_STRING
import org.jetbrains.compose.resources.stringResource
import kotlin.math.absoluteValue
import kotlin.uuid.Uuid

import com.ensodai.avalon.domain.audio.TtsPlayerState
import com.ensodai.shared.avalon.liveCall.models.VoiceModel

import com.ensodai.avalon.presentation.chatScreen.viewState.MessageContainerUiState
import com.ensodai.avalon.presentation.chatScreen.viewState.MessageActionsConfigUiState
import com.ensodai.avalon.presentation.chatScreen.viewState.MessageBodyUiItem
import com.ensodai.avalon.theme.models.ChatRoleplayPreset

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    uiState: MessageContainerUiState,
    preset: ChatRoleplayPreset,
    modifier: Modifier,
    selectedMessagesText: String?,
    selectedSystemInstructionText: String? = null,
    isSelected: Boolean,
    isEnableSelectedMessagesMode: Boolean,
    isEnableEditMessageMode: Boolean,
    shouldShowAnimatedText: Boolean,
    highlightQuery: String,
    minLines: Int = 1,
    isEnableSelectMessage: Boolean,
    isDefaultFocusRequester: Boolean = true,
    userReaction: String?,
    characterReaction: String?,
    userAvatarUrl: String?,
    characterAvatarUrl: String?,
    showReactionPicker: Boolean,
    reactionPickerOffset: Offset?,
    onShowReactionMenu: (Offset) -> Unit,
    onDismissReactionMenu: () -> Unit,
    onToggleReaction: (reaction: String?) -> Unit,
    onEditMessageText: (String) -> Unit,
    onChangeEditedSystemInstruction: (String) -> Unit = {},
    onSelectMessage: (MessageContainerModel) -> Unit,
    onEnableSelectedMessagesMode: (MessageContainerModel) -> Unit,
    onComplaintIconClick: () -> Unit,
    onOpenSettingsScreen: () -> Unit,
    onActionButtonClick: (systemEventType: SystemEventType?) -> Unit,
    onBuyEnergyClick: () -> Unit = {},
    onImageClick: (Uuid) -> Unit = {},
    onSendOutreachFeedback: (isPositive: Boolean) -> Unit = {},
    currentPlayingContentId: Uuid? = null,
    ttsPlayerState: TtsPlayerState = TtsPlayerState.Idle,
    voice: VoiceModel? = null,
    onPlaySpeech: (Uuid, VoiceModel?) -> Unit = { _, _ -> },
    onPauseSpeech: () -> Unit = {},
    generatingTtsMessageIds: Set<Uuid> = emptySet(),
    slot: ChatMessageModel
) {
    val haptic = LocalHapticFeedback.current
    val hasSystemEventPayload = remember(uiState.bodyItems) {
        uiState.bodyItems.any { it is MessageBodyUiItem.SystemEvent }
    }
    val systemEventPayload = remember(uiState.bodyItems) {
        uiState.bodyItems.filterIsInstance<MessageBodyUiItem.SystemEvent>().firstOrNull()
    }
    val displayAsAudio = remember(uiState.bodyItems) {
        uiState.bodyItems.any { it is MessageBodyUiItem.Audio }
    }

    val bubbleStyle = rememberBubbleStyle(
        role = uiState.role,
        rarity = uiState.rarity,
        systemEventType = systemEventPayload?.systemEventType,
        hasAudio = displayAsAudio,
        preset = preset
    )
    val systemInfo = systemEventPayload?.let {
        resolveSystemMessageContent(it.systemEventType, uiState.id)
    }

    val premiumAccent = AvalonTheme.colors.premiumAccent
    val neonAccent = AvalonTheme.colors.neonAccent

    val actionsConfig = remember(
        uiState.actionsConfig, 
        systemInfo, 
        onSendOutreachFeedback, 
        onActionButtonClick, 
        onBuyEnergyClick, 
        premiumAccent,
        neonAccent
    ) {
        when (val config = uiState.actionsConfig) {
            is MessageActionsConfigUiState.Feedback -> {
                MessageActionsConfig.FeedbackAction(
                    onPositiveClick = { onSendOutreachFeedback(true) },
                    onNegativeClick = { onSendOutreachFeedback(false) }
                )
            }
            is MessageActionsConfigUiState.Double -> {
                MessageActionsConfig.DoubleAction(
                    primaryText = systemInfo?.buttonText.orEmpty(),
                    primaryClick = { onActionButtonClick(config.systemEventType) },
                    secondaryText = "",
                    secondaryClick = onBuyEnergyClick,
                    primaryColor = premiumAccent,
                    secondaryColor = neonAccent
                )
            }
            is MessageActionsConfigUiState.Single -> {
                MessageActionsConfig.SingleAction(
                    text = systemInfo?.buttonText.orEmpty(),
                    onClick = { onActionButtonClick(config.systemEventType) },
                    actionColor = premiumAccent
                )
            }
            else -> MessageActionsConfig.None
        }
    }

    val activeContainer = slot.activeContainer ?: slot.containers.first()

    CustomBox(
        modifier = modifier,
        isSelected = isSelected,
        startColor = Color.Transparent,
        endColor = AvalonTheme.colors.neonAccent,
        isEnableSelectedMessagesMode = isEnableSelectedMessagesMode,
        isEnableEditMessageMode = isEnableEditMessageMode,
        role = uiState.role,
        isEnableSelectMessage = isEnableSelectMessage,
        onLongClick = {
            if (!isEnableSelectedMessagesMode) onEnableSelectedMessagesMode(activeContainer)
        },
        onClick = { offset ->
            if (!isEnableEditMessageMode && isEnableSelectMessage) onSelectMessage(activeContainer)
            if (!isEnableEditMessageMode && !isEnableSelectMessage) onShowReactionMenu(offset)
        },
        onDoubleClick = {
            if (!isEnableEditMessageMode && !isEnableSelectedMessagesMode) {
                if (!hasSystemEventPayload) {
                    onToggleReaction("❤️")
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                }
            }
        },
        actionButtons = { /* твои кнопки */ },
        circleCheck = {
            AnimateCircleCheckMessage(
                isSelected = isSelected,
                isShow = isEnableSelectedMessagesMode && !isEnableEditMessageMode
            )
        }
    ) {
        MessageCard(
            role = uiState.role,
            bodyItems = uiState.bodyItems,
            status = uiState.status,
            bubbleStyle = bubbleStyle,
            shouldShowAnimatedText = shouldShowAnimatedText,
            isEnableSelectedMessagesMode = isEnableSelectedMessagesMode,
            isEnableEditMessageMode = isEnableEditMessageMode,
            selectedMessagesText = selectedMessagesText,
            selectedSystemInstructionText = selectedSystemInstructionText,
            systemInstructionText = uiState.systemInstructionText,
            isSelected = isSelected,
            rarity = uiState.rarity,
            minLines = minLines,
            isDefaultFocusRequester = isDefaultFocusRequester,
            userReaction = userReaction,
            characterReaction = characterReaction,
            userAvatarUrl = userAvatarUrl,
            characterAvatarUrl = characterAvatarUrl,
            onEditMessageText = onEditMessageText,
            onChangeEditedSystemInstruction = onChangeEditedSystemInstruction,
            onComplaintIconClick = onComplaintIconClick,
            onOpenSettingsScreen = onOpenSettingsScreen,
            onReactionClick = onToggleReaction,
            onImageClick = onImageClick,
            actionsConfig = actionsConfig,
            currentPlayingContentId = currentPlayingContentId,
            ttsPlayerState = ttsPlayerState,
            voice = voice,
            onPlaySpeech = onPlaySpeech,
            onPauseSpeech = onPauseSpeech,
            generatingTtsMessageIds = generatingTtsMessageIds,
            isStreaming = uiState.isStreaming,
            chatRoleplayPreset = preset
        )
    }

    if (showReactionPicker && reactionPickerOffset != null) {
        val density = LocalDensity.current

        val positionProvider = remember(reactionPickerOffset, density) {
            SmartReactionPositionProvider(
                touchOffset = reactionPickerOffset,
                density = density
            )
        }

        Popup(
            popupPositionProvider = positionProvider,
            onDismissRequest = { onDismissReactionMenu() },
            properties = PopupProperties(
                focusable = false,
                dismissOnClickOutside = true
            )
        ) {
            ReactionPickerContent(
                onEmojiSelected = { emoji ->
                    onToggleReaction(emoji)
                    onDismissReactionMenu()
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                }
            )
        }
    }
}


data class SystemMessageContent(
    val text: String,
    val buttonText: String? = null,
    val systemEventType: SystemEventType? = null
)

@Composable
fun resolveSystemMessageContent(
    systemEventType: SystemEventType,
    seedId: Uuid
): SystemMessageContent {
    return when (systemEventType) {
        SystemEventType.QUOTA_EXCEEDED -> SystemMessageContent(
            text = stringResource(Res.string.system_message_quota_exceeded),
            buttonText = stringResource(Res.string.sys_btn_ai_settings),
            systemEventType = SystemEventType.QUOTA_EXCEEDED
        )

        SystemEventType.CUSTOM_MODEL_MISSING -> SystemMessageContent(
            text = stringResource(Res.string.system_message_custom_model_missing),
            buttonText = stringResource(Res.string.sys_btn_ai_settings),
            systemEventType = systemEventType
        )

        SystemEventType.CUSTOM_KEY_INVALID -> SystemMessageContent(
            text = stringResource(Res.string.system_message_custom_key_invalid),
            buttonText = stringResource(Res.string.sys_btn_ai_settings),
            systemEventType = systemEventType
        )

        SystemEventType.CUSTOM_INSUFFICIENT_CREDITS -> SystemMessageContent(
            text = stringResource(Res.string.system_message_custom_insufficient_credits),
            buttonText = stringResource(Res.string.sys_btn_ai_settings),
            systemEventType = systemEventType
        )

        SystemEventType.CUSTOM_RATE_LIMIT -> SystemMessageContent(
            text = stringResource(Res.string.system_message_custom_rate_limit),
            buttonText = null,
            systemEventType = systemEventType
        )

        SystemEventType.CUSTOM_MODEL_NOT_FOUND -> SystemMessageContent(
            text = stringResource(Res.string.system_message_custom_model_not_found),
            buttonText = stringResource(Res.string.sys_btn_ai_settings),
            systemEventType = systemEventType
        )

        SystemEventType.CUSTOM_CONTEXT_LENGTH_EXCEEDED -> SystemMessageContent(
            text = stringResource(Res.string.system_message_custom_context_length_exceeded),
            buttonText = stringResource(Res.string.sys_btn_ai_settings),
            systemEventType = systemEventType
        )

        SystemEventType.CUSTOM_GUARDRAILS_RESTRICTED -> SystemMessageContent(
            text = stringResource(Res.string.system_message_custom_guardrails_restricted),
            buttonText = stringResource(Res.string.sys_btn_ai_settings),
            systemEventType = systemEventType
        )

        SystemEventType.CUSTOM_PROVIDER_UNAVAILABLE -> SystemMessageContent(
            text = stringResource(Res.string.system_message_custom_provider_unavailable),
            buttonText = stringResource(Res.string.sys_btn_ai_settings),
            systemEventType = systemEventType
        )

        SystemEventType.MESSAGE_BLOCKED -> SystemMessageContent(
            text = stringResource(Res.string.system_message_blocked),
            buttonText = null,
            systemEventType = systemEventType
        )

        SystemEventType.IMAGE_PERSON_BLOCKED -> SystemMessageContent(
            text = stringResource(Res.string.system_message_image_person_blocked),
            buttonText = null,
            systemEventType = systemEventType
        )

        SystemEventType.IMAGE_SAFETY_BLOCKED -> SystemMessageContent(
            text = stringResource(Res.string.system_message_image_safety_blocked),
            buttonText = null,
            systemEventType = systemEventType
        )

        SystemEventType.SERVER_ERROR -> SystemMessageContent(
            text = stringResource(Res.string.system_message_server_error),
            buttonText = null,
            systemEventType = systemEventType
        )

        SystemEventType.USER_BANNED -> SystemMessageContent(
            text = stringResource(Res.string.system_message_user_banned),
            buttonText = null,
            systemEventType = systemEventType
        )
    }
}

@Composable
fun rememberBubbleStyle(
    role: MessageRole,
    rarity: Rarity,
    systemEventType: SystemEventType? = null,
    hasAudio: Boolean = false,
    preset: ChatRoleplayPreset
): BubbleStyle {
    val colors = AvalonTheme.colors
    return remember(role, rarity, systemEventType, colors, hasAudio, preset) {
        val userFill = colors.userBubble
        val modelFill = colors.botBubble

        val quotaFill = BubbleFill.Solid(colors.premiumAccent.copy(alpha = 0.12f))
        val blockedFill = BubbleFill.Solid(colors.warningAccent.copy(alpha = 0.12f))
        val errorFill = BubbleFill.Solid(colors.error.copy(alpha = 0.12f))
        val defaultSystemFill = BubbleFill.Solid(colors.textMain.copy(alpha = 0.08f))
        val actionBtnColor = if (systemEventType == SystemEventType.QUOTA_EXCEEDED ||
            systemEventType == SystemEventType.CUSTOM_KEY_INVALID ||
            systemEventType == SystemEventType.CUSTOM_MODEL_MISSING) {
            colors.premiumAccent
        } else {
            null
        }
        val containerFill = when {
            preset != ChatRoleplayPreset.STANDARD_BUBBLE && role != MessageRole.SYSTEM -> BubbleFill.Solid(Color.Transparent)
            else -> when (role) {
                MessageRole.USER -> userFill
                MessageRole.SYSTEM -> when (systemEventType) {
                    SystemEventType.QUOTA_EXCEEDED,
                    SystemEventType.CUSTOM_KEY_INVALID,
                    SystemEventType.CUSTOM_MODEL_MISSING,
                    SystemEventType.CUSTOM_INSUFFICIENT_CREDITS,
                    SystemEventType.CUSTOM_RATE_LIMIT,
                    SystemEventType.CUSTOM_MODEL_NOT_FOUND,
                    SystemEventType.CUSTOM_CONTEXT_LENGTH_EXCEEDED,
                    SystemEventType.CUSTOM_GUARDRAILS_RESTRICTED,
                    SystemEventType.CUSTOM_PROVIDER_UNAVAILABLE -> quotaFill
                    SystemEventType.MESSAGE_BLOCKED -> blockedFill
                    SystemEventType.SERVER_ERROR -> errorFill
                    else -> defaultSystemFill
                }

                else -> modelFill
            }
        }

        val bubbleCornerRadius = colors.bubbleCornerRadius.dp
        val shape = when (role) {
            MessageRole.USER -> resolveBubbleShape(
                shapeType = if (hasAudio) BubbleShapeType.Rounded else colors.bubbleShapeType,
                cornerRadius = bubbleCornerRadius,
                isOutgoing = true
            )

            MessageRole.MODEL -> resolveBubbleShape(
                shapeType = if (hasAudio) BubbleShapeType.Rounded else colors.bubbleShapeType,
                cornerRadius = bubbleCornerRadius,
                isOutgoing = false
            )

            MessageRole.SYSTEM -> RoundedCornerShape(bubbleCornerRadius)
        }

        val contentPadding = when {
            preset != ChatRoleplayPreset.STANDARD_BUBBLE && role != MessageRole.SYSTEM -> PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
            else -> when (role) {
                MessageRole.USER -> PaddingValues(start = 70.dp, end = 12.dp)
                MessageRole.SYSTEM -> PaddingValues(start = 12.dp, end = 70.dp)
                else -> PaddingValues(start = 12.dp, end = 70.dp)
            }
        }

        val glowConfig = when {
            preset != ChatRoleplayPreset.STANDARD_BUBBLE && role != MessageRole.SYSTEM -> null
            else -> when (rarity) {
                Rarity.COMMON -> null
                Rarity.UNCOMMON -> GlowConfig(colors.rarityUncommonGlow.copy(alpha = 0.5f), 2.dp)
                Rarity.RARE -> GlowConfig(colors.rarityRareGlow.copy(alpha = 0.5f), 2.dp)
                Rarity.EPIC -> GlowConfig(colors.rarityEpicGlow.copy(alpha = 0.5f), 2.dp)
                Rarity.MYTHIC -> GlowConfig(colors.rarityMythicGlow.copy(alpha = 0.5f), 2.dp)
            }
        }
        val alignment = if (role == MessageRole.USER) Alignment.End else Alignment.Start

        BubbleStyle(
            backgroundFill = containerFill,
            shape = shape,
            padding = contentPadding,
            glowConfig = glowConfig,
            alignment = alignment,
            actionButtonColor = actionBtnColor
        )
    }
}

@Immutable
data class BubbleStyle(
    val backgroundFill: BubbleFill,
    val padding: PaddingValues,
    val glowConfig: GlowConfig?,
    val shape: Shape,
    val alignment: Alignment.Horizontal,
    val actionButtonColor: Color?
)

@Immutable
data class GlowConfig(
    val glowColor: Color,
    val glowRadius: Dp
)

@Composable
fun bubbleSelectionTint(
    isSelected: Boolean,
    isBackground: Boolean = false,
    startColor: Color,
    endColor: Color
): Color {
    val animatedColorBubble by animateColorAsState(
        targetValue = if (isSelected) endColor.copy(alpha = 0.2f) else startColor,
        animationSpec = tween(
            durationMillis = 600,
            easing = LinearOutSlowInEasing
        )
    )

    val animatedColorBackground by animateColorAsState(
        targetValue = if (isSelected) endColor.copy(alpha = 0.1f) else startColor,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearOutSlowInEasing
        )
    )

    return if (isBackground) animatedColorBackground else animatedColorBubble
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomBox(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    startColor: Color,
    endColor: Color,
    isEnableSelectedMessagesMode: Boolean,
    isEnableEditMessageMode: Boolean,
    isEnableSelectMessage: Boolean,
    role: MessageRole,
    onDoubleClick: () -> Unit,
    onLongClick: () -> Unit,
    onClick: (offset: Offset) -> Unit,
    actionButtons: @Composable BoxScope.() -> Unit,
    circleCheck: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val shouldRenderSelectionBackground = isEnableSelectMessage || isSelected
    val offsetXMessageBubble by animateDpAsState(
        targetValue = if (
            isEnableSelectedMessagesMode &&
            !isEnableEditMessageMode &&
            (role == MessageRole.MODEL ||
                    role == MessageRole.SYSTEM)
        ) {
            32.dp
        } else {
            0.dp
        }, label = "offsetXMessageBubble"
    )

    val interactionSource = remember { MutableInteractionSource() }

    var pressOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Press) {
                // Сохраняем позицию нажатия
                pressOffset = interaction.pressPosition
            }
        }
    }


    Box(
        modifier = modifier
            .selectionBackground(
                enabled = shouldRenderSelectionBackground,
                isSelected = isSelected && isEnableSelectMessage,
                startColor = startColor,
                endColor = endColor
            )
            .background(Color.Transparent)
            .fillMaxWidth()
//            .animateContentSize()
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onLongClick = onLongClick,
                onDoubleClick = onDoubleClick,
                onClick = {
                    onClick(pressOffset)
                }
            )
    ) {
        Column {
            Box {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetXMessageBubble.roundToPx(), 0) }
                ) {
                    content()
                }
                if (isEnableSelectMessage) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 2.dp)
                            .align(alignment = Alignment.BottomStart)
                    ) {
                        circleCheck()
                    }
                }
            }
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetXMessageBubble.roundToPx(), 0) }
                    .align(Alignment.Start)
            ) {
                actionButtons()
            }
        }
    }
}

@Composable
private fun Modifier.selectionBackground(
    enabled: Boolean,
    isSelected: Boolean,
    startColor: Color,
    endColor: Color
): Modifier {
    if (!enabled) return this

    val animatedColor = bubbleSelectionTint(
        isSelected = isSelected,
        isBackground = true,
        startColor = startColor,
        endColor = endColor
    )

    return drawBehind {
        drawRect(
            color = animatedColor,
            topLeft = Offset(0f, -4.dp.toPx()),
            size = Size(size.width, size.height + 8.dp.toPx())
        )
    }
}


/**
 * Модификатор, который рисует свечение по контуру заданной формы.
 *
 * @param shape Форма, вокруг которой будет рисоваться свечение.
 * @param glowColor Цвет свечения.
 * @param glowRadius Радиус размытия для эффекта свечения.
 * @param strokeWidth Ширина обводки, которая будет размыта.
 */
fun Modifier.glowBehindShape(
    shape: Shape,
    glowColor: Color,
    glowRadius: Dp,
    strokeWidth: Dp
): Modifier = this.drawBehind {
    glowRadius.toPx()
    val strokeWidthPx = strokeWidth.toPx()

    // Создаем контур для заданной формы
    val outline = shape.createOutline(
        size = size,
        layoutDirection = layoutDirection,
        density = this
    )

    val path = when (outline) {
        is Outline.Rectangle -> Path().apply { addRect(outline.rect) }
        is Outline.Rounded -> Path().apply { addRoundRect(outline.roundRect) }
        is Outline.Generic -> outline.path
    }

    // Рисуем свечение
    if (glowRadius > 0.dp) {
        val glowPaint = Paint().apply {
            this.style = PaintingStyle.Stroke // Рисуем только контур
            this.strokeWidth = strokeWidthPx   // Задаем толщину контура
            this.color = glowColor
        }
        drawPath(
            path = path,
            color = glowPaint.color,
            style = Stroke(width = glowPaint.strokeWidth)
        )
    }
}

@Composable
fun MessageStatusIndicator(
    status: MessageStatus,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val toastNotify = toastNotify()
    val icon = when (status) {
        is MessageStatus.PENDING,
        is MessageStatus.SENDING -> IconsManager.Schedule

        is MessageStatus.SENT -> IconsManager.DoneAll
        is MessageStatus.FAILED -> IconsManager.ErrorOutline
    }

    // Цвета: индикатор всегда на пузыре пользователя,
    // поэтому используем цвет текста пользовательского пузыря.
    val bubbleTextColor = AvalonTheme.colors.userBubbleTextColor
    val iconColor = when (status) {
        is MessageStatus.PENDING,
        is MessageStatus.SENDING -> bubbleTextColor.copy(alpha = 0.5f)

        is MessageStatus.SENT -> bubbleTextColor.copy(alpha = 0.8f)
        is MessageStatus.FAILED -> AvalonTheme.colors.error
    }

    val iconSize = 18.dp
    val errorText =
        if (status is MessageStatus.FAILED) getFriendlyErrorMessage(status.reason) else EMPTY_STRING

    Icon(
        imageVector = icon,
        contentDescription = status.toString(),
        tint = iconColor,
        modifier = modifier
            .size(iconSize)
            .then(
                if (status is MessageStatus.FAILED) {
                    Modifier.clickable {
                        toastNotify(errorText)
                        onClick()
                    }
                } else Modifier
            )
    )
}

@Composable
fun getFriendlyErrorMessage(reason: String): String {
    return stringResource(Res.string.some_message_error)
//    return when {
//        reason.contains("chat_not_found", ignoreCase = true) -> stringResource(Res.string.error_chat_deleted)
//        reason.contains("quota", ignoreCase = true) -> stringResource(Res.string.error_out_of_tokens)
//        reason.contains("timeout", ignoreCase = true) -> stringResource(Res.string.error_internet_connection)
//        reason.contains("user_banned", ignoreCase = true) -> stringResource(Res.string.error_banned)
//        else -> stringResource(Res.string.error_generic_try_again) // "Что-то пошло не так"
//    }
}

/**
 * Создаёт форму пузыря на основе пользовательских настроек.
 */
internal fun resolveBubbleShape(
    shapeType: BubbleShapeType,
    cornerRadius: Dp,
    isOutgoing: Boolean
): Shape {
    return when (shapeType) {
        BubbleShapeType.Tail -> MessageBubbleWithTailShape(
            cornerRadius = cornerRadius,
            isOutgoing = isOutgoing
        )

        BubbleShapeType.Rounded -> RoundedCornerShape(cornerRadius)

        BubbleShapeType.SoftCorner -> SoftCornerBubbleShape(
            cornerRadius = cornerRadius,
            softCornerRadius = 4.dp,
            isOutgoing = isOutgoing
        )
    }
}

/**
 * Пузырь без хвостика, но с одним «мягким» углом вместо него.
 * У user — правый нижний угол маленький, у бота — левый нижний.
 */
internal class SoftCornerBubbleShape(
    private val cornerRadius: Dp,
    private val softCornerRadius: Dp = 4.dp,
    private val isOutgoing: Boolean
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r = cornerRadius.value * density.density
        val sr = softCornerRadius.value * density.density
        val path = Path().apply {
            if (isOutgoing) {
                // Все углы = r, кроме правого нижнего = sr
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = 0f, top = 0f, right = size.width, bottom = size.height,
                        topLeftCornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
                        topRightCornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
                        bottomLeftCornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
                        bottomRightCornerRadius = androidx.compose.ui.geometry.CornerRadius(sr, sr)
                    )
                )
            } else {
                // Все углы = r, кроме левого нижнего = sr
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left = 0f, top = 0f, right = size.width, bottom = size.height,
                        topLeftCornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
                        topRightCornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
                        bottomLeftCornerRadius = androidx.compose.ui.geometry.CornerRadius(sr, sr),
                        bottomRightCornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r)
                    )
                )
            }
        }
        return Outline.Generic(path)
    }
}
