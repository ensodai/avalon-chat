package com.ensodai.avalon.presentation.archiveScreen.viewState

import com.ensodai.avalon.presentation.core.BaseViewState
import com.ensodai.avalon.utils.EMPTY_STRING
import kotlin.uuid.Uuid

data class ArchiveScreenViewState(
    val empty: String = EMPTY_STRING,
    val typingCharacters: Set<Int> = emptySet(),
    val selectedChats: List<Uuid> = mutableListOf(),
    val isShowAlertDialogDeleteChat: Boolean = false
) : BaseViewState()

val ArchiveScreenViewState.isEnableSelectionChatsMode: Boolean
    get() = selectedChats.isNotEmpty()