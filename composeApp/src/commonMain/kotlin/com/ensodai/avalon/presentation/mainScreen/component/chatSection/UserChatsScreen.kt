package com.ensodai.avalon.presentation.mainScreen.component.chatSection

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.ensodai.avalon.theme.AvalonTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.data.db.models.ChatSessionPreview
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType.MESSAGE_SYSTEM_INSTRUCTION
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType.SYSTEM_EVENT
import com.ensodai.shared.avalon.chat.models.SystemEventType
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.no_messages_yet
import com.ensodai.avalon.generated.resources.start_message
import com.ensodai.avalon.generated.resources.system_message_server_error
import com.ensodai.avalon.presentation.benchmark.BenchmarkTags
import com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.resolveSystemMessageContent
import com.ensodai.avalon.presentation.commonComponents.CharacterChatItem
import com.ensodai.avalon.presentation.mainScreen.viewState.MainScreenViewState
import com.ensodai.avalon.presentation.mainScreen.viewState.OnboardingViewState
import com.ensodai.avalon.presentation.mainScreen.viewState.ScreenMode
import com.ensodai.avalon.presentation.presentationUtils.bottomSystemButtonsPadding
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
fun UserChatsScreen(
    viewState: MainScreenViewState,
    listState: LazyListState,
    innerPadding: PaddingValues,
    chats: List<ChatSessionPreview>,
    archivedCharacters: List<String>,
    isShowArchiveSection: Boolean,
    onboardingViewState: OnboardingViewState,
    onClickChat: (Uuid) -> Unit,
    navigateToChatScreen: (Uuid) -> Unit,
    onEnableSelectionChatsModel: (Uuid) -> Unit,
    navigateToArchiveScreen: () -> Unit,
    onCloseTelegramReminder: () -> Unit,
    onCloseOpenApiTransitionAnnouncement: () -> Unit,
    onOpenOpenApiTransitionBottomSheet: () -> Unit
) {

    Box(modifier = Modifier.fillMaxSize()) {
        if (viewState.isShowStartMessage) {
            StartMessage(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .align(Alignment.Center)
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag(BenchmarkTags.MainUserChatsList),
            state = listState,
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = bottomSystemButtonsPadding()
            )
        ) {

            if (onboardingViewState.isShowOpenApiTransitionAnnouncement) {
                item {
                    OpenApiTransitionAnnouncementItem(
                        onClick = onOpenOpenApiTransitionBottomSheet,
                        onClose = onCloseOpenApiTransitionAnnouncement
                    )
                }
            }

            if (onboardingViewState.isShowTelegramReminder) {
                item {
                    TelegramReminderItem(
                        onClose = { onCloseTelegramReminder() }
                    )
                }
            }

            if (isShowArchiveSection) {
                item {
                    ArchiveItemChat(
                        modifier = Modifier,
                        archivedCharacters = archivedCharacters,
                        onClick = { navigateToArchiveScreen() }
                    )
                }
            }

            itemsIndexed(items = chats, key = { _, item -> item.sessionId }) { index, currentChat ->

                val messageContent =
                    currentChat.lastMessageContent ?: stringResource(Res.string.no_messages_yet)

                val displayMessage = if (currentChat.lastMessageType == SYSTEM_EVENT) {
                    val eventType = currentChat.lastMessageContent?.let { content ->
                        SystemEventType.entries.find { it.name == content }
                    }
                    if (eventType != null) {
                        resolveSystemMessageContent(
                            systemEventType = eventType,
                            seedId = currentChat.sessionId,
                        ).text
                    } else {
                        stringResource(Res.string.system_message_server_error)
                    }
                } else {
                    currentChat.lastMessageContent
                }

                if (currentChat.lastMessageType != MESSAGE_SYSTEM_INSTRUCTION) {
                    CharacterChatItem(
                        modifier = Modifier
                            .testTag(BenchmarkTags.userChatItem(index))
                            .animateItem(
                                fadeInSpec = null,
                                placementSpec = spring(
                                    stiffness = Spring.StiffnessMediumLow,
                                    visibilityThreshold = IntOffset.VisibilityThreshold
                                ),
                                fadeOutSpec = null
                            ),
                        isTyping = viewState.typingCharacters.contains(currentChat.sessionId),
                        isEnableSelectionChats = viewState.screenMode == ScreenMode.SelectedChatMode,
                        onSelectChat = {
                            if (viewState.screenMode == ScreenMode.SelectedChatMode) {
                                onClickChat(it)
                            } else {
                                navigateToChatScreen(currentChat.characterId)
                            }
                        },
                        onEnableSelectionChats = { onEnableSelectionChatsModel(it) },
                        chatName = currentChat.characterName,
                        chatId = currentChat.sessionId,
                        characterId = currentChat.characterId,
                        chatImage = currentChat.characterAvatarUrl,
                        lastTimeMessage = currentChat.lastMessageTime,
                        lastMessage = displayMessage,
                        lastMessageType = currentChat.lastMessageType,
                        draftMessage = currentChat.draftMessage,
                        isSelected = currentChat.sessionId in viewState.selectedChats,
                        unreadCount = currentChat.unreadCount
                    )
                }
            }
        }
    }
}

@Composable
private fun StartMessage(
    modifier: Modifier
) {
    Text(
        modifier = modifier
            .background(com.ensodai.avalon.theme.AvalonTheme.colors.surfaceHigh.copy(alpha = 0.2f)),
        text = stringResource(Res.string.start_message),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall,
        color = com.ensodai.avalon.theme.AvalonTheme.colors.placeholderText
    )
}
