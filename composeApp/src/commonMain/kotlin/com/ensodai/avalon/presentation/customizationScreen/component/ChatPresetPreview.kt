package com.ensodai.avalon.presentation.customizationScreen.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.chat.models.MessageStatus
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.avalon.domain.models.chat.RpgMessageBlock
import com.ensodai.avalon.domain.useCases.chatScreenUseCase.chat.ParseRpgMessageUseCase
import com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.MessageCard
import com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.rememberBubbleStyle
import com.ensodai.avalon.presentation.chatScreen.viewState.MessageBodyUiItem
import com.ensodai.avalon.presentation.customizationScreen.ChatPresetsViewState
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.AvalonThemePresets
import com.ensodai.avalon.theme.LocalAvalonColors
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleShapeType
import com.ensodai.avalon.theme.models.ChatBackgroundConfig
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import kotlin.uuid.Uuid
import org.jetbrains.compose.resources.stringResource
import com.ensodai.avalon.generated.resources.*

@Composable
fun ChatPresetPreview(
    preset: ChatRoleplayPreset,
    viewState: ChatPresetsViewState,
    localFontSize: Int,
    localShapeType: BubbleShapeType,
    localCornerRadius: Int,
    modifier: Modifier = Modifier
) {
    // Демо-сообщения (локализованные)
    val botMsg = stringResource(Res.string.chat_preset_demo_bot_message)
    val botTextItem = remember(botMsg) {
        MessageBodyUiItem.Text(
            textId = Uuid.random(),
            rawText = botMsg,
            parsedBlocks = listOf(RpgMessageBlock.Plain(botMsg)),
            isStreaming = false
        )
    }

    val userMsg = stringResource(Res.string.chat_preset_demo_user_message)
    val userTextItem = remember(userMsg) {
        MessageBodyUiItem.Text(
            textId = Uuid.random(),
            rawText = userMsg,
            parsedBlocks = listOf(RpgMessageBlock.Plain(userMsg)),
            isStreaming = false
        )
    }

    val rpgText = stringResource(Res.string.chat_preset_demo_rpg_message)
    val rpgTextItem = remember(rpgText) {
        MessageBodyUiItem.Text(
            textId = Uuid.random(),
            rawText = rpgText,
            parsedBlocks = ParseRpgMessageUseCase().invoke(rpgText),
            isStreaming = false
        )
    }

    val defaultThemeColors = AvalonThemePresets.resolve(viewState.selectedThemePreset ?: AvalonThemePreset.TechNoir)
    val defaultChatBgColor = when (val cfg = defaultThemeColors.chatBackground) {
        is ChatBackgroundConfig.Default -> defaultThemeColors.background
        is ChatBackgroundConfig.Solid -> cfg.color
        is ChatBackgroundConfig.Pattern -> cfg.baseColor
        is ChatBackgroundConfig.Image -> defaultThemeColors.background
    }

    val currentThemeColors = AvalonTheme.colors
    val temporaryThemeColors = remember(currentThemeColors, localFontSize, localShapeType, localCornerRadius) {
        currentThemeColors.copy(
            bubbleFontSize = localFontSize,
            bubbleShapeType = localShapeType,
            bubbleCornerRadius = localCornerRadius
        )
    }

    CompositionLocalProvider(LocalAvalonColors provides temporaryThemeColors) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(defaultChatBgColor)
        ) {
            if (viewState.chatBackgroundImagePath != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .then(if (viewState.chatBackgroundBlur > 0) Modifier.blur(viewState.chatBackgroundBlur.dp) else Modifier)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(viewState.chatBackgroundImagePath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                if (preset == ChatRoleplayPreset.STANDARD_BUBBLE) {
                    val botBubbleStyle = rememberBubbleStyle(
                        role = MessageRole.MODEL,
                        rarity = Rarity.COMMON,
                        preset = ChatRoleplayPreset.STANDARD_BUBBLE
                    )
                    val userBubbleStyle = rememberBubbleStyle(
                        role = MessageRole.USER,
                        rarity = Rarity.COMMON,
                        preset = ChatRoleplayPreset.STANDARD_BUBBLE
                    )

                    MessageCard(
                        role = MessageRole.MODEL,
                        bodyItems = listOf(botTextItem),
                        status = MessageStatus.SENT,
                        bubbleStyle = botBubbleStyle,
                        shouldShowAnimatedText = false,
                        isEnableSelectedMessagesMode = false,
                        isEnableEditMessageMode = false,
                        selectedMessagesText = null,
                        isSelected = false,
                        rarity = Rarity.COMMON,
                        minLines = 1,
                        isDefaultFocusRequester = false,
                        userReaction = null,
                        characterReaction = null,
                        userAvatarUrl = null,
                        characterAvatarUrl = null,
                        onEditMessageText = {},
                        onComplaintIconClick = {},
                        onOpenSettingsScreen = {},
                        onReactionClick = {},
                        chatRoleplayPreset = ChatRoleplayPreset.STANDARD_BUBBLE,
                        showTtsButton = false
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    MessageCard(
                        role = MessageRole.USER,
                        bodyItems = listOf(userTextItem),
                        status = MessageStatus.SENT,
                        bubbleStyle = userBubbleStyle,
                        shouldShowAnimatedText = false,
                        isEnableSelectedMessagesMode = false,
                        isEnableEditMessageMode = false,
                        selectedMessagesText = null,
                        isSelected = false,
                        rarity = Rarity.COMMON,
                        minLines = 1,
                        isDefaultFocusRequester = false,
                        userReaction = null,
                        characterReaction = null,
                        userAvatarUrl = null,
                        characterAvatarUrl = null,
                        onEditMessageText = {},
                        onComplaintIconClick = {},
                        onOpenSettingsScreen = {},
                        onReactionClick = {},
                        chatRoleplayPreset = ChatRoleplayPreset.STANDARD_BUBBLE,
                        showTtsButton = false
                    )
                } else {
                    val rpgBubbleStyle = rememberBubbleStyle(
                        role = MessageRole.MODEL,
                        rarity = Rarity.COMMON,
                        preset = preset
                    )

                    MessageCard(
                        role = MessageRole.MODEL,
                        bodyItems = listOf(rpgTextItem),
                        status = MessageStatus.SENT,
                        bubbleStyle = rpgBubbleStyle,
                        shouldShowAnimatedText = false,
                        isEnableSelectedMessagesMode = false,
                        isEnableEditMessageMode = false,
                        selectedMessagesText = null,
                        isSelected = false,
                        rarity = Rarity.COMMON,
                        minLines = 1,
                        isDefaultFocusRequester = false,
                        userReaction = null,
                        characterReaction = null,
                        userAvatarUrl = null,
                        characterAvatarUrl = null,
                        onEditMessageText = {},
                        onComplaintIconClick = {},
                        onOpenSettingsScreen = {},
                        onReactionClick = {},
                        chatRoleplayPreset = preset,
                        showTtsButton = false
                    )
                }
            }
        }
    }
}
