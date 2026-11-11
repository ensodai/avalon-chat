package com.ensodai.avalon.presentation.editWelcomeCardScreen.action

import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import com.ensodai.avalon.presentation.commonComponents.welcomCard.CardColor
import com.ensodai.avalon.presentation.core.BaseActions
import io.github.vinceglb.filekit.PlatformFile

data class EditWelcomeCardScreenActions(
    val onChangeDescription: (String) -> Unit,
    val onAttachImages: (List<PlatformFile>) -> Unit,
    val onShowPermissionDialog: (Boolean) -> Unit,
    val onDeleteImage: (WelcomeCardImageDto) -> Unit,
    val onDeletePendingImage: (String) -> Unit,
    val onSelectStartTone: (CardColor) -> Unit,
    val onSelectEndTone: (CardColor) -> Unit,
    val onToggleLight: () -> Unit,
    val onSaveChanges: () -> Unit
) : BaseActions()
