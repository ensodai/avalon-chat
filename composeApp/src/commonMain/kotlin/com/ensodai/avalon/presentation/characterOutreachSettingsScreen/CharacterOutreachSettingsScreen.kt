package com.ensodai.avalon.presentation.characterOutreachSettingsScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun CharacterOutreachSettingsScreen(
    navigation: Navigation,
    characterId: Uuid,
    viewModel: CharacterOutreachSettingsViewModel = koinViewModel {
        parametersOf(characterId)
    }
) {
    val actions = remember { viewModel.actions }
    val viewState by viewModel.viewState.collectAsState()

    LaunchedEffect(viewState.saveSuccess) {
        if (viewState.saveSuccess) {
            navigation.navigateBack()
        }
    }

    CharacterOutreachSettingsScreenContent(
        actions = actions,
        viewState = viewState,
        navigation = navigation
    )
}
