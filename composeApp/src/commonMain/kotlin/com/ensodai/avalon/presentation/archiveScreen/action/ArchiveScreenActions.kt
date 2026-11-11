package com.ensodai.avalon.presentation.archiveScreen.action

import com.ensodai.avalon.presentation.core.BaseActions
import kotlin.uuid.Uuid

data class ArchiveScreenActions(
    val unarchiveChats: () -> Unit,
    val onSelectChat: (Uuid) -> Unit,
    val onEnableSelectionChatsModel: (Uuid) -> Unit,
    val onDeleteSelectedChats: () -> Unit,
    val onDisableSelectedChats: () -> Unit,
    val onCloseAlertDialog: () -> Unit,
    val onShowAlertDialogDeleteShat: () -> Unit
) : BaseActions()
