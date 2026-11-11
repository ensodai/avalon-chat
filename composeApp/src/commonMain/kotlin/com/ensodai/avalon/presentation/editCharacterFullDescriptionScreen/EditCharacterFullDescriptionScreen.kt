package com.ensodai.avalon.presentation.editCharacterFullDescriptionScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
fun EditCharacterFullDescriptionScreen(
    navigation: Navigation,
    characterId: Uuid,
    viewModel: EditCharacterFullDescriptionScreenViewModel = koinViewModel {
        parametersOf(characterId)
    },
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {

    val actions = remember { viewModel.actions }
    val viewState by viewModel.viewState.collectAsState()

    EditCharacterFullDescriptionScreenContent(
        viewState = viewState,
        actions = actions,
        navigation = navigation,
        onEnableSwipeable = onEnableSwipeable,
        onDisableSwipeable = onDisableSwipeable
    )
}
