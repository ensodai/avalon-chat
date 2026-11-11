package com.ensodai.avalon.presentation.editUserProfileScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun EditUserProfileScreen(
    navigation: Navigation,
    nickname: String,
    viewModel: EditUserProfileScreenViewModel = koinViewModel {
        parametersOf(nickname)
    },
    onEnableSwipeable: () -> Unit,
    onDisableSwipeable: () -> Unit
) {

    val viewState by viewModel.viewState.collectAsState()
    val actions = remember { viewModel.actions }

    EditUserProfileScreenContent(
        viewState = viewState,
        navigation = navigation,
        actions = actions,
        onEnableSwipeable = { onEnableSwipeable() },
        onDisableSwipeable = { onDisableSwipeable() }
    )
}
