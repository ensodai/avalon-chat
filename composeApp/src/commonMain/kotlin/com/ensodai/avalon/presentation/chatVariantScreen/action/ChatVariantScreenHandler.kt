package com.ensodai.avalon.presentation.chatVariantScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.data.db.dto.ChatVariant
import com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase.ActivateChatVariantUseCase
import com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase.ClearChatHistoryUseCase
import com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase.DeleteChatVariantUseCase
import com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase.GetActiveChatVariantFlowUseCase
import com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase.GetChatVariantsFlowUseCase
import com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase.UpdateChatVariantUseCase
import com.ensodai.avalon.presentation.chatVariantScreen.ChatVariantScreenViewModel
import com.ensodai.avalon.presentation.chatVariantScreen.viewState.AlertDialog
import com.ensodai.avalon.presentation.chatVariantScreen.viewState.BottomSheet
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.utils.IODispatcher
import com.ensodai.avalon.utils.L
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

@OptIn(FlowPreview::class)
fun ChatVariantScreenViewModel.createViewState(
    characterId: Uuid
) {
    viewModelScope.launch(IODispatcher) {

        updateViewState {
            it.copy(
                characterId = characterId,
            )
        }
    }
}

fun ChatVariantScreenViewModel.showMenuBottomSheet(selectedStory: ChatVariant) {
    val imageDto = selectedStory.image?.url?.let { url ->
        CharacterImageDTO(
            imagePublicId = selectedStory.image.id ?: Uuid.random(),
            url = url,
            thumbHash = selectedStory.image.thumbHash
        )
    }
    updateViewState {
        it.copy(
            selectedStory = selectedStory,
            bottomSheetState = it.bottomSheetState.copy(
                bottomSheet = BottomSheet.MainMenuBottomSheet,
                selectedStory = selectedStory,
                attachedImage = imageDto,
                isEnableDeleteButton = chatVariants.value.size > 1
            ),
        )
    }
}

fun ChatVariantScreenViewModel.closeMenuBottomSheet() {
    updateViewState {
        it.copy(
            bottomSheetState = it.bottomSheetState.copy(
                bottomSheet = BottomSheet.Closed,
                selectedStory = null,
                attachedImage = null
            ),
        )
    }
}

fun ChatVariantScreenViewModel.showRenameBottomSheet() {
    updateViewState {
        it.copy(
            bottomSheetState = it.bottomSheetState.copy(bottomSheet = BottomSheet.RenameBottomSheet),
        )
    }
}

fun ChatVariantScreenViewModel.renameStory(
    storyName: String,
    updateChatVariant: UpdateChatVariantUseCase
) {
    val variantId = viewState.value.selectedStory?.id ?: return

    viewModelScope.launch(IODispatcher) {
        updateViewState {
            it.copy(loadingState = LoadingState.Loading)
        }
        updateChatVariant.updateName(
            name = storyName,
            variantId = variantId
        ).fold(
            onSuccess = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Success,
                    )
                }
                closeMenuBottomSheet()
            },
            onFailure = {
                updateViewState {
                    it.copy(
                        loadingState = LoadingState.Error,
                    )
                }
                closeMenuBottomSheet()
            }
        )
    }
}

fun ChatVariantScreenViewModel.showUpdateImageBottomSheet() {
    val currentImage = viewState.value.selectedStory?.image
    val imageDto = currentImage?.url?.let { url ->
        CharacterImageDTO(
            imagePublicId = currentImage.id ?: Uuid.random(),
            url = url,
            thumbHash = currentImage.thumbHash
        )
    }
    updateViewState {
        it.copy(
            bottomSheetState = it.bottomSheetState.copy(
                bottomSheet = BottomSheet.UpdateImageBottomSheet,
                attachedImage = it.bottomSheetState.attachedImage ?: imageDto
            ),
        )
    }
}

fun ChatVariantScreenViewModel.showDeleteStoryBottomSheet() {
    updateViewState {
        it.copy(
            bottomSheetState = it.bottomSheetState.copy(bottomSheet = BottomSheet.DeleteBottomSheet),
        )
    }
}

fun ChatVariantScreenViewModel.updateImage(updateChatVariant: UpdateChatVariantUseCase) {
    val newImage = viewState.value.bottomSheetState.attachedImage
    val currentImage = viewState.value.selectedStory?.image
    val variantId = viewState.value.selectedStory?.id
    if (variantId != null) {
        viewModelScope.launch(IODispatcher) {
            updateViewState {
                it.copy(loadingState = LoadingState.Loading)
            }
            updateChatVariant.updateImage(
                newImage = newImage,
                variantId = variantId,
                currentImage = currentImage
            ).fold(
                onSuccess = {
                    updateViewState {
                        it.copy(loadingState = LoadingState.Success)
                    }
                    closeMenuBottomSheet()
                },
                onFailure = {
                    updateViewState {
                        it.copy(loadingState = LoadingState.Error)
                    }
                    closeMenuBottomSheet()
                }
            )
        }
    }
}

fun ChatVariantScreenViewModel.deleteStory(deleteChatVariant: DeleteChatVariantUseCase) {
    val id = viewState.value.selectedStory?.id
    if (id != null) {
        updateViewState {
            it.copy(
                loadingState = LoadingState.Loading,
            )
        }
        viewModelScope.launch(IODispatcher) {
            deleteChatVariant.delete(id).fold(
                onSuccess = {
                    updateViewState {
                        it.copy(
                            loadingState = LoadingState.Success,
                        )
                    }
                    closeMenuBottomSheet()
                },
                onFailure = {
                    updateViewState {
                        it.copy(
                            loadingState = LoadingState.Error,
                        )
                    }
                    closeMenuBottomSheet()
                }
            )
        }
    }
}

fun ChatVariantScreenViewModel.showClearStoryChatBottomSheet() {
    updateViewState {
        it.copy(
            bottomSheetState = it.bottomSheetState.copy(bottomSheet = BottomSheet.ClearChatStoryBottomSheet),
        )
    }
}

fun ChatVariantScreenViewModel.clearChatHistory(clearChatHistory: ClearChatHistoryUseCase) {
    val id = viewState.value.bottomSheetState.selectedStory?.id
    if (id != null) {
        updateViewState {
            it.copy(
                loadingState = LoadingState.Loading,
            )
        }
        viewModelScope.launch(IODispatcher) {
            clearChatHistory.clear(id)
            updateViewState {
                it.copy(
                    loadingState = LoadingState.Success,
                )
            }
            closeMenuBottomSheet()
        }
    }
}

fun ChatVariantScreenViewModel.attachImages(image: List<CharacterImageDTO>) {
    if (image.isNotEmpty()) {
        updateViewState {
            it.copy(
                bottomSheetState = it.bottomSheetState.copy(attachedImage = image[0]),
            )
        }
    }
}

fun ChatVariantScreenViewModel.detachImage() {
    updateViewState {
        it.copy(
            bottomSheetState = it.bottomSheetState.copy(attachedImage = null),
        )
    }
}


fun ChatVariantScreenViewModel.showPermissionDialog() {
    updateViewState {
        it.copy(
            alertDialog = AlertDialog.PermissionDialog,
        )
    }
}

fun ChatVariantScreenViewModel.closeDialog() {
    updateViewState {
        it.copy(alertDialog = AlertDialog.Closed)
    }
}

fun ChatVariantScreenViewModel.activateChatVariant(
    chatVariant: ChatVariant,
    activateChatVariant: ActivateChatVariantUseCase
) {
    viewModelScope.launch(IODispatcher) {
        activateChatVariant.activate(
            characterId = viewState.value.characterId,
            chatVariantId = chatVariant.id
        )
    }
}

fun ChatVariantScreenViewModel.updateIsEnableDeleteChatVariantButton(getChatVariantsFlow: GetChatVariantsFlowUseCase) {
//    viewModelScope.launch(IODispatcher) {
//        viewState
//            .map { it.characterLocalId }
//            .distinctUntilChanged()
//            .collectLatest { characterLocalId ->
//                if (characterLocalId <= 0) return@collectLatest
//
//                getChatVariantsFlow.get(characterLocalId)
//                    .map { count -> count > 1 }
//                    .collect { isEnabled ->
//                        withContext(Dispatchers.Main) {
//                            updateViewState {
//                                it.copy(
//                                    bottomSheetState = it.bottomSheetState.copy(isEnableDeleteButton = isEnabled)
//                                )
//                            }
//                        }
//                    }
//            }
//    }
}

fun ChatVariantScreenViewModel.updateActiveVariantId(
    characterId: Uuid,
    getActiveChatVariantFlow: GetActiveChatVariantFlowUseCase
) {
    viewModelScope.launch(IODispatcher) {
        getActiveChatVariantFlow
            .get(characterId)
            .collectLatest { chatVariant ->
                L.d("ChatVariantScreenViewModel", "updateActiveVariantId: $chatVariant")
                updateViewState {
                    it.copy(activeChatVariantId = chatVariant)
                }
            }
    }
}
