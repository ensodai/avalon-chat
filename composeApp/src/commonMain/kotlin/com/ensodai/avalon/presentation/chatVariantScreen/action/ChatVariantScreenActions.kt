package com.ensodai.avalon.presentation.chatVariantScreen.action

import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.data.db.dto.ChatVariant
import com.ensodai.avalon.presentation.core.BaseActions

data class ChatVariantScreenActions(
    val onShowBottomSheet: (ChatVariant) -> Unit,
    val activateChatVariant: (ChatVariant) -> Unit,
    val bottomSheetActions: BottomSheetActions,
    val alertDialogActions: AlertDialogActions
) : BaseActions()

data class AlertDialogActions(
    val onCloseDialog: () -> Unit
) : BaseActions()

data class BottomSheetActions(
    val onCloseBottomSheet: () -> Unit,
    val onShowRenameBottomSheet: () -> Unit,
    val onRenameStory: (String) -> Unit,
    val onShowUpdateImageBottomSheet: () -> Unit,
    val onShowDeleteStoryBottomSheet: () -> Unit,
    val onShowClearStoryChatBottomSheet: () -> Unit,
    val onUpdateImage: () -> Unit,
    val onDeleteStory: () -> Unit,
    val onClearStoryChat: () -> Unit,
    val onAttachImage: (List<CharacterImageDTO>) -> Unit,
    val onDetachImage: () -> Unit,
    val onShowPermissionDialog: () -> Unit
) : BaseActions()