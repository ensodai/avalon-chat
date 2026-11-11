package com.ensodai.avalon.presentation.archiveScreen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.ensodai.avalon.presentation.commonComponents.GemIslandTopBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.archive
import com.ensodai.avalon.generated.resources.delete_selected_chats
import com.ensodai.avalon.navigation.AvalonBackHandler
import com.ensodai.avalon.presentation.archiveScreen.action.ArchiveScreenActions
import com.ensodai.avalon.presentation.archiveScreen.viewState.ArchiveScreenViewState
import com.ensodai.avalon.presentation.archiveScreen.viewState.isEnableSelectionChatsMode
import com.ensodai.avalon.presentation.commonComponents.AnimateCloseButtonAndCounter
import com.ensodai.avalon.presentation.commonComponents.BaseItemAlphaAnimation
import com.ensodai.avalon.presentation.commonComponents.CharacterChatItem
import com.ensodai.avalon.presentation.commonComponents.alertDialogs.AlertDialogDeleteItem
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.presentation.presentationUtils.bottomSystemButtonsPadding
import com.ensodai.avalon.theme.IconsManager
import org.jetbrains.compose.resources.stringResource
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType.SYSTEM_EVENT
import com.ensodai.shared.avalon.chat.models.SystemEventType
import com.ensodai.avalon.generated.resources.system_message_server_error
import com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble.resolveSystemMessageContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreenContent(
    viewState: ArchiveScreenViewState,
    actions: ArchiveScreenActions,
    navigation: Navigation,
    viewModel: ArchiveScreenViewModel
) {
    val chats by viewModel.chats.collectAsState()

    AvalonBackHandler(viewState.isEnableSelectionChatsMode) {
        if (viewState.isEnableSelectionChatsMode) {
            actions.onDisableSelectedChats()
        } else {
            navigation.navigateBack()
        }
    }

    AlertDialogDeleteItem(
        title = stringResource(Res.string.delete_selected_chats) + " ${viewState.selectedChats.size}",
        onDismiss = { actions.onCloseAlertDialog() },
        onConfirm = {
            actions.onDeleteSelectedChats()
        },
        isShowDialog = viewState.isShowAlertDialogDeleteChat
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            GemIslandTopBar(
                modifier = Modifier
                    .fillMaxWidth(),
                title = {
                    BaseItemAlphaAnimation(isVisible = !viewState.isEnableSelectionChatsMode) {
                        Text(
                            text = stringResource(Res.string.archive),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    BaseItemAlphaAnimation(isVisible = viewState.isEnableSelectionChatsMode) {
                        AnimateCloseButtonAndCounter(
                            counter = viewState.selectedChats.size,
                            isEnable = viewState.isEnableSelectionChatsMode,
                            onCloseClick = { actions.onDisableSelectedChats() },
                        )
                    }

                    BaseItemAlphaAnimation(isVisible = !viewState.isEnableSelectionChatsMode) {
                        NavigationBackButton(
                            onBackButtonClick = {
                                navigation.navigateBack()
                            }
                        )
                    }
                },
                actions = {
                    BaseItemAlphaAnimation(isVisible = viewState.isEnableSelectionChatsMode) {
                        IconButton(
                            onClick = { actions.unarchiveChats() },
                        ) {
                            Icon(
                                imageVector = IconsManager.Unarchive,
                                contentDescription = "Unarchive"
                            )
                        }
                    }
                    BaseItemAlphaAnimation(isVisible = viewState.isEnableSelectionChatsMode) {
                        IconButton(
                            onClick = { actions.onShowAlertDialogDeleteShat() },
                        ) {
                            Icon(
                                imageVector = IconsManager.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = bottomSystemButtonsPadding()
            )
        ) {
            items(chats) { currentChat ->

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

                CharacterChatItem(
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = null,
                            placementSpec = spring(
                                stiffness = Spring.StiffnessMediumLow,
                                visibilityThreshold = IntOffset.VisibilityThreshold
                            ),
                            fadeOutSpec = null
                        ),
                    isTyping = false,
                    isEnableSelectionChats = viewState.isEnableSelectionChatsMode,
                    onSelectChat = {
                        if (viewState.isEnableSelectionChatsMode) {
                            actions.onSelectChat(it)
                        } else {
                            navigation.navigateToChatScreen(
                                characterPublicId = currentChat.characterId
                            )
                        }
                    },
                    onEnableSelectionChats = { actions.onEnableSelectionChatsModel(it) },
                    chatName = currentChat.characterName,
                    chatId = currentChat.sessionId,
                    characterId = currentChat.characterId,
                    chatImage = currentChat.characterAvatarUrl,
                    lastTimeMessage = currentChat.lastMessageTime,
                    lastMessage = displayMessage,
                    lastMessageType = currentChat.lastMessageType,
                    isSelected = currentChat.sessionId in viewState.selectedChats,
                    unreadCount = currentChat.unreadCount
                )
            }
        }
    }
}
