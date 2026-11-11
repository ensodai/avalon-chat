package com.ensodai.avalon.core.pagination

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Paginator<K, T>(
    private val getKey: (T) -> K,
    private val loadPage: suspend (page: Int) -> Result<PageResult<T>>
) {
    data class PageResult<T>(
        val items: List<T>,
        val totalPages: Int,
    )

    private val _state = MutableStateFlow(PaginatorState<T>())
    val state: StateFlow<PaginatorState<T>> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val loadMutex = Mutex()
    private var currentPage = 1

    fun initialize() {
        scope.launch { loadNextPage(isRefresh = true) }
    }

    suspend fun loadMore() {
        if (_state.value.isEndReached) return
        if (!loadMutex.tryLock()) return
        try {
            loadNextPage(isRefresh = false)
        } finally {
            loadMutex.unlock()
        }
    }

    fun refresh() {
        scope.launch {
            loadMutex.withLock {
                currentPage = 1
                _state.update { it.copy(isRefreshing = true, error = null) }
                loadNextPage(isRefresh = true)
                _state.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private suspend fun loadNextPage(isRefresh: Boolean) {
        _state.update { it.copy(isLoadingMore = true, error = null) }
        loadPage(currentPage)
            .onSuccess { result ->
                currentPage++
                _state.update { current ->
                    val existingKeys = if (isRefresh) emptySet()
                    else current.items.map { getKey(it) }.toSet()
                    val uniqueItems = result.items.filter { getKey(it) !in existingKeys }
                    current.copy(
                        items = if (isRefresh) result.items else current.items + uniqueItems,
                        isLoadingMore = false,
                        isEndReached = currentPage > result.totalPages,
                    )
                }
            }
            .onFailure { error ->
                _state.update { it.copy(isLoadingMore = false, error = error) }
            }
    }

    fun cancel() {
        scope.cancel()
    }
}