package com.ensodai.avalon.presentation.globalLorebooksScreen

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.character.models.ForkLorebookRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import com.ensodai.avalon.domain.lorebook.LorebookManager
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.globalLorebooksScreen.action.GlobalLorebooksScreenActions
import com.ensodai.avalon.presentation.globalLorebooksScreen.viewState.GlobalLorebooksScreenViewState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@KoinViewModel
class GlobalLorebooksViewModel(
    private val characterRpcService: CharacterRpcService,
    private val lorebookManager: LorebookManager
) : BaseViewModel<GlobalLorebooksScreenViewState, GlobalLorebooksScreenActions>(
    initialState = GlobalLorebooksScreenViewState()
) {
    private var searchJob: Job? = null

    override val actions = GlobalLorebooksScreenActions(
        onSearchQueryChanged = { query ->
            updateViewState { it.copy(searchQuery = query) }
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                delay(350)
                loadCatalog(reset = true)
            }
        },
        onRefresh = {
            loadCatalog(reset = true, isPullToRefresh = true)
        },
        onLoadMore = {
            val state = viewState.value
            if (!state.isLoadingMore && state.currentPage < state.totalPages) {
                loadCatalog(reset = false)
            }
        },
        onToggleSaveToLibrary = { lorebookId, save ->
            toggleSaveToLibrary(lorebookId, save)
        },
        onForkLorebook = { lorebookId ->
            forkLorebook(lorebookId)
        },
        onOpenLorebookDetails = {},
        onDismissFeedbackMessage = {
            updateViewState { it.copy(actionFeedbackMessage = null) }
        }
    )

    init {
        loadCatalog(reset = true)
    }

    private fun loadCatalog(reset: Boolean, isPullToRefresh: Boolean = false) {
        viewModelScope.launch {
            val targetPage = if (reset) 1 else viewState.value.currentPage + 1
            if (reset) {
                updateViewState {
                    it.copy(
                        isCatalogLoading = !isPullToRefresh,
                        isRefreshing = isPullToRefresh,
                        currentPage = 1
                    )
                }
            } else {
                updateViewState { it.copy(isLoadingMore = true) }
            }

            try {
                val query = viewState.value.searchQuery.trim().ifEmpty { null }
                val response = characterRpcService.getPublicLorebooks(
                    query = query,
                    page = targetPage,
                    limit = 20
                )
                updateViewState { current ->
                    val combined = if (reset) response.items else current.catalogBooks + response.items
                    current.copy(
                        catalogBooks = combined,
                        currentPage = targetPage,
                        totalPages = response.totalPages,
                        isCatalogLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false
                    )
                }
            } catch (e: Exception) {
                updateViewState {
                    it.copy(
                        isCatalogLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        actionFeedbackMessage = e.message ?: "Failed to load catalog"
                    )
                }
            }
        }
    }

    private fun toggleSaveToLibrary(lorebookId: Uuid, save: Boolean) {
        viewModelScope.launch {
            // Optimistic update
            updateViewState { state ->
                val updatedCatalog = state.catalogBooks.map {
                    if (it.id == lorebookId) it.copy(isSavedToLibrary = save) else it
                }
                state.copy(catalogBooks = updatedCatalog)
            }

            try {
                if (save) {
                    lorebookManager.saveLorebookToLibrary(lorebookId)
                } else {
                    lorebookManager.removeLorebookFromLibrary(lorebookId)
                }
            } catch (e: Exception) {
                // Rollback on error
                updateViewState { state ->
                    val rolledBack = state.catalogBooks.map {
                        if (it.id == lorebookId) it.copy(isSavedToLibrary = !save) else it
                    }
                    state.copy(
                        catalogBooks = rolledBack,
                        actionFeedbackMessage = e.message ?: "Action failed"
                    )
                }
            }
        }
    }

    private fun forkLorebook(lorebookId: Uuid) {
        viewModelScope.launch {
            try {
                characterRpcService.forkLorebook(
                    ForkLorebookRequest(lorebookId = lorebookId)
                )
                lorebookManager.loadUserLibrary()
                updateViewState {
                    it.copy(actionFeedbackMessage = "Копия книги успешно создана в вашей библиотеке!")
                }
            } catch (e: Exception) {
                updateViewState {
                    it.copy(actionFeedbackMessage = e.message ?: "Не удалось создать копию")
                }
            }
        }
    }
}
