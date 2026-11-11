package com.ensodai.avalon.presentation.editCharacterAvatarScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.domain.useCases.commonUseCase.UploadPickedImageUseCase
import com.ensodai.avalon.domain.useCases.editCharacterAvatarScreenUseCase.GetCharacterImagesUseCase
import com.ensodai.avalon.domain.useCases.editCharacterAvatarScreenUseCase.UpdateCharacterImagesUseCase
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.PendingImageUpload
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.EditCharacterAvatarScreenViewModel
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.viewState.AttachedCharacterImageUpload
import com.ensodai.avalon.utils.IODispatcher
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

fun EditCharacterAvatarScreenViewModel.createViewState(
    characterId: Uuid,
    getCharacterImages: GetCharacterImagesUseCase
) {

    viewModelScope.launch(IODispatcher) {
        getCharacterImages
            .get(characterId)
            .collect { images ->
                val avatar = images.avatar
                val gallery = images.images
                val fullList = gallery.plus(avatar).filterNotNull().distinctBy { it.imagePublicId }
                
                // Для экрана аватаров фильтруем только не-приватные картинки
                val publicList = fullList.filter { !it.isPrivate }
                
                updateViewState { viewState ->
                    viewState.copy(
                        characterAvatar = avatar,
                        characterImages = publicList,
                        allImages = fullList
                    )
                }
            }
    }
}

fun EditCharacterAvatarScreenViewModel.deleteImage(image: CharacterImageDTO) {
    updateViewState { state ->
        val currentCharacterImages = state.characterImages
        val currentAllImages = state.allImages
        val isCurrentAvatarDeleted = state.characterAvatar == image
        val imageIndexToDelete = currentCharacterImages.indexOf(image)
        
        val updatedCharacterImages = currentCharacterImages.filter { it != image }
        val updatedAllImages = currentAllImages.filter { it != image }
        
        val deletedImages = state.deletedImages
            .toMutableList()
            .apply {
                if (state.attachedImages.any { it.image == image }) {
                    remove(image)
                } else {
                    add(image)
                }
            }
        val attachedImages = state.attachedImages
            .toMutableList()
            .apply {
                removeAll { it.image == image }
            }
        val newCharacterAvatar = if (isCurrentAvatarDeleted) {
            if (updatedCharacterImages.isNotEmpty()) {
                if (imageIndexToDelete > 0) {
                    updatedCharacterImages[imageIndexToDelete - 1]
                } else if (updatedCharacterImages.size > 1) {
                    updatedCharacterImages[0]
                } else {
                    updatedCharacterImages[0]
                }
            } else {
                null
            }
        } else {
            state.characterAvatar
        }

        state.copy(
            characterImages = updatedCharacterImages.ifEmpty { emptyList() },
            allImages = updatedAllImages,
            characterAvatar = newCharacterAvatar,
            attachedImages = attachedImages,
            deletedImages = deletedImages
        )
    }
}

fun EditCharacterAvatarScreenViewModel.attachImages(
    files: List<PlatformFile>,
    uploadPickedImageUseCase: UploadPickedImageUseCase
) {
    val currentState = viewState.value
    val availableSlots =
        (50 - currentState.allImages.size - currentState.pendingImageUploads.size)
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
            uploadPickedImageUseCase.uploadCharacterImage(file).fold(
                onSuccess = { uploaded ->
                    if (viewState.value.pendingImageUploads.none { it.id == pendingId }) {
                        return@fold
                    }
                    val imageDto = CharacterImageDTO(
                        imagePublicId = Uuid.parse(uploaded.localId),
                        url = uploaded.imageUrl,
                        thumbHash = uploaded.thumbHash,
                        isPrivate = false,
                        energyCost = 0
                    )
                    val attachedImage = AttachedCharacterImageUpload(
                        image = imageDto,
                        uploadToken = uploaded.uploadToken
                    )
                    updateViewState { state ->
                        state.copy(
                            characterImages = state.characterImages + imageDto,
                            allImages = state.allImages + imageDto,
                            characterAvatar = state.characterAvatar ?: imageDto,
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

fun EditCharacterAvatarScreenViewModel.deletePendingImage(pendingId: String) {
    cancelUploadJob(pendingId)
    updateViewState { state ->
        state.copy(
            pendingImageUploads = state.pendingImageUploads.filterNot { it.id == pendingId }
        )
    }
}

fun EditCharacterAvatarScreenViewModel.selectAvatar(avatar: CharacterImageDTO) {
    updateViewState {
        it.copy(characterAvatar = avatar)
    }
}

fun EditCharacterAvatarScreenViewModel.showPermissionDialog(isShow: Boolean) {
    updateViewState {
        it.copy(isShowPermissionDialog = isShow)
    }
}

fun EditCharacterAvatarScreenViewModel.updateImages(
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
            characterImages = viewState.value.allImages // Отправляем allImages, чтобы не затереть приватные картинки на бэкенде!
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
