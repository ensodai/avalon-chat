package com.ensodai.avalon.presentation.settingScreen.provider

import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.presentation.settingScreen.action.SettingScreenAction
import com.ensodai.avalon.presentation.settingScreen.viewState.SettingScreenViewState

data class ProvideSettingScreenConstructor(
    val viewState: SettingScreenViewState,
    val action: SettingScreenAction,
    val navigation: Navigation
)