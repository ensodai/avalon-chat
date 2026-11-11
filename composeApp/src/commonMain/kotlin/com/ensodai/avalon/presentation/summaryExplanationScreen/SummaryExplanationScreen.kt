package com.ensodai.avalon.presentation.summaryExplanationScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SummaryExplanationScreen(
    navigation: Navigation
) {
    val viewModel: SummaryExplanationScreenViewModel = koinViewModel()
    val state by viewModel.viewState.collectAsState()

    SummaryExplanationScreenContent(
        state = state,
        actions = viewModel.actions,
        onBack = { navigation.navigateBack() }
    )
}
