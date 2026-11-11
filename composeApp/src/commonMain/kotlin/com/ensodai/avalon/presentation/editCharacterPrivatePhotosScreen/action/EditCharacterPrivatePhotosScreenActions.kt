package com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen.action

import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.presentation.core.BaseActions
import io.github.vinceglb.filekit.PlatformFile

data class EditCharacterPrivatePhotosScreenActions(
    val onDeleteImage: (CharacterImageDTO) -> Unit = {},
    val onDeletePendingImage: (String) -> Unit = {},
    val onAttachImages: (List<PlatformFile>) -> Unit = {},
    val onShowPermissionDialog: (Boolean) -> Unit = {},
    val onUpdateImages: () -> Unit = {},
    val onUpdateImagePrice: (image: CharacterImageDTO, price: Int) -> Unit = { _, _ -> },
    val onSelectImageToConfig: (CharacterImageDTO?) -> Unit = {}
) : BaseActions()
