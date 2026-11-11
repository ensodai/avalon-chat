package com.ensodai.avalon.presentation.selectAiProviderScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SelectAiProviderScreen(
    navigation: Navigation
) {
    val viewModel: SelectAiProviderScreenViewModel = koinViewModel()
    val state by viewModel.viewState.collectAsState()

    SelectAiProviderScreenContent(
        state = state,
        actions = viewModel.actions,
        onBack = { navigation.navigateBack() }
    )
}
