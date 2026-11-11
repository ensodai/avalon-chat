package com.ensodai.avalon.presentation.editCharacterPrivatePhotosScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
fun EditCharacterPrivatePhotosScreen(
    navigation: Navigation,
    characterId: Uuid,
    viewModel: EditCharacterPrivatePhotosScreenViewModel = koinViewModel {
        parametersOf(characterId)
    },
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {
    val viewState by viewModel.viewState.collectAsState()
    val actions = remember { viewModel.actions }

    EditCharacterPrivatePhotosScreenContent(
        navigation = navigation,
        viewState = viewState,
        actions = actions,
        onEnableSwipeable = onEnableSwipeable,
        onDisableSwipeable = onDisableSwipeable
    )
}
