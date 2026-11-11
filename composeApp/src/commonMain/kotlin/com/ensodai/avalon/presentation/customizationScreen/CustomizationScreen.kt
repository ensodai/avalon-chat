package com.ensodai.avalon.presentation.customizationScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CustomizationScreen(
    navigation: Navigation,
    viewModel: CustomizationScreenViewModel = koinViewModel()
) {
    val action = remember(navigation) {
        viewModel.actions.copy(
            onChatRoleplayPresetsClick = {
                navigation.navigateToChatPresetsScreen()
            }
        )
    }
    val viewState by viewModel.viewState.collectAsState()

    CustomizationScreenContent(
        viewState = viewState,
        actions = action,
        navigation = navigation,
    )
}