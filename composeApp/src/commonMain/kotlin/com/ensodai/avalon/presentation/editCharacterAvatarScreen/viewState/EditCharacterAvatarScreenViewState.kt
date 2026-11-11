package com.ensodai.avalon.presentation.editCharacterAvatarScreen.viewState

import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.PendingImageUpload
import com.ensodai.avalon.presentation.core.BaseViewState
import com.ensodai.avalon.presentation.core.LoadingState

data class AttachedCharacterImageUpload(
    val image: CharacterImageDTO,
    val uploadToken: String
)

data class EditCharacterAvatarScreenViewState(
    val isShowPermissionDialog: Boolean = false,
    val characterAvatar: CharacterImageDTO? = null,
    val characterImages: List<CharacterImageDTO> = emptyList(),
    val allImages: List<CharacterImageDTO> = emptyList(),
    val attachedImages: List<AttachedCharacterImageUpload> = emptyList(),
    val deletedImages: List<CharacterImageDTO> = emptyList(),
    val pendingImageUploads: List<PendingImageUpload> = emptyList(),
    val loadingState: LoadingState = LoadingState.Idle,
    val selectedImageToConfig: CharacterImageDTO? = null
) : BaseViewState()
