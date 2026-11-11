package com.ensodai.avalon.presentation.characterProfileScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ensodai.avalon.presentation.characterProfileScreen.component.topBarSection.configs.createToolbarConfig
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
fun CharacterProfileScreen(
    navigation: Navigation,
    characterId: Uuid,
    fullScreenImage: Boolean,
    characterHasAvatars: Boolean,
    viewModel: CharacterProfileScreenViewModel = koinViewModel {
        parametersOf(characterId, fullScreenImage, characterHasAvatars)
    },
) {

    val actions = remember { viewModel.actions }
    val viewState by viewModel.viewState.collectAsState()

    val toolbarConfig = createToolbarConfig(
        startWithFullScreenImages = viewState.startWithFullScreenImages,
        topBarHasExpanded = viewState.characterHasAvatars,
    )
    CharacterProfileScreenContent(
        viewState = viewState,
        actions = actions,
        navigation = navigation,
        toolbarConfig = toolbarConfig
    )
}
