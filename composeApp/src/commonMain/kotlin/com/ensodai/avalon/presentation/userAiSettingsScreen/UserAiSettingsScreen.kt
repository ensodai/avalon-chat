package com.ensodai.avalon.presentation.userAiSettingsScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.presentation.userAiSettingsScreen.action.createViewState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UserAiSettingsScreen(
    navigation: Navigation
) {
    val viewModel: UserAiSettingsScreenViewModel = koinViewModel()
    val state by viewModel.viewState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.createViewState(viewModel.getUserAiSettings)
    }

    UserAiSettingsScreenContent(
        state = state,
        actions = viewModel.actions,
        onBack = { navigation.navigateBack() },
        onNavigateToSummaryExplanation = { navigation.navigateToSummaryExplanationScreen() },
        onNavigateToSelectProvider = { navigation.navigateToSelectAiProviderScreen() }
    )
}
