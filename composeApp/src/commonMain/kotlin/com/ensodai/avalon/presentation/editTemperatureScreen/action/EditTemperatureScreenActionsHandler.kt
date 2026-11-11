package com.ensodai.avalon.presentation.editTemperatureScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.avalon.domain.useCases.editTemperatureScreenUseCase.GetTemperatureUseCase
import com.ensodai.avalon.domain.useCases.editTemperatureScreenUseCase.UpdateTemperatureUseCase
import com.ensodai.avalon.presentation.core.LoadingState
import com.ensodai.avalon.presentation.editTemperatureScreen.EditTemperatureScreenViewModel
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

fun EditTemperatureScreenViewModel.createViewState(
    characterId: Uuid,
    getTemperature: GetTemperatureUseCase,
) {

    viewModelScope.launch(IODispatcher) {
        getTemperature
            .get(characterId)
            .collect { temperature ->
                if (temperature != null) {
                    updateViewState {
                        it.copy(temperature = temperature)
                    }
                }
            }
    }
}

fun EditTemperatureScreenViewModel.onChangeTemperature(temperature: Float) {
    updateViewState {
        it.copy(temperature = temperature)
    }
}

fun EditTemperatureScreenViewModel.onSaveTemperature(
    characterId: Uuid?,
    temperature: Float,
    updateTemperature: UpdateTemperatureUseCase
) {
    if (characterId == null) return
    updateViewState {
        it.copy(loadingState = LoadingState.Loading)
    }
    viewModelScope.launch(IODispatcher) {
        updateTemperature.update(
            characterId = characterId,
            temperature = temperature
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
