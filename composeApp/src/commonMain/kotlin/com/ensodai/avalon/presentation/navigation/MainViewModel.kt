package com.ensodai.avalon.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.domain.sessionWorker.DeletionUndoManager
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class MainViewModel(
    private val deletionUndoManager: DeletionUndoManager,
    private val logger: AppLogger
) : ViewModel() {

    private val _viewState = MutableStateFlow(MainViewState())
    val viewState: StateFlow<MainViewState> = _viewState

    val action = MainActions(
        onEnableSwipeable = { enableSwipeable() },
        onDisableSwipeable = { disableSwipeable() },
        undoDeletion = { item -> deletionUndoManager.undoDeletion(item) },
        forceDelete = { item -> deletionUndoManager.forceCommitDeletion(item) }
    )

    init {
        updateDeleteItems()
    }

    override fun onCleared() {
        // ViewModel уничтожается (например, пользователь вышел из аккаунта
        // и граф навигации был уничтожен). Чистим всё за собой.
        super.onCleared()
        logger.d("MainViewModel", "MainViewModel cleared. Cleaning up WebSocket.")
//        webSocketManager.cleanup()
    }

    private fun enableSwipeable() {
        _viewState.getAndUpdate {
            it.copy(isSwipeable = true)
        }
    }

    private fun disableSwipeable() {
        _viewState.getAndUpdate {
            it.copy(isSwipeable = false)
        }
    }

    private fun updateDeleteItems() {
        viewModelScope.launch(IODispatcher) {
            deletionUndoManager
                .pendingDeletions
                .collect { deleteItems ->
                    _viewState.getAndUpdate {
                        it.copy(pendingDeletions = deleteItems)
                    }
                }
        }

    }
}