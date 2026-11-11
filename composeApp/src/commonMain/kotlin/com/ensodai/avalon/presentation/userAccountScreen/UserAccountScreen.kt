package com.ensodai.avalon.presentation.userAccountScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UserAccountScreen(
    viewModel: UserAccountScreenViewModel = koinViewModel(),
    navigation: Navigation
) {

    val viewState by viewModel.viewState.collectAsState()
    val actions = remember { viewModel.actions }

    UserAccountScreenContent(
        navigation = navigation,
        viewState = viewState,
        actions = actions
    )

}
