package com.ensodai.avalon.presentation.mainScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = koinViewModel(),
    navigation: Navigation
) {
    val action = viewModel.actions
    val viewState by viewModel.viewState.collectAsState()

    MainScreenContent(
        viewState = viewState,
        actions = action,
        navigation = navigation,
        viewModel = viewModel
    )
}