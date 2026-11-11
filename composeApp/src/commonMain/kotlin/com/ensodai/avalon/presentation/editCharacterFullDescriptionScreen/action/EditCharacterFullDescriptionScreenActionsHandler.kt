package com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import com.ensodai.avalon.domain.useCases.editCharacterFullDescriptionScreenUseCase.GetCharacterFullDescriptionUseCase
import com.ensodai.avalon.domain.useCases.editCharacterFullDescriptionScreenUseCase.UpdateCharacterFullDescriptionUseCase
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen.EditCharacterFullDescriptionScreenViewModel
import com.ensodai.avalon.utils.EMPTY_STRING
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

fun EditCharacterFullDescriptionScreenViewModel.createViewState(
    getCharacterFullDescription: GetCharacterFullDescriptionUseCase,
    characterId: Uuid
) {
    viewModelScope.launch(IODispatcher) {
        getCharacterFullDescription
            .get(characterId)
            .collect { fullDescription ->
                val fullDescriptionPrivacy = if (fullDescription?.isPublicFullDescription == true) {
                    CharacterVisibility.PUBLIC
                } else {
                    CharacterVisibility.PRIVATE
                }
                updateViewState {
                    it.copy(
                        characterFullDescription = fullDescription?.fullDescription ?: EMPTY_STRING,
                        characterVisibility = fullDescriptionPrivacy
                    )
                }
            }
    }
}

fun EditCharacterFullDescriptionScreenViewModel.updateFullDescriptionVisibility(visibility: CharacterVisibility) {
    updateViewState {
        it.copy(
            characterVisibility = visibility
        )
    }
}

fun EditCharacterFullDescriptionScreenViewModel.updateCharacterFullDescription(
    characterId: Uuid?,
    updateCharacterFullDescription: UpdateCharacterFullDescriptionUseCase,
    fullDescription: String
) {
    if (characterId == null) return

    updateViewState {
        it.copy(loadingState = LoadingState.Loading)
    }
    viewModelScope.launch(IODispatcher) {
        updateCharacterFullDescription.update(
            characterId = characterId,
            fullDescription = fullDescription,
            isPublicFullDescription = viewState.value.characterVisibility == CharacterVisibility.PUBLIC
        ).fold(
            onSuccess = {
                updateViewState {
                    it.copy(loadingState = LoadingState.Success)
                }
            },
            onFailure = {
                updateViewState {
                    it.copy(loadingState = LoadingState.Error)
                }
            }
        )
    }
}