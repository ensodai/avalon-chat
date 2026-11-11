package com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen

import com.ensodai.avalon.domain.useCases.commonUseCase.UploadPickedImageUseCase
import com.ensodai.avalon.domain.useCases.editCharacterAvatarScreenUseCase.GetCharacterImagesUseCase
import com.ensodai.avalon.domain.useCases.editCharacterAvatarScreenUseCase.UpdateCharacterImagesUseCase
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen.action.EditCharacterPrivatePhotosScreenActions
import com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen.action.attachImages
import com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen.action.createViewState
import com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen.action.deleteImage
import com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen.action.deletePendingImage
import com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen.action.showPermissionDialog
import com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen.action.updateImages
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen.viewState.EditCharacterPrivatePhotosScreenViewState
import kotlinx.coroutines.Job
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
class EditCharacterPrivatePhotosScreenViewModel(
    @InjectedParam val characterId: Uuid,
    private val getCharacterImages: GetCharacterImagesUseCase,
    private val updateCharacterImages: UpdateCharacterImagesUseCase,
    private val uploadPickedImageUseCase: UploadPickedImageUseCase
) : BaseViewModel<EditCharacterPrivatePhotosScreenViewState, EditCharacterPrivatePhotosScreenActions>(
    initialState = EditCharacterPrivatePhotosScreenViewState()
) {

    private val uploadJobs = mutableMapOf<String, Job>()

    override val actions = EditCharacterPrivatePhotosScreenActions(
        onDeleteImage = { deleteImage(image = it) },
        onDeletePendingImage = { deletePendingImage(it) },
        onAttachImages = {
            attachImages(
                files = it,
                uploadPickedImageUseCase = uploadPickedImageUseCase
            )
        },
        onShowPermissionDialog = { showPermissionDialog(it) },
        onUpdateImages = {
            updateImages(
                updateCharacterImages = updateCharacterImages,
                characterId = characterId
            )
        },
        onUpdateImagePrice = { image, price ->
            updateImagePrice(image, price)
        },
        onSelectImageToConfig = { selectImageToConfig(it) }
    )

    fun selectImageToConfig(image: CharacterImageDTO?) {
        updateViewState { it.copy(selectedImageToConfig = image) }
    }

    fun updateImagePrice(
        image: CharacterImageDTO,
        price: Int
    ) {
        updateViewState { state ->
            val updatedImages = state.characterImages.map { img ->
                if (img.imagePublicId == image.imagePublicId) {
                    img.copy(
                        isPrivate = true,
                        energyCost = price,
                        requiredLevel = null
                    )
                } else img
            }
            val updatedAllImages = state.allImages.map { img ->
                if (img.imagePublicId == image.imagePublicId) {
                    img.copy(
                        isPrivate = true,
                        energyCost = price,
                        requiredLevel = null
                    )
                } else img
            }
            val updatedAttached = state.attachedImages.map { attached ->
                if (attached.image.imagePublicId == image.imagePublicId) {
                    attached.copy(
                        image = attached.image.copy(
                            isPrivate = true,
                            energyCost = price,
                            requiredLevel = null
                        )
                    )
                } else attached
            }
            state.copy(
                characterImages = updatedImages,
                allImages = updatedAllImages,
                attachedImages = updatedAttached
            )
        }
    }

    init {
        createViewState(
            characterId = characterId,
            getCharacterImages = getCharacterImages
        )
    }

    fun registerUploadJob(pendingId: String, job: Job) {
        uploadJobs[pendingId] = job
    }

    fun clearUploadJob(pendingId: String) {
        uploadJobs.remove(pendingId)
    }

    fun cancelUploadJob(pendingId: String) {
        uploadJobs.remove(pendingId)?.cancel()
    }

    override fun onCleared() {
        uploadJobs.values.forEach { it.cancel() }
        uploadJobs.clear()
        super.onCleared()
    }
}
