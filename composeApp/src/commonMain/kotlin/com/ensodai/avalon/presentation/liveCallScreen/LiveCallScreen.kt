package com.ensodai.avalon.presentation.liveCallScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ensodai.avalon.presentation.navigation.Navigation
import com.ensodai.avalon.utils.L
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
fun LiveCallScreen(
    navigation: Navigation,
    characterId: Uuid,
    viewModel: LiveCallViewModel = koinViewModel {
        parametersOf(characterId)
    },
) {
    val state by viewModel.viewState.collectAsState()
    val actions = remember { viewModel.actions }
    val userVolume by viewModel.userVolume.collectAsState(initial = 0f)
    val aiVolume by viewModel.aiVolume.collectAsState(initial = 0f)
//    L.d("aiVolume", "$aiVolume")
//    L.d("userVolume", "$userVolume")
    var permissionDenied by remember { mutableStateOf(false) }

    val requestPermission = rememberMicrophonePermissionLauncher(
        onGranted = { actions.startCallScreen() },
        onDenied = { permissionDenied = true }
    )

    val wrappedActions = remember(actions, requestPermission) {
        actions.copy(
            startCallScreen = { requestPermission() }
        )
    }

    LiveCallScreenContent(
        navigation = navigation,
        state = state,
        actions = wrappedActions,
        userVolume = userVolume,
        aiVolume = aiVolume,
        permissionDenied = permissionDenied
    )
}
