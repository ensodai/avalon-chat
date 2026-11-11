package com.ensodai.avalon.presentation.navigation

import com.ensodai.avalon.domain.sessionWorker.PendingDeletionItem

data class MainActions(
    val onEnableSwipeable: () -> Unit,
    val onDisableSwipeable: () -> Unit,
    val undoDeletion: (PendingDeletionItem) -> Unit,
    val forceDelete: (PendingDeletionItem) -> Unit
)