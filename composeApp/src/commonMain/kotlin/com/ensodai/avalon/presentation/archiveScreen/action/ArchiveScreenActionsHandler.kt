package com.ensodai.avalon.presentation.archiveScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.domain.useCases.commonUseCase.ArchiveChatSessionsUseCase
import com.ensodai.avalon.domain.useCases.mainScreenUseCase.DeleteChatSessionsUseCase
import com.ensodai.avalon.presentation.archiveScreen.ArchiveScreenViewModel
import com.ensodai.avalon.presentation.archiveScreen.viewState.isEnableSelectionChatsMode
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

fun ArchiveScreenViewModel.selectChat(chatId: Uuid) {
    if (viewState.value.isEnableSelectionChatsMode) {
        updateViewState {
            val updatedSelectedChats = if (chatId in it.selectedChats) {
                it.selectedChats - chatId
            } else {
                it.selectedChats + chatId
            }
            it.copy(selectedChats = updatedSelectedChats)
        }
    }
}

fun ArchiveScreenViewModel.enableSelectionChatsModel(chatId: Uuid) {
    updateViewState {
        it.copy(
            selectedChats = it.selectedChats + chatId,
        )
    }
}

fun ArchiveScreenViewModel.disableSelectedChats() {
    updateViewState {
        it.copy(
            selectedChats = emptyList()
        )
    }
}

fun ArchiveScreenViewModel.unarchiveChats(unarchivedChats: ArchiveChatSessionsUseCase) {
    viewModelScope.launch(IODispatcher) {
        unarchivedChats.unarchive(chatSessionsIds = viewState.value.selectedChats)
        updateViewState {
            it.copy(
                selectedChats = emptyList()
            )
        }
    }
}

fun ArchiveScreenViewModel.deleteChats(deleteChatSessions: DeleteChatSessionsUseCase) {
    viewModelScope.launch(IODispatcher) {
        deleteChatSessions.delete(chatSessionsIds = viewState.value.selectedChats)
        updateViewState {
            it.copy(
                selectedChats = emptyList(),
                isShowAlertDialogDeleteChat = false
            )
        }
    }
}

fun ArchiveScreenViewModel.updateTypingStatusForChats() {

}

fun ArchiveScreenViewModel.showAlertDialogDeleteChat() {
    updateViewState {
        it.copy(isShowAlertDialogDeleteChat = true)
    }
}

fun ArchiveScreenViewModel.closeAlertDialog() {
    updateViewState {
        it.copy(isShowAlertDialogDeleteChat = false)
    }
}