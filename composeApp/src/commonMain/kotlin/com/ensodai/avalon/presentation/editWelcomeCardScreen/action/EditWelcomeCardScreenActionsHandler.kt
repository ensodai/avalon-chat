package com.ensodai.avalon.presentation.editWelcomeCardScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import com.ensodai.avalon.domain.useCases.commonUseCase.UploadPickedImageUseCase
import com.ensodai.avalon.domain.useCases.editWelcomeCardUseCase.GetCharacterWelcomeCardUseCase
import com.ensodai.avalon.domain.useCases.editWelcomeCardUseCase.UpdateWelcomeCardConfig
import com.ensodai.avalon.domain.useCases.editWelcomeCardUseCase.UpdateWelcomeCardUseCase
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.PendingImageUpload
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.UploadedWelcomeCardImageRef
import com.ensodai.avalon.presentation.commonComponents.welcomCard.CardColor
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.editWelcomeCardScreen.EditWelcomeCardScreenViewModel
import com.ensodai.avalon.utils.IODispatcher
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

fun EditWelcomeCardScreenViewModel.createViewState(
    characterId: Uuid,
    getCharacterWelcomeCard: GetCharacterWelcomeCardUseCase
) {
    viewModelScope.launch(IODispatcher) {
        getCharacterWelcomeCard
            .get(characterId)
            .collect { welcomeCard ->
                if (welcomeCard != null) {
                    updateViewState {
                        it.copy(
                            welcomeCardDescription = welcomeCard.welcomeCardDescription,
                            welcomeCardImages = welcomeCard.welcomeCardImages ?: emptyList(),
                            startColor = CardColor.fromKey(welcomeCard.startColor),
                            endColor = CardColor.fromKey(welcomeCard.endColor),
                            colorLight = CardColor.fromKey(welcomeCard.colorLight),
                            isEnableLight = welcomeCard.isEnableLight
                        )
                    }
                }
            }
    }
}

fun EditWelcomeCardScreenViewModel.onChangeDescription(description: String) {
    updateViewState {
        it.copy(
            welcomeCardDescription = description
        )
    }
}

fun EditWelcomeCardScreenViewModel.onAttachImages(
    files: List<PlatformFile>,
    uploadPickedImageUseCase: UploadPickedImageUseCase
) {
    val currentState = viewState.value
    val availableSlots =
        (3 - currentState.welcomeCardImages.size - currentState.pendingImageUploads.size)
            .coerceAtLeast(0)

    files.take(availableSlots).forEach { file ->
        val pendingId = Uuid.random().toString()
        updateViewState { state ->
            state.copy(
                pendingImageUploads = state.pendingImageUploads + PendingImageUpload(
                    id = pendingId,
                    fileName = "image-${pendingId.take(8)}"
                )
            )
        }

        val job = viewModelScope.launch(IODispatcher) {
            uploadPickedImageUseCase.uploadWelcomeCardImage(file).fold(
                onSuccess = { uploaded ->
                    if (viewState.value.pendingImageUploads.none { it.id == pendingId }) {
                        return@fold
                    }
                    val imageDto = WelcomeCardImageDto(
                        imagePublicId = Uuid.parse(uploaded.localId),
                        url = uploaded.imageUrl,
                        thumbHash = uploaded.thumbHash
                    )
                    updateViewState { state ->
                        state.copy(
                            welcomeCardImages = state.welcomeCardImages + imageDto,
                            attachedImages = state.attachedImages + UploadedWelcomeCardImageRef(
                                image = imageDto,
                                uploadToken = uploaded.uploadToken
                            ),
                            pendingImageUploads = state.pendingImageUploads.filterNot { it.id == pendingId }
                        )
                    }
                },
                onFailure = { error ->
                    if (error is CancellationException) return@fold
                    updateViewState { state ->
                        state.copy(
                            pendingImageUploads = state.pendingImageUploads.filterNot { it.id == pendingId },
                            loadingState = LoadingState.Error
                        )
                    }
                }
            )
        }
        registerUploadJob(pendingId, job)
        job.invokeOnCompletion {
            clearUploadJob(pendingId)
        }
    }
}

fun EditWelcomeCardScreenViewModel.onDeleteImage(welcomeCard: WelcomeCardImageDto) {
    updateViewState { state ->
        val updatedWelcomeCardImages = state.welcomeCardImages.filter { it != welcomeCard }
        val deletedImages = state.deletedImages
            .toMutableList()
            .apply {
                if (state.attachedImages.any { it.image == welcomeCard }) {
                    remove(welcomeCard)
                } else {
                    add(welcomeCard)
                }
            }
        val attachedImages = state.attachedImages
            .toMutableList()
            .apply {
                removeAll { it.image == welcomeCard }
            }

        state.copy(
            welcomeCardImages = updatedWelcomeCardImages,
            attachedImages = attachedImages,
            deletedImages = deletedImages
        )
    }
}

fun EditWelcomeCardScreenViewModel.onDeletePendingImage(pendingId: String) {
    cancelUploadJob(pendingId)
    updateViewState { state ->
        state.copy(
            pendingImageUploads = state.pendingImageUploads.filterNot { it.id == pendingId }
        )
    }
}

fun EditWelcomeCardScreenViewModel.onShowPermissionDialog(isShow: Boolean) {
    updateViewState { it.copy(isShowPermissionDialog = isShow) }
}

fun EditWelcomeCardScreenViewModel.onSelectStartTone(color: CardColor) {
    updateViewState {
        it.copy(startColor = color)
    }
}

fun EditWelcomeCardScreenViewModel.onSelectEndTone(color: CardColor) {
    updateViewState {
        it.copy(endColor = color)
    }
}

fun EditWelcomeCardScreenViewModel.onToggleLight() {
    updateViewState {
        it.copy(isEnableLight = !it.isEnableLight)
    }
}

fun EditWelcomeCardScreenViewModel.onSaveChanges(
    characterId: Uuid?,
    updateWelcomeCard: UpdateWelcomeCardUseCase
) {
    if (characterId == null) return
    if (viewState.value.pendingImageUploads.isNotEmpty()) return
    val currentState = viewState.value
    val welcomeCardConfig = UpdateWelcomeCardConfig(
        deletedImages = currentState.deletedImages.map { it.imagePublicId },
        welcomeCardDescription = currentState.welcomeCardDescription,
        startColor = currentState.startColor.key,
        endColor = currentState.endColor.key,
        colorLight = currentState.startColor.key,
        isEnableLight = currentState.isEnableLight
    )
    viewModelScope.launch(IODispatcher) {
        updateWelcomeCard
            .update(
                attachedImages = currentState.attachedImages,
                characterId = characterId,
                welcomeCardConfig = welcomeCardConfig
            ).fold(
                onSuccess = {
                    updateViewState {
                        it.copy(loadingState = LoadingState.Success)
                    }
                },
                onFailure = {
                    updateViewState {
                        it.copy(loadingState = LoadingState.Error)
                    }
                }
            )
    }
}
