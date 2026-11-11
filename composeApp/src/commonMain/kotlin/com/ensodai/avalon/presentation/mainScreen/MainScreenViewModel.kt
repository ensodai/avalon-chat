package com.ensodai.avalon.presentation.mainScreen

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.character.models.CharacterPreviewResponse
import com.ensodai.avalon.core.pagination.Paginator
import com.ensodai.avalon.core.pagination.PaginatorState
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.mainScreen.viewState.MainScreenViewState
import com.ensodai.avalon.presentation.mainScreen.action.MainScreenActions
import com.ensodai.avalon.presentation.mainScreen.action.*
import com.ensodai.avalon.utils.L
import com.ensodai.avalon.utils.messageOrDefault
import kotlinx.coroutines.launch
import com.ensodai.avalon.domain.useCases.mainScreenUseCase.*
import com.ensodai.avalon.domain.useCases.commonUseCase.auth.AuthenticationUserFromChatUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.ArchiveChatSessionsUseCase
import com.ensodai.avalon.domain.sessionWorker.DeletionUndoManager
import com.ensodai.avalon.data.ResourceWrapper
import com.ensodai.avalon.data.db.models.ChatSessionPreview
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import com.ensodai.avalon.domain.useCases.commonUseCase.promoFlags.GetShowPromoFlagsFlowUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.promoFlags.DisableTelegramReminderPromoFlagUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.promoFlags.DisableOpenApiTransitionAnnouncementPromoFlagUseCase
import com.ensodai.avalon.presentation.mainScreen.viewState.SearchLoadingState
import com.ensodai.avalon.utils.EMPTY_STRING
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.ensodai.avalon.domain.sessionWorker.RaceConditionTester
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
class MainScreenViewModel(
    private val getAllChats: GetAllChatSessionsUseCase,
    private val checkSavedUserChats: CheckSavedUserChatsUseCase,
    private val authenticationUser: AuthenticationUserFromChatUseCase,
    private val deleteChatSessions: DeleteChatSessionsUseCase,
    private val loadCharactersCategory: LoadCharactersCategoryUseCase,
    private val getCategoriesFlow: GetCategoriesFlowUseCase,
    private val archivedChats: ArchiveChatSessionsUseCase,
    private val getArchivedCharacterNamesFlow: GetArchivedCharacterNamesFlowUseCase,
    private val getShowPromoFlagsFlow: GetShowPromoFlagsFlowUseCase,
    private val disableTelegramReminderPromoFlag: DisableTelegramReminderPromoFlagUseCase,
    private val disableOpenApiTransitionAnnouncementPromoFlag: DisableOpenApiTransitionAnnouncementPromoFlagUseCase,
    private val refreshCategoryCounters: RefreshCategoryCountersUseCase,
    private val getAllTypingCharactersIdsFlow: GetAllTypingCharactersIdsFlowUseCase,
    private val chatSyncRpcClient: ChatSyncRpcClient,
    private val deletionUndoManager: DeletionUndoManager,
    private val resourceWrapper: ResourceWrapper,
    private val searchCharactersUseCase: SearchCharactersUseCase,
    private val getCharactersByCategoryUseCase: GetCharactersByCategoryUseCase,
    private val raceConditionTester: RaceConditionTester
) : BaseViewModel<MainScreenViewState, MainScreenActions>(
    initialState = MainScreenViewState()
) {

    private val categoryPaginators = mutableMapOf<String, Paginator<Uuid, CharacterPreviewResponse>>()
    private var searchPaginator: Paginator<Uuid, CharacterPreviewResponse>? = null

    private val _categoryState = MutableStateFlow(PaginatorState<CharacterPreviewResponse>())
    val categoryState: StateFlow<PaginatorState<CharacterPreviewResponse>> = _categoryState.asStateFlow()

    private val _searchState = MutableStateFlow(PaginatorState<CharacterPreviewResponse>())
    val searchState: StateFlow<PaginatorState<CharacterPreviewResponse>> = _searchState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    val chats: StateFlow<List<ChatSessionPreview>> = getAllChats.getAllChats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    override val actions = MainScreenActions(
        onClickChat = { selectChat(it) },
        onCopySelectedChat = { },
        onEnableSelectionChatsModel = { enableSelectionChats(it) },
        setDefaultScreenMode = {
            _searchQuery.value = EMPTY_STRING
            setDefaultScreenMode()
        },
        onDeleteSelectedChats = {
            deleteChatConfigAndMessage(
                deletionUndoManager = deletionUndoManager,
                resourceWrapper = resourceWrapper
            )
        },
        onCloseAlertDialog = { closeAlertDialog() },
        auth = { authentication(it, authenticationUser) },
        onClickTestButton = {
            raceConditionTester.triggerRaceWsVsRpc()
        },
        onOpenUserChatsScreen = { openUserChatsScreen() },
        onOpenAllCharactersChatsScreen = { openAllCharactersChatsScreen() },
        onSelectCategory = {
            selectCurrentCategory(it)
        },
        onArchivedSelectedChats = { archivedSelectedChats(archivedChats = archivedChats) },
        onCloseTelegramReminder = {
            closeTelegramReminder(disableTelegramReminderPromoFlag = disableTelegramReminderPromoFlag)
        },
        onCloseOpenApiTransitionAnnouncement = {
            closeOpenApiTransitionAnnouncement(disableOpenApiTransitionAnnouncementPromoFlag)
        },
        onOpenOpenApiTransitionBottomSheet = {
            openOpenApiTransitionBottomSheet()
        },
        onDismissOpenApiTransitionBottomSheet = {
            dismissOpenApiTransitionBottomSheet()
        },
        refreshCategoryCounters = {
            refreshCurrentCategory()
            refreshCategoryCounters(refreshCategoryCounters)
        },
        onSearchMode = { enableSearchMode() },
        onSelectScreenPage = { selectScreenPage(it) },
        onChangeSearchText = {
            _searchQuery.value = it
            changeSearchText(query = it)
        },
        updateSearchState = { updateSearchState(it) },
        testButton = { }
    )

    init {
        observeCategory()
        observeSearch()

        syncCategory(loadCharactersCategory)
        updateTypingStatusForChats(getAllTypingCharactersIdsFlow)
        getCategories(getCategoriesFlow)
        updateStartMessageVisible(checkSavedUserChats)
        updateArchivedCharacterNames(getArchivedCharacterNamesFlow = getArchivedCharacterNamesFlow)
        updateArchiveItemIsVisible(getArchivedCharacterNamesFlow = getArchivedCharacterNamesFlow)
        updateOnboardingState(getShowPromoFlagsFlow = getShowPromoFlagsFlow)
        updateConnectionState(chatSyncRpcClient)
    }

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(500L)
                .distinctUntilChanged()
                .collectLatest { query ->
                    searchPaginator?.cancel()
                    searchPaginator = null

                    if (query.isBlank()) {
                        _searchState.value = PaginatorState()
                        updateViewState { it.copy(searchLoadingState = SearchLoadingState.Idle) }
                        return@collectLatest
                    }

                    val paginator = Paginator(
                        getKey = { character -> character.id },
                        loadPage = { page ->
                            searchCharactersUseCase(query = query, page = page)
                                .map { Paginator.PageResult(it.items, it.totalPages) }
                        }
                    )
                    searchPaginator = paginator
                    paginator.initialize()

                    paginator.state.collect { paginatorState ->
                        _searchState.value = paginatorState
                        val loadingState = when {
                            paginatorState.isRefreshing -> SearchLoadingState.Loading
                            paginatorState.isLoadingMore -> SearchLoadingState.Loading
                            paginatorState.error != null -> SearchLoadingState.Error
                            paginatorState.items.isEmpty() -> SearchLoadingState.Idle
                            else -> SearchLoadingState.Success
                        }
                        updateViewState { it.copy(searchLoadingState = loadingState) }
                    }
                }
        }
    }

    private fun observeCategory() {
        viewModelScope.launch {
            viewState
                .map { it.currentCategory }
                .distinctUntilChanged()
                .collectLatest { currentCategory ->
                    val categoryCode = currentCategory?.categoryCode ?: return@collectLatest

                    val paginator = categoryPaginators.getOrPut(categoryCode) {
                        Paginator(
                            getKey = { character -> character.id },
                            loadPage = { page ->
                                getCharactersByCategoryUseCase(category = categoryCode, page = page)
                                    .map { Paginator.PageResult(it.items, it.totalPages) }
                            }
                        ).also { it.initialize() }
                    }

                    paginator.state.collect { _categoryState.value = it }
                }
        }
    }


    fun loadMoreCharacters() {
        viewModelScope.launch {
            if (_searchQuery.value.isNotBlank()) {
                searchPaginator?.loadMore()
            } else {
                val categoryCode = viewState.value.currentCategory?.categoryCode ?: "all"
                categoryPaginators[categoryCode]?.loadMore()
            }
        }
    }

    fun refreshCurrentCategory() {
        val categoryCode = viewState.value.currentCategory?.categoryCode ?: "all"
        categoryPaginators[categoryCode]?.refresh()
    }

    override fun onCleared() {
        super.onCleared()
        categoryPaginators.values.forEach { it.cancel() }
        searchPaginator?.cancel()
    }
}
