package com.ensodai.avalon.presentation.archiveScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ArchiveScreen(
    viewModel: ArchiveScreenViewModel = koinViewModel(),
    navigation: Navigation
) {

    val actions = remember { viewModel.actions }
    val viewState by viewModel.viewState.collectAsState()

    ArchiveScreenContent(
        viewState = viewState,
        actions = actions,
        navigation = navigation,
        viewModel = viewModel
    )

}
