package com.ensodai.avalon.presentation.globalLorebooksScreen.action

import com.ensodai.avalon.presentation.core.BaseActions
import kotlin.uuid.Uuid

data class GlobalLorebooksScreenActions(
    val onSearchQueryChanged: (String) -> Unit,
    val onRefresh: () -> Unit,
    val onLoadMore: () -> Unit,
    val onToggleSaveToLibrary: (Uuid, Boolean) -> Unit,
    val onForkLorebook: (Uuid) -> Unit,
    val onOpenLorebookDetails: (Uuid) -> Unit,
    val onDismissFeedbackMessage: () -> Unit
) : BaseActions()
