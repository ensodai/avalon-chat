package com.ensodai.avalon.presentation.characterLorebooksScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
fun CharacterLorebooksScreen(
    navigation: Navigation,
    characterId: Uuid,
    viewModel: CharacterLorebooksScreenViewModel = koinViewModel {
        parametersOf(characterId)
    },
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {
    val viewState by viewModel.viewState.collectAsState()
    val actions = remember { viewModel.actions }

    CharacterLorebooksScreenContent(
        viewState = viewState,
        actions = actions,
        navigation = navigation,
        onEnableSwipeable = onEnableSwipeable,
        onDisableSwipeable = onDisableSwipeable
    )
}
