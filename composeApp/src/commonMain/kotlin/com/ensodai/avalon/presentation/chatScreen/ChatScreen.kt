package com.ensodai.avalon.presentation.chatScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.util.trace
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
fun ChatScreen(
    navigation: Navigation,
    characterPublicId: Uuid?,
    showAiWarning: Boolean,
    chatRoleplayPreset: ChatRoleplayPreset,
    viewModel: ChatScreenViewModel = koinViewModel {
        parametersOf(characterPublicId, showAiWarning, chatRoleplayPreset)
    },
) {
    trace("HeavyScreen::composition") {

        val actions = remember { viewModel.actions }
        val viewState by viewModel.viewState.collectAsState()

        ChatScreenContent(
            actions = actions,
            viewState = viewState,
            navigation = navigation,
            viewModel = viewModel
        )
    }
}
