package com.ensodai.avalon.presentation.editCharacterTextScreen.viewState

import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.avalon.presentation.core.BaseViewState
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.editCharacterTextScreen.model.CharacterTextType
import com.ensodai.avalon.utils.EMPTY_STRING

data class EditCharacterTextScreenViewState(
    val text: String = EMPTY_STRING,
    val characterVisibility: CharacterVisibility = CharacterVisibility.PRIVATE,
    val loadingState: LoadingState = LoadingState.Idle,
    val textType: CharacterTextType = CharacterTextType.PERSONALITY
) : BaseViewState()
