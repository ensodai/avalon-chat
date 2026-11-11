package com.ensodai.avalon.presentation.globalLorebooksScreen.viewState

import com.ensodai.shared.avalon.character.models.UserLorebookSummaryDto
import com.ensodai.avalon.presentation.core.BaseViewState

data class GlobalLorebooksScreenViewState(
    val searchQuery: String = "",
    val catalogBooks: List<UserLorebookSummaryDto> = emptyList(),
    val isCatalogLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val actionFeedbackMessage: String? = null
) : BaseViewState()
