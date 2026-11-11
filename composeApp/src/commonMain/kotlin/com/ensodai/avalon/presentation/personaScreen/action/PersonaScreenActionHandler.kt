package com.ensodai.avalon.presentation.personaScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.domain.useCases.personaScreenUseCase.CreateNewPersonaUseCase
import com.ensodai.avalon.domain.useCases.personaScreenUseCase.DeletePersonaUseCase
import com.ensodai.avalon.domain.useCases.personaScreenUseCase.GetPersonaByIdUseCase
import com.ensodai.avalon.domain.useCases.personaScreenUseCase.UpdatePersonaUseCase
import com.ensodai.avalon.presentation.personaScreen.PersonaScreenViewModel
import com.ensodai.avalon.presentation.personaScreen.viewState.PersonaLoadingState
import com.ensodai.avalon.utils.IODispatcher
import com.ensodai.avalon.utils.L
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

fun PersonaScreenViewModel.createViewState(
    id: Uuid?,
    isUpdatePersona: Boolean,
    getPersonaById: GetPersonaByIdUseCase
) {
    if (isUpdatePersona && id != null) {
        viewModelScope.launch(IODispatcher) {
            val persona = getPersonaById.get(id)
            if (persona != null) {
                L.d("PersonaScreenViewModel", "createViewState: $persona")
                updateViewState {
                    it.copy(
                        id = id,
                        name = persona.name,
                        description = persona.description
                    )
                }
            }
        }
    }
}

fun PersonaScreenViewModel.changeName(name: String) {
    val truncatedText = name.take(20)
    updateViewState {
        it.copy(
            name = truncatedText,
        )
    }
}

fun PersonaScreenViewModel.changeDescription(description: String) {
    updateViewState {
        it.copy(
            description = description.take(1000),
        )
    }
}

fun PersonaScreenViewModel.createNewPersona(createNewPersona: CreateNewPersonaUseCase) {
    viewModelScope.launch(IODispatcher) {
        updateViewState {
            it.copy(loadingState = PersonaLoadingState.Loading)
        }
        val persona = viewState.value
        val result = createNewPersona.create(
            name = persona.name,
            description = persona.description
        )
        result.fold(
            onSuccess = {
                updateViewState {
                    it.copy(loadingState = PersonaLoadingState.Success)
                }
            },
            onFailure = {
                updateViewState {
                    it.copy(loadingState = PersonaLoadingState.Error)
                }
            }
        )
    }
}

fun PersonaScreenViewModel.deletePersona(deletePersona: DeletePersonaUseCase) {
    viewModelScope.launch(IODispatcher) {
        updateViewState {
            it.copy(
                loadingState = PersonaLoadingState.Loading,
                isShowDeleteAlertDialog = false
            )
        }
        val persona = viewState.value
        val result = deletePersona.delete(persona.id)
        result.fold(
            onSuccess = {
                updateViewState {
                    it.copy(loadingState = PersonaLoadingState.Success)
                }
            },
            onFailure = {
                updateViewState {
                    it.copy(loadingState = PersonaLoadingState.Error)
                }
            }
        )
    }
}

fun PersonaScreenViewModel.saveChanges(updatePersona: UpdatePersonaUseCase) {
    viewModelScope.launch(IODispatcher) {
        updateViewState {
            it.copy(loadingState = PersonaLoadingState.Loading)
        }
        val persona = viewState.value
        val result = updatePersona.update(
            personaId = persona.id,
            name = persona.name,
            description = persona.description
        )
        result.fold(
            onSuccess = {
                updateViewState {
                    it.copy(loadingState = PersonaLoadingState.Success)
                }
            },
            onFailure = {
                updateViewState {
                    it.copy(loadingState = PersonaLoadingState.Error)
                }
            }
        )
    }
}

fun PersonaScreenViewModel.showDeletePersonaAlertDialog() {
    if (viewState.value.loadingState == PersonaLoadingState.Loading) return

    updateViewState {
        it.copy(isShowDeleteAlertDialog = true)
    }
}


fun PersonaScreenViewModel.closeAlertDialog() {
    updateViewState {
        it.copy(
            isShowDeleteAlertDialog = false,
            isShowAboutAlertDialog = false
        )
    }
}

fun PersonaScreenViewModel.showAboutAlertDialog() {
    updateViewState {
        it.copy(isShowAboutAlertDialog = true)
    }
}
