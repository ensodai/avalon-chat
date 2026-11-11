package com.ensodai.avalon.presentation.personaScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
fun PersonaScreen(
    navigation: Navigation,
    id: Uuid?,
    isUpdatePersona: Boolean,
    viewModel: PersonaScreenViewModel = koinViewModel {
        parametersOf(id, isUpdatePersona)
    },
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {
    val actions = remember { viewModel.actions }
    val viewState by viewModel.viewState.collectAsState()


    PersonaScreenContent(
        navigation = navigation,
        viewState = viewState,
        actions = actions,
        isUpdatePersona = isUpdatePersona,
        onEnableSwipeable = { onEnableSwipeable() },
        onDisableSwipeable = { onDisableSwipeable() }
    )

}
