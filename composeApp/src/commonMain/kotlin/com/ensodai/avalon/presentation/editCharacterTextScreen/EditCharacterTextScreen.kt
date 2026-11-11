package com.ensodai.avalon.presentation.editCharacterTextScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ensodai.avalon.presentation.editCharacterTextScreen.model.CharacterTextType
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
fun EditCharacterTextScreen(
    navigation: Navigation,
    characterId: Uuid,
    textType: CharacterTextType,
    viewModel: EditCharacterTextScreenViewModel = koinViewModel(
        key = "${characterId}_$textType"
    ) {
        parametersOf(characterId, textType)
    },
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {
    val actions = remember { viewModel.actions }
    val viewState by viewModel.viewState.collectAsState()

    EditCharacterTextScreenContent(
        viewState = viewState,
        actions = actions,
        navigation = navigation,
        onEnableSwipeable = onEnableSwipeable,
        onDisableSwipeable = onDisableSwipeable
    )
}
