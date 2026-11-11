package com.ensodai.avalon.presentation.editTemperatureScreen.action

import com.ensodai.avalon.presentation.core.BaseActions

data class EditTemperatureScreenActions(
    val onChangeTemperature: (Float) -> Unit,
    val onSaveTemperature: () -> Unit
) : BaseActions()