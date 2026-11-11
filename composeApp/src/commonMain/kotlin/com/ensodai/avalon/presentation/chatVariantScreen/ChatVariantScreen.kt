package com.ensodai.avalon.presentation.chatVariantScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.ensodai.avalon.presentation.navigation.Navigation
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
fun ChatVariantScreen(
    navigation: Navigation,
    characterId: Uuid?,
    viewModel: ChatVariantScreenViewModel = koinViewModel {
        parametersOf(characterId)
    },
) {

    val action = remember { viewModel.actions }
    val viewState by viewModel.viewState.collectAsState()
    val chatVariants by viewModel.chatVariants.collectAsState()

    ChatVariantScreenContent(
        viewState = viewState,
        actions = action,
        navigation = navigation,
        chatVariants = chatVariants
    )
}
