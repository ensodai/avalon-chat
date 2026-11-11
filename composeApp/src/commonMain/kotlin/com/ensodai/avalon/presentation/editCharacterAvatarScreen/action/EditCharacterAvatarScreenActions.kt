package com.ensodai.avalon.presentation.editCharacterAvatarScreen.action

import com.ensodai.avalon.data.db.dto.CharacterImageDTO
import com.ensodai.avalon.presentation.core.BaseActions
import io.github.vinceglb.filekit.PlatformFile

data class EditCharacterAvatarScreenActions(
    val onDeleteImage: (CharacterImageDTO) -> Unit,
    val onDeletePendingImage: (String) -> Unit,
    val onAttachImages: (List<PlatformFile>) -> Unit,
    val onSelectImageAvatar: (CharacterImageDTO) -> Unit,
    val onShowPermissionDialog: (isShow: Boolean) -> Unit,
    val onUpdateImages: () -> Unit,
    val onUpdateImagePrivacy: (image: CharacterImageDTO, isPrivate: Boolean, energyCost: Int, requiredLevel: Int?) -> Unit = { _, _, _, _ -> },
    val onSelectImageToConfig: (CharacterImageDTO?) -> Unit = {}
) : BaseActions()
