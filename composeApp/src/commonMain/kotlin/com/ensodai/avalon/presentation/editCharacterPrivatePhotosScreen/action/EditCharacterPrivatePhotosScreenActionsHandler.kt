package com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.domain.useCases.commonUseCase.UploadPickedImageUseCase
import com.ensodai.avalon.domain.useCases.editCharacterAvatarScreenUseCase.GetCharacterImagesUseCase
import com.ensodai.avalon.domain.useCases.editCharacterAvatarScreenUseCase.UpdateCharacterImagesUseCase
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.PendingImageUpload
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen.EditCharacterPrivatePhotosScreenViewModel
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.viewState.AttachedCharacterImageUpload
import com.ensodai.avalon.utils.IODispatcher
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

fun EditCharacterPrivatePhotosScreenViewModel.createViewState(
    characterId: Uuid,
    getCharacterImages: GetCharacterImagesUseCase
) {
    viewModelScope.launch(IODispatcher) {
        val images = getCharacterImages.get(characterId).first()
        val avatar = images.avatar
        val gallery = images.images
        val fullList = gallery.plus(avatar).filterNotNull()
        
        // Фильтруем только те, которые помечены как приватные
        val privateList = fullList.filter { it.isPrivate }
        
        updateViewState { viewState ->
            viewState.copy(
                characterAvatar = avatar,
                allImages = fullList,
                characterImages = privateList
            )
        }
    }
}

fun EditCharacterPrivatePhotosScreenViewModel.deleteImage(image: CharacterImageDTO) {
    updateViewState { state ->
        val currentPrivateImages = state.characterImages
        val currentAllImages = state.allImages
        
        val updatedPrivateImages = currentPrivateImages.filter { it.imagePublicId != image.imagePublicId }
        val updatedAllImages = currentAllImages.filter { it.imagePublicId != image.imagePublicId }
        
        val deletedImages = state.deletedImages
            .toMutableList()
            .apply {
                if (!state.attachedImages.any { it.image.imagePublicId == image.imagePublicId }) {
                    add(image)
                }
            }
        val attachedImages = state.attachedImages
            .toMutableList()
            .apply {
                removeAll { it.image.imagePublicId == image.imagePublicId }
            }

        state.copy(
            characterImages = updatedPrivateImages,
            allImages = updatedAllImages,
            attachedImages = attachedImages,
            deletedImages = deletedImages
        )
    }
}

fun EditCharacterPrivatePhotosScreenViewModel.attachImages(
    files: List<PlatformFile>,
    uploadPickedImageUseCase: UploadPickedImageUseCase
) {
    val currentState = viewState.value
    val availableSlots = (100 - currentState.allImages.size - currentState.pendingImageUploads.size)
        .coerceAtLeast(0)

    files.take(availableSlots).forEach { file ->
        val pendingId = Uuid.random().toString()
        updateViewState { state ->
            state.copy(
                pendingImageUploads = state.pendingImageUploads + PendingImageUpload(
                    id = pendingId,
                    fileName = "private-${pendingId.take(8)}"
                )
            )
        }

        val job = viewModelScope.launch(IODispatcher) {
            uploadPickedImageUseCase.uploadCharacterImage(file).fold(
                onSuccess = { uploaded ->
                    if (viewState.value.pendingImageUploads.none { it.id == pendingId }) {
                        return@fold
                    }
                    // По умолчанию картинка, загруженная на этом экране, приватная
                    val imageDto = CharacterImageDTO(
                        imagePublicId = Uuid.parse(uploaded.localId),
                        url = uploaded.imageUrl,
                        thumbHash = uploaded.thumbHash,
                        isPrivate = true,
                        energyCost = 50
                    )
                    val attachedImage = AttachedCharacterImageUpload(
                        image = imageDto,
                        uploadToken = uploaded.uploadToken
                    )
                    updateViewState { state ->
                        state.copy(
                            characterImages = state.characterImages + imageDto,
                            allImages = state.allImages + imageDto,
                            attachedImages = state.attachedImages + attachedImage,
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

fun EditCharacterPrivatePhotosScreenViewModel.deletePendingImage(pendingId: String) {
    cancelUploadJob(pendingId)
    updateViewState { state ->
        state.copy(
            pendingImageUploads = state.pendingImageUploads.filterNot { it.id == pendingId }
        )
    }
}

fun EditCharacterPrivatePhotosScreenViewModel.showPermissionDialog(isShow: Boolean) {
    updateViewState {
        it.copy(isShowPermissionDialog = isShow)
    }
}

fun EditCharacterPrivatePhotosScreenViewModel.updateImages(
    updateCharacterImages: UpdateCharacterImagesUseCase,
    characterId: Uuid?
) {
    if (characterId == null) return
    if (viewState.value.pendingImageUploads.isNotEmpty()) return
    updateViewState {
        it.copy(loadingState = LoadingState.Loading)
    }
    viewModelScope.launch(IODispatcher) {
        updateCharacterImages.update(
            characterId = characterId,
            attachedImages = viewState.value.attachedImages,
            deletedImages = viewState.value.deletedImages,
            avatar = viewState.value.characterAvatar,
            characterImages = viewState.value.allImages // Отправляем allImages, чтобы не удалить аватар и публичные фото!
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
