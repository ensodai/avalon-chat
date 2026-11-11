package com.ensodai.avalon.presentation.chatVariantScreen

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.data.db.dto.ChatVariant
import com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase.ActivateChatVariantUseCase
import com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase.ClearChatHistoryUseCase
import com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase.DeleteChatVariantUseCase
import com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase.GetActiveChatVariantFlowUseCase
import com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase.GetChatVariantsFlowUseCase
import com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase.GetChatVariantsUseCase
import com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase.UpdateChatVariantUseCase
import com.ensodai.avalon.presentation.chatVariantScreen.action.AlertDialogActions
import com.ensodai.avalon.presentation.chatVariantScreen.action.BottomSheetActions
import com.ensodai.avalon.presentation.chatVariantScreen.action.ChatVariantScreenActions
import com.ensodai.avalon.presentation.chatVariantScreen.action.activateChatVariant
import com.ensodai.avalon.presentation.chatVariantScreen.action.attachImages
import com.ensodai.avalon.presentation.chatVariantScreen.action.clearChatHistory
import com.ensodai.avalon.presentation.chatVariantScreen.action.closeDialog
import com.ensodai.avalon.presentation.chatVariantScreen.action.closeMenuBottomSheet
import com.ensodai.avalon.presentation.chatVariantScreen.action.createViewState
import com.ensodai.avalon.presentation.chatVariantScreen.action.deleteStory
import com.ensodai.avalon.presentation.chatVariantScreen.action.detachImage
import com.ensodai.avalon.presentation.chatVariantScreen.action.renameStory
import com.ensodai.avalon.presentation.chatVariantScreen.action.showClearStoryChatBottomSheet
import com.ensodai.avalon.presentation.chatVariantScreen.action.showDeleteStoryBottomSheet
import com.ensodai.avalon.presentation.chatVariantScreen.action.showMenuBottomSheet
import com.ensodai.avalon.presentation.chatVariantScreen.action.showPermissionDialog
import com.ensodai.avalon.presentation.chatVariantScreen.action.showRenameBottomSheet
import com.ensodai.avalon.presentation.chatVariantScreen.action.showUpdateImageBottomSheet
import com.ensodai.avalon.presentation.chatVariantScreen.action.updateActiveVariantId
import com.ensodai.avalon.presentation.chatVariantScreen.action.updateImage
import com.ensodai.avalon.presentation.chatVariantScreen.action.updateIsEnableDeleteChatVariantButton
import com.ensodai.avalon.presentation.chatVariantScreen.viewState.ChatVariantScreenViewState
import com.ensodai.avalon.presentation.core.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
class ChatVariantScreenViewModel(
    private val getChatVariants: GetChatVariantsUseCase,
    private val deleteChatVariant: DeleteChatVariantUseCase,
    private val clearChatHistory: ClearChatHistoryUseCase,
    private val updateChatVariant: UpdateChatVariantUseCase,
    private val activateChatVariant: ActivateChatVariantUseCase,
    private val getChatVariantsFlow: GetChatVariantsFlowUseCase,
    @InjectedParam val characterId: Uuid?,
    private val getActiveChatVariantFlow: GetActiveChatVariantFlowUseCase
) : BaseViewModel<ChatVariantScreenViewState, ChatVariantScreenActions>(
    initialState = ChatVariantScreenViewState()
) {

    val chatVariants: StateFlow<List<ChatVariant>> = flow {
        emitAll(getChatVariants.get(characterId))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    override val actions = ChatVariantScreenActions(
        onShowBottomSheet = { selectedStory -> showMenuBottomSheet(selectedStory) },
        bottomSheetActions = BottomSheetActions(
            onCloseBottomSheet = { closeMenuBottomSheet() },
            onShowRenameBottomSheet = { showRenameBottomSheet() },
            onRenameStory = { storyName -> renameStory(storyName, updateChatVariant) },
            onShowUpdateImageBottomSheet = { showUpdateImageBottomSheet() },
            onShowDeleteStoryBottomSheet = { showDeleteStoryBottomSheet() },
            onUpdateImage = { updateImage(updateChatVariant) },
            onDeleteStory = { deleteStory(deleteChatVariant) },
            onShowClearStoryChatBottomSheet = { showClearStoryChatBottomSheet() },
            onClearStoryChat = { clearChatHistory(clearChatHistory) },
            onAttachImage = { image -> attachImages(image) },
            onShowPermissionDialog = { showPermissionDialog() },
            onDetachImage = { detachImage() }
        ),
        alertDialogActions = AlertDialogActions(
            onCloseDialog = { closeDialog() }
        ),
        activateChatVariant = { chatVariant ->
            activateChatVariant(
                chatVariant,
                activateChatVariant
            )
        }
    )

    init {
        updateIsEnableDeleteChatVariantButton(getChatVariantsFlow)
        if (characterId != null) {
            createViewState(
                characterId = characterId
            )
            updateActiveVariantId(
                characterId = characterId,
                getActiveChatVariantFlow = getActiveChatVariantFlow
            )
        }
    }
}
