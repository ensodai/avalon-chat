package com.ensodai.avalon.presentation.mainScreen.component.topBar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ensodai.avalon.domain.chatEvents.RpcConnectionState
import com.ensodai.avalon.presentation.commonComponents.AnimateCloseButtonAndCounter
import com.ensodai.avalon.presentation.commonComponents.BaseItemAlphaAnimation
import com.ensodai.avalon.presentation.commonComponents.GemPill
import com.ensodai.avalon.presentation.commonComponents.topBarIcons.NavigationBackButton
import com.ensodai.avalon.presentation.mainScreen.component.chatSection.ChatsScreens
import com.ensodai.avalon.presentation.mainScreen.viewState.ScreenMode
import com.ensodai.avalon.presentation.mainScreen.viewState.SearchLoadingState
import com.ensodai.avalon.theme.AvalonTheme
import com.ensodai.avalon.theme.IconsManager

private val MainTopBarPillHeight = 40.dp
private val MainTopBarCenterPillWidth = 88.dp
private val MainTopBarEdgePadding = 8.dp
private val MainTopBarContainerHeight = 64.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarMainScreen(
    modifier: Modifier,
    onOpenAccountScreen: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    countSelectedChats: Int,
    isShowAccountButton: Boolean,
    isShowDeleteButton: Boolean,
    isShowEditButton: Boolean,
    isShowCopyButton: Boolean,
    isShowSearchButton: Boolean,
    pagerState: PagerState,
    tabScreenItem: List<ChatsScreens>,
    searchQuery: String,
    searchLoadingState: SearchLoadingState,
    onCloseSearchMode: () -> Unit,
    onChangeSearchText: (String) -> Unit,
    onSearchButtonClick: () -> Unit,
    onDisableSelectedChats: () -> Unit,
    onDeleteSelectedChats: () -> Unit,
    onOpenEditSelectedChatConfigScreen: () -> Unit,
    onCopyClick: () -> Unit,
    onArchivedSelectedChats: () -> Unit,
    screenMode: ScreenMode,
    connectionState: RpcConnectionState
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(MainTopBarContainerHeight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MainTopBarEdgePadding)
                .align(Alignment.Center)
        ) {
            BaseItemAlphaAnimation(
                modifier = Modifier.align(Alignment.CenterStart),
                isVisible = screenMode == ScreenMode.DefaultMode
            ) {
                GemPill(
                    modifier = Modifier
                        .size(MainTopBarPillHeight)
                        .height(MainTopBarPillHeight),
                    horizontalArrangement = Arrangement.Center,
                    fillContentSize = true
                ) {
                    HexagonConnectionStatus(
                        connectionState = connectionState,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            val centerPillWidth = (44 * tabScreenItem.size.coerceAtLeast(1)).dp
            ScreenTabRow(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(centerPillWidth)
                    .height(MainTopBarPillHeight),
                isEnable = screenMode == ScreenMode.DefaultMode,
                pagerState = pagerState,
                items = tabScreenItem
            )

            BaseItemAlphaAnimation(
                modifier = Modifier.align(Alignment.Center),
                isVisible = screenMode == ScreenMode.SearchMode
            ) {
                GemPill {
                    NavigationBackButton(
                        onBackButtonClick = { onCloseSearchMode() },
                    )
                    SearchCharacterTextField(
                        modifier = Modifier,
                        inputText = searchQuery,
                        searchLoadingState = searchLoadingState,
                        onChangeSearchText = { onChangeSearchText(it) }
                    )
                }
            }

            BaseItemAlphaAnimation(
                modifier = Modifier.align(Alignment.Center),
                isVisible = screenMode == ScreenMode.SelectedChatMode
            ) {
                GemPill {
                    AnimateCloseButtonAndCounter(
                        counter = countSelectedChats,
                        isEnable = true,
                        onCloseClick = { onDisableSelectedChats() },
                    )
                    IconButton(
                        onClick = { onArchivedSelectedChats() },
                    ) {
                        Icon(
                            imageVector = IconsManager.Archive,
                            contentDescription = "Archive selected chats"
                        )
                    }
                    IconButton(
                        onClick = { onDeleteSelectedChats() },
                    ) {
                        Icon(
                            imageVector = IconsManager.Delete,
                            contentDescription = "Delete selected chats"
                        )
                    }
                }
            }

            BaseItemAlphaAnimation(
                modifier = Modifier.align(Alignment.CenterEnd),
                isVisible = screenMode == ScreenMode.DefaultMode
            ) {
                GemPill(
                    modifier = Modifier
                        .size(MainTopBarPillHeight)
                        .height(MainTopBarPillHeight),
                    horizontalArrangement = Arrangement.Center,
                    fillContentSize = true
                ) {
                    ActionsRowMainScreen(
                        isShowAccountButton = isShowAccountButton,
                        isShowSearchButton = isShowSearchButton,
                        isShowDeleteButton = isShowDeleteButton,
                        isShowEditButton = isShowEditButton,
                        isShowCopyButton = isShowCopyButton,
                        onSearchCharacterClick = { onSearchButtonClick() },
                        onEditSelectedChat = { onOpenEditSelectedChatConfigScreen() },
                        onDeleteSelectedChats = { onDeleteSelectedChats() },
                        onOpenAccountClick = { onOpenAccountScreen() },
                        onCopyClick = { onCopyClick() },
                        onArchivedSelectedChats = { onArchivedSelectedChats() }
                    )
                }
            }
        }
    }
}
