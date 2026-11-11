package com.ensodai.avalon.presentation.settingScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ensodai.avalon.presentation.navigation.Navigation
//import com.ensodai.avalon.presentation.navigation.AppyxScreenRoute
import com.ensodai.avalon.presentation.settingScreen.provider.ProvideSettingScreenConstructor
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingScreen(
    viewModel: SettingScreenViewModel = koinViewModel(),
    navigation: Navigation,
    argumentGuide: Boolean
) {

    val action = viewModel.action
    val viewState by viewModel.viewState.collectAsState()
    val provideConstructor = ProvideSettingScreenConstructor(
        viewState = viewState,
        action = action,
        navigation = navigation
    )
    SettingScreenContent(provideConstructor = provideConstructor)
}
