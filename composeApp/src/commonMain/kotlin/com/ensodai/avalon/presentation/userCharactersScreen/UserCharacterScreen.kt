package com.ensodai.avalon.presentation.userCharactersScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UserCharacterScreen(
    navigation: Navigation,
    viewModel: UserCharacterScreenViewModel = koinViewModel()
) {

    val viewState by viewModel.viewState.collectAsState()
    val actions = remember { viewModel.actions }
    val charactersItem by viewModel.characters.collectAsState()

    UserCharacterScreenContent(
        navigation = navigation,
        viewState = viewState,
        actions = actions,
        charactersItem = charactersItem
    )

}