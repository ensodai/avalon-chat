package com.ensodai.avalon.presentation.mainScreen.provider

import com.ensodai.avalon.presentation.mainScreen.action.MainScreenActions
import com.ensodai.avalon.presentation.mainScreen.viewState.MainScreenViewState
import com.ensodai.avalon.presentation.navigation.Navigation

data class ProvideMainScreenConstructor(
    val viewState: MainScreenViewState,
    val action: MainScreenActions,
    val navigation: Navigation
)