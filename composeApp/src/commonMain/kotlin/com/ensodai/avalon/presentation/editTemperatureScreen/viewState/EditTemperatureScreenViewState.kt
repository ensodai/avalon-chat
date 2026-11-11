package com.ensodai.avalon.presentation.editTemperatureScreen.viewState

import com.ensodai.avalon.presentation.core.BaseViewState
import com.ensodai.avalon.presentation.core.LoadingState

data class EditTemperatureScreenViewState(
    val temperature: Float = 0.5f,
    val loadingState: LoadingState = LoadingState.Idle
) : BaseViewState()
