package com.ensodai.avalon.presentation.editWelcomeCardScreen.viewState

import com.ensodai.avalon.data.db.dto.WelcomeCardImageDto
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.PendingImageUpload
import com.ensodai.avalon.presentation.addCharacterScreen.viewState.UploadedWelcomeCardImageRef
import com.ensodai.avalon.presentation.commonComponents.welcomCard.CardColor
import com.ensodai.avalon.presentation.core.BaseViewState
import com.ensodai.avalon.presentation.core.LoadingState

data class EditWelcomeCardScreenViewState(
    val isShowPermissionDialog: Boolean = false,
    val loadingState: LoadingState = LoadingState.Idle,
    val welcomeCardDescription: String? = null,
    val welcomeCardImages: List<WelcomeCardImageDto> = emptyList(),
    val attachedImages: List<UploadedWelcomeCardImageRef> = emptyList(),
    val deletedImages: List<WelcomeCardImageDto> = emptyList(),
    val pendingImageUploads: List<PendingImageUpload> = emptyList(),
    val startColor: CardColor = CardColor.DEFAULT,
    val endColor: CardColor = CardColor.DEFAULT,
    val colorLight: CardColor = CardColor.DEFAULT,
    val allowedColors: List<CardColor> = CardColor.getSelectableColors(),
    val isEnableLight: Boolean = true
) : BaseViewState()

val EditWelcomeCardScreenViewState.isShowPreviewCard: Boolean
    get() = !welcomeCardDescription.isNullOrEmpty() || welcomeCardImages.isNotEmpty()
