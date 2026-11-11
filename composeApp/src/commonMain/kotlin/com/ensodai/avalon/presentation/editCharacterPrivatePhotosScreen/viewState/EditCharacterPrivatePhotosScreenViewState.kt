package com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen.viewState

import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.PendingImageUpload
import com.ensodai.avalon.presentation.core.BaseViewState
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.editCharacterAvatarScreen.viewState.AttachedCharacterImageUpload

data class EditCharacterPrivatePhotosScreenViewState(
    val isShowPermissionDialog: Boolean = false,
    val characterAvatar: CharacterImageDTO? = null,
    val allImages: List<CharacterImageDTO> = emptyList(), // Полный список всех картинок (публичных и приватных)
    val characterImages: List<CharacterImageDTO> = emptyList(), // Только приватные картинки для UI
    val attachedImages: List<AttachedCharacterImageUpload> = emptyList(),
    val deletedImages: List<CharacterImageDTO> = emptyList(),
    val pendingImageUploads: List<PendingImageUpload> = emptyList(),
    val loadingState: LoadingState = LoadingState.Idle,
    val selectedImageToConfig: CharacterImageDTO? = null
) : BaseViewState()
