package com.ensodai.avalon.presentation.editCharacterScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
fun EditCharacterScreen(
    navigation: Navigation,
    characterId: Uuid,
    viewModel: EditCharacterScreenViewModel = koinViewModel {
        parametersOf(characterId)
    },
) {
    val actions = remember { viewModel.actions }

    val viewState by viewModel.viewState.collectAsState()

    LifecycleResumeEffect(characterId) {
        viewModel.refreshPromptDetails()
        onPauseOrDispose { }
    }

    EditChatConfigScreenContent(
        actions = actions,
        viewState = viewState,
        navigation = navigation
    )
}
