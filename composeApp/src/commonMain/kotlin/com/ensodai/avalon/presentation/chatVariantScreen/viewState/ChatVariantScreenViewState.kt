package com.ensodai.avalon.presentation.chatVariantScreen.viewState

import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.data.db.dto.ChatVariant
import com.ensodai.avalon.presentation.core.BaseViewState
import com.ensodai.avalon.presentation.core.LoadingState
import kotlin.uuid.Uuid

data class ChatVariantScreenViewState(
    val selectedStory: ChatVariant? = null,
    val bottomSheetState: BottomSheetState = BottomSheetState(),
    val loadingState: LoadingState = LoadingState.Idle,
    val alertDialog: AlertDialog = AlertDialog.Closed,
    val characterId: Uuid = Uuid.random(),
    val activeChatVariantId: Uuid? = null
) : BaseViewState()

data class BottomSheetState(
    val selectedStory: ChatVariant? = null,
    val attachedImage: CharacterImageDTO? = null,
    val isEnableDeleteButton: Boolean = false,
    val bottomSheet: BottomSheet = BottomSheet.Closed
)

sealed class BottomSheet {
    object Closed : BottomSheet()
    object MainMenuBottomSheet : BottomSheet()
    object RenameBottomSheet : BottomSheet()
    object DeleteBottomSheet : BottomSheet()
    object UpdateImageBottomSheet : BottomSheet()

    object ClearChatStoryBottomSheet : BottomSheet()
}

sealed class AlertDialog {
    object Closed : AlertDialog()
    object PermissionDialog : AlertDialog()
}