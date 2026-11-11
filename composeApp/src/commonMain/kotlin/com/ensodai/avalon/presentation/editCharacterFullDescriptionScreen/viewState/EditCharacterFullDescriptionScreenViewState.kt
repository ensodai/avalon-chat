package com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen.viewState

import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.avalon.presentation.core.BaseViewState
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.utils.EMPTY_STRING

data class EditCharacterFullDescriptionScreenViewState(
    val characterFullDescription: String = EMPTY_STRING,
    val characterVisibility: CharacterVisibility = CharacterVisibility.PRIVATE,
    val loadingState: LoadingState = LoadingState.Idle
) : BaseViewState()