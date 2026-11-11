package com.ensodai.avalon.presentation.editCharacterAvatarScreen

import com.ensodai.avalon.domain.useCases.commonUseCase.UploadPickedImageUseCase
import com.ensodai.avalon.domain.useCases.editCharacterAvatarScreenUseCase.GetCharacterImagesUseCase
import com.ensodai.avalon.domain.useCases.editCharacterAvatarScreenUseCase.UpdateCharacterImagesUseCase
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.action.EditCharacterAvatarScreenActions
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.action.attachImages
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.action.createViewState
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.action.deleteImage
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.action.deletePendingImage
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.action.selectAvatar
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.action.showPermissionDialog
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.action.updateImages
import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.viewState.EditCharacterAvatarScreenViewState
import kotlinx.coroutines.Job
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
class EditCharacterAvatarScreenViewModel(
    @InjectedParam val characterId: Uuid,
    private val getCharacterImages: GetCharacterImagesUseCase,
    private val updateCharacterImages: UpdateCharacterImagesUseCase,
    private val uploadPickedImageUseCase: UploadPickedImageUseCase

) : BaseViewModel<EditCharacterAvatarScreenViewState, EditCharacterAvatarScreenActions>(
    initialState = EditCharacterAvatarScreenViewState()
) {

    private val uploadJobs = mutableMapOf<String, Job>()

    override val actions = EditCharacterAvatarScreenActions(
        onDeleteImage = { deleteImage(image = it) },
        onDeletePendingImage = { deletePendingImage(it) },
        onAttachImages = {
            attachImages(
                files = it,
                uploadPickedImageUseCase = uploadPickedImageUseCase
            )
        },
        onSelectImageAvatar = { selectAvatar(avatar = it) },
        onShowPermissionDialog = { showPermissionDialog(it) },
        onUpdateImages = {
            updateImages(
                updateCharacterImages = updateCharacterImages,
                characterId = characterId
            )
        },
        onUpdateImagePrivacy = { image, isPrivate, energyCost, requiredLevel ->
            updateImagePrivacy(image, isPrivate, energyCost, requiredLevel)
        },
        onSelectImageToConfig = { selectImageToConfig(it) }
    )

    fun selectImageToConfig(image: CharacterImageDTO?) {
        updateViewState { it.copy(selectedImageToConfig = image) }
    }

    fun updateImagePrivacy(
        image: CharacterImageDTO,
        isPrivate: Boolean,
        energyCost: Int,
        requiredLevel: Int?
    ) {
        updateViewState { state ->
            val updatedImages = state.characterImages.map { img ->
                if (img.imagePublicId == image.imagePublicId) {
                    img.copy(
                        isPrivate = isPrivate,
                        energyCost = energyCost,
                        requiredLevel = requiredLevel
                    )
                } else img
            }
            val updatedAttached = state.attachedImages.map { attached ->
                if (attached.image.imagePublicId == image.imagePublicId) {
                    attached.copy(
                        image = attached.image.copy(
                            isPrivate = isPrivate,
                            energyCost = energyCost,
                            requiredLevel = requiredLevel
                        )
                    )
                } else attached
            }
            state.copy(
                characterImages = updatedImages,
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
