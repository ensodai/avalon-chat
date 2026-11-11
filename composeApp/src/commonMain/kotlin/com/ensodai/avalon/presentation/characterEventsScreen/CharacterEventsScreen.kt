package com.ensodai.avalon.presentation.characterEventsScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
fun CharacterEventsScreen(
    navigation: Navigation,
    characterId: Uuid,
    viewModel: CharacterEventsScreenViewModel = koinViewModel {
        parametersOf(characterId)
    }
) {
    val actions = remember { viewModel.actions }
    val viewState by viewModel.viewState.collectAsState()

    CharacterEventsScreenContent(
        actions = actions,
        viewState = viewState,
        navigation = navigation
    )
}