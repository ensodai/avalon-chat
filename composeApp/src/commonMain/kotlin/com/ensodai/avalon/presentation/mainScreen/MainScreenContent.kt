package com.ensodai.avalon.presentation.mainScreen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ensodai.avalon.generated.resources.Res
import com.ensodai.avalon.generated.resources.delete_selected_chats
import com.ensodai.avalon.core.pagination.PaginatorState
import com.ensodai.avalon.navigation.AvalonBackHandler
import com.ensodai.avalon.presentation.commonComponents.alertDialogs.AlertDialogDeleteItem
import com.ensodai.avalon.presentation.benchmark.BenchmarkTags
import com.ensodai.avalon.presentation.globalLorebooksScreen.GlobalLorebooksScreen
import com.ensodai.avalon.presentation.mainScreen.action.MainScreenActions
import com.ensodai.avalon.presentation.mainScreen.component.CreateButtons
import com.ensodai.avalon.presentation.mainScreen.component.chatSection.AllCharactersChatsScreen
import com.ensodai.avalon.presentation.mainScreen.component.chatSection.ChatsScreens
import com.ensodai.avalon.presentation.mainScreen.component.chatSection.OpenApiTransitionBottomSheet
import com.ensodai.avalon.presentation.mainScreen.component.chatSection.UserChatsScreen
import com.ensodai.avalon.presentation.mainScreen.component.topBar.TopBarMainScreen
import com.ensodai.avalon.presentation.mainScreen.viewState.MainScreenViewState
import com.ensodai.avalon.presentation.mainScreen.viewState.ScreenMode
import com.ensodai.avalon.presentation.mainScreen.viewState.isShowAddCharacterButton
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.utils.L
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun MainScreenContent(
    viewState: MainScreenViewState,
    actions: MainScreenActions,
    navigation: Navigation,
    viewModel: MainScreenViewModel
) {
    val categoryState by viewModel.categoryState.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val isSearching = viewState.screenMode == ScreenMode.SearchMode
    val characterItems = if (isSearching) searchState.items else categoryState.items

    val chatSessionsItems by viewModel.chats.collectAsState()

    val isRefreshing = if (isSearching) searchState.isRefreshing else categoryState.isRefreshing
    val isLoadingMore = if (isSearching) searchState.isLoadingMore else categoryState.isLoadingMore
    if (isSearching) searchState.error else categoryState.error

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { viewState.allScreenList.size })

    val gridStates = remember { mutableStateMapOf<String, LazyGridState>() }
    val currentGridState =
        gridStates.getOrPut(viewState.currentCategory?.categoryCode ?: "all") { LazyGridState() }

    AvalonBackHandler(viewState.screenMode == ScreenMode.SelectedChatMode || viewState.screenMode == ScreenMode.SearchMode) {
        if (viewState.screenMode == ScreenMode.SelectedChatMode || viewState.screenMode == ScreenMode.SearchMode) {
            actions.setDefaultScreenMode()
        } else {
            navigation.navigateBack()
        }
    }

    LaunchedEffect(currentGridState) {
        snapshotFlow { currentGridState.layoutInfo }
            .map { it.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .debounce(100L)
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= characterItems.size - 5) {
                    L.d("MainScreenContent", "load more characters lastIndex=$lastIndex")
                    viewModel.loadMoreCharacters()
                }
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
            TopBarMainScreen(
                modifier = Modifier
                    .fillMaxWidth(),
                onOpenAccountScreen = {
                    navigation.navigateToUserAccountScreen()
                },
                connectionState = viewState.connectionState,
                scrollBehavior = scrollBehavior,
                countSelectedChats = viewState.selectedChats.size,
                screenMode = viewState.screenMode,
                isShowAccountButton = viewState.isShowAccountButton && pagerState.currentPage == 0,
                isShowDeleteButton = viewState.isShowDeleteButton,
                isShowEditButton = false,
                isShowCopyButton = false,
                pagerState = pagerState,
                tabScreenItem = viewState.allScreenList,
                searchQuery = viewState.searchQuery,
                searchLoadingState = viewState.searchLoadingState,
                isShowSearchButton = viewState.isShowSearchButton && (pagerState.currentPage == 1 || pagerState.currentPage == 2),
                onCloseSearchMode = { actions.setDefaultScreenMode() },
                onSearchButtonClick = { actions.onSearchMode() },
                onDisableSelectedChats = { actions.setDefaultScreenMode() },
                onDeleteSelectedChats = { actions.onDeleteSelectedChats() },
                onOpenEditSelectedChatConfigScreen = {
                    navigation
                        .navigateToEditCharacterScreen(viewState.selectedChats.last())
                },
                onCopyClick = {
                    actions.onCopySelectedChat()
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                onArchivedSelectedChats = { actions.onArchivedSelectedChats() },
                onChangeSearchText = { actions.onChangeSearchText(it) }
            )
        },
        floatingActionButton = {
            CreateButtons(
                onOpenTimeChat = {
                    navigation.navigateToTimeChat()
                },
                onClickTestButton = { actions.onClickTestButton() },
                onOpenEditChatConfigScreen = {
                    navigation.navigateToAddCharacterScreen()
                },
                onOpenImportCharacterScreen = {
                    navigation.navigateToImportCharacterScreen()
                },
                isShowFloatingActionButton = viewState.isShowAddCharacterButton && pagerState.currentPage == 0
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            modifier = Modifier
                .fillMaxSize()
                .testTag(BenchmarkTags.MainPager),
            state = pagerState,
        ) { currentPage ->
            val currentScreen = viewState.allScreenList[currentPage]
            when (currentScreen) {
                ChatsScreens.AllCharactersChatsScreen -> {
                    AllCharactersChatsScreen(
                        isRefreshing = isRefreshing,
                        isLoadingMore = isLoadingMore,
                        currentState = currentGridState,
                        innerPadding = innerPadding,
                        categories = viewState.categories,
                        characters = characterItems,
                        screenMode = viewState.screenMode,
                        currentCategory = viewState.currentCategory,
                        onSelectCategory = { actions.onSelectCategory(it) },
                        onOpenChat = { character ->
                            navigation.navigateToChatScreen(
                                characterPublicId = character.id
                            )
                        },
                        onRefreshCategory = { actions.refreshCategoryCounters() }
                    )
                }

                ChatsScreens.UserChatsScreen -> {
//                    actions.onSelectScreenPage(currentPage)
                    UserChatsScreen(
                        viewState = viewState,
                        listState = listState,
                        innerPadding = innerPadding,
                        chats = chatSessionsItems,
                        archivedCharacters = viewState.archivedCharacterNames,
                        isShowArchiveSection = !viewState.archivedCharacterNames.isEmpty(),
                        onboardingViewState = viewState.onboardingViewState,
                        onClickChat = { chatId -> actions.onClickChat(chatId) },
                        navigateToChatScreen = { characterPublicId ->
                            navigation.navigateToChatScreen(
                                characterPublicId = characterPublicId
                            )
                        },
                        onEnableSelectionChatsModel = { chetId ->
                            actions.onEnableSelectionChatsModel(chetId)
                        },
                        navigateToArchiveScreen = {
                            if (viewState.screenMode != ScreenMode.SelectedChatMode) {
                                navigation.navigateToArchiveScreen()
                            }
                        },
                        onCloseTelegramReminder = { actions.onCloseTelegramReminder() },
                        onCloseOpenApiTransitionAnnouncement = { actions.onCloseOpenApiTransitionAnnouncement() },
                        onOpenOpenApiTransitionBottomSheet = { actions.onOpenOpenApiTransitionBottomSheet() }
                    )
                }

                ChatsScreens.GlobalLorebooksScreen -> {
                    GlobalLorebooksScreen(
                        navigation = navigation,
                        innerPadding = innerPadding,
                        searchQuery = if (viewState.screenMode == ScreenMode.SearchMode) viewState.searchQuery else "",
                        isSearchMode = viewState.screenMode == ScreenMode.SearchMode
                    )
                }
            }
        }
    }

    if (viewState.isShowOpenApiTransitionBottomSheet) {
        OpenApiTransitionBottomSheet(
            onDismiss = { actions.onDismissOpenApiTransitionBottomSheet() },
            onOpenAiSettings = {
                actions.onDismissOpenApiTransitionBottomSheet()
                navigation.navigateToUserAiSettingsScreen()
            }
        )
    }
}
