package com.ensodai.avalon.presentation.lorebookDetailsScreen.viewState

import com.ensodai.shared.avalon.character.models.CharacterLorebookDetailsDto
import com.ensodai.avalon.presentation.core.BaseViewState
import kotlin.uuid.Uuid

data class LorebookDetailsScreenViewState(
    val lorebookId: Uuid,
    val lorebook: CharacterLorebookDetailsDto? = null,
    val isLoading: Boolean = true,
    val isOwner: Boolean = false,
    val isSavedToLibrary: Boolean = false,
    val isShowComplaintDialog: Boolean = false,
    val complaintText: String = "",
    val isSubmittingReport: Boolean = false,
    val isForking: Boolean = false,
    val feedbackMessage: String? = null
) : BaseViewState()
