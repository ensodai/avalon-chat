package com.ensodai.avalon.presentation.archiveScreen

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.data.db.models.ChatSessionPreview
import com.ensodai.avalon.domain.useCases.archiveScreenUseCase.GetArchivedChatsUseCase
import com.ensodai.avalon.domain.useCases.commonUseCase.ArchiveChatSessionsUseCase
import com.ensodai.avalon.domain.useCases.mainScreenUseCase.DeleteChatSessionsUseCase
import com.ensodai.avalon.presentation.archiveScreen.action.ArchiveScreenActions
import com.ensodai.avalon.presentation.archiveScreen.action.closeAlertDialog
import com.ensodai.avalon.presentation.archiveScreen.action.deleteChats
import com.ensodai.avalon.presentation.archiveScreen.action.disableSelectedChats
import com.ensodai.avalon.presentation.archiveScreen.action.enableSelectionChatsModel
import com.ensodai.avalon.presentation.archiveScreen.action.selectChat
import com.ensodai.avalon.presentation.archiveScreen.action.showAlertDialogDeleteChat
import com.ensodai.avalon.presentation.archiveScreen.action.unarchiveChats
import com.ensodai.avalon.presentation.archiveScreen.action.updateTypingStatusForChats
import com.ensodai.avalon.presentation.archiveScreen.viewState.ArchiveScreenViewState
import com.ensodai.avalon.presentation.core.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ArchiveScreenViewModel(
    private val getArchivedChats: GetArchivedChatsUseCase,
    private val archivedChats: ArchiveChatSessionsUseCase,
    private val deleteChatConfigAndMessage: DeleteChatSessionsUseCase
    ) : BaseViewModel<ArchiveScreenViewState, ArchiveScreenActions>(
    initialState = ArchiveScreenViewState()
) {

    val chats: StateFlow<List<ChatSessionPreview>> = getArchivedChats.get()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    override val actions = ArchiveScreenActions(
        unarchiveChats = { unarchiveChats(unarchivedChats = archivedChats) },
        onSelectChat = { selectChat(chatId = it) },
        onEnableSelectionChatsModel = { enableSelectionChatsModel(chatId = it) },
        onDeleteSelectedChats = { deleteChats(deleteChatSessions = deleteChatConfigAndMessage) },
        onDisableSelectedChats = { disableSelectedChats() },
        onCloseAlertDialog = { closeAlertDialog() },
        onShowAlertDialogDeleteShat = { showAlertDialogDeleteChat() },
    )

    init {
        updateTypingStatusForChats()
    }

}
