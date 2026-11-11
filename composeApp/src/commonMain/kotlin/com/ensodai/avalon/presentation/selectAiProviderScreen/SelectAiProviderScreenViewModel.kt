package com.ensodai.avalon.presentation.selectAiProviderScreen

import com.ensodai.avalon.domain.useCases.userAiSettingsScreenUseCase.GetAvailableAiModelsUseCase
import com.ensodai.avalon.domain.useCases.userAiSettingsScreenUseCase.GetUserAiSettingsUseCase
import com.ensodai.avalon.domain.useCases.userAiSettingsScreenUseCase.UpdateUserAiSettingsUseCase
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.selectAiProviderScreen.action.SelectAiProviderScreenActions
import com.ensodai.avalon.presentation.selectAiProviderScreen.action.createViewState
import com.ensodai.avalon.presentation.selectAiProviderScreen.action.selectProvider
import com.ensodai.avalon.presentation.selectAiProviderScreen.viewState.SelectAiProviderScreenViewState
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SelectAiProviderScreenViewModel(
    val getUserAiSettings: GetUserAiSettingsUseCase,
    val updateUserAiSettings: UpdateUserAiSettingsUseCase,
    val getAvailableAiModels: GetAvailableAiModelsUseCase
) : BaseViewModel<SelectAiProviderScreenViewState, SelectAiProviderScreenActions>(
    initialState = SelectAiProviderScreenViewState()
) {

    override val actions = SelectAiProviderScreenActions(
        onSelectProvider = { providerId ->
            selectProvider(
                providerId = providerId,
                updateUserAiSettings = updateUserAiSettings,
                getAvailableAiModels = getAvailableAiModels
            )
        },
        onRefresh = {
            createViewState(getUserAiSettings = getUserAiSettings)
        }
    )

    init {
        createViewState(getUserAiSettings = getUserAiSettings)
    }
}
