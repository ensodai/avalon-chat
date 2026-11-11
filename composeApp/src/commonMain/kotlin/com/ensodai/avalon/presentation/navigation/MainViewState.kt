package com.ensodai.avalon.presentation.navigation

import com.ensodai.avalon.domain.sessionWorker.PendingDeletionItem

data class MainViewState(
    val isSwipeable: Boolean = true,
    val pendingDeletions: List<PendingDeletionItem> = emptyList()
)
