package com.ensodai.avalon.presentation.editTemperatureScreen

import com.ensodai.avalon.domain.useCases.editTemperatureScreenUseCase.GetTemperatureUseCase
import com.ensodai.avalon.domain.useCases.editTemperatureScreenUseCase.UpdateTemperatureUseCase
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.editTemperatureScreen.action.EditTemperatureScreenActions
import com.ensodai.avalon.presentation.editTemperatureScreen.action.createViewState
import com.ensodai.avalon.presentation.editTemperatureScreen.action.onChangeTemperature
import com.ensodai.avalon.presentation.editTemperatureScreen.action.onSaveTemperature
import com.ensodai.avalon.presentation.editTemperatureScreen.viewState.EditTemperatureScreenViewState
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
class EditTemperatureScreenViewModel(
    private val getTemperature: GetTemperatureUseCase,
    private val updateTemperatureUseCase: UpdateTemperatureUseCase,
    @InjectedParam val characterId: Uuid,
) : BaseViewModel<EditTemperatureScreenViewState, EditTemperatureScreenActions>(
    initialState = EditTemperatureScreenViewState()
) {

    override val actions = EditTemperatureScreenActions(
        onChangeTemperature = { onChangeTemperature(it) },
        onSaveTemperature = {
            onSaveTemperature(
                characterId = characterId,
                temperature = viewState.value.temperature,
                updateTemperature = updateTemperatureUseCase
            )
        }
    )

    init {
        createViewState(
            characterId = characterId,
            getTemperature = getTemperature
        )
    }
}
