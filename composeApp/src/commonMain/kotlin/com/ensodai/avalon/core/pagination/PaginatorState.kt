package com.ensodai.avalon.core.pagination

data class PaginatorState<T>(
    val items: List<T> = emptyList(),
    val isRefreshing: Boolean = false,  // pull-to-refresh / первая загрузка
    val isLoadingMore: Boolean = false, // подгрузка следующей страницы
    val isEndReached: Boolean = false,
    val error: Throwable? = null,
)