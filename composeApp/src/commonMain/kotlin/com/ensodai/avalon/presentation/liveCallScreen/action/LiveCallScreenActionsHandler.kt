package com.ensodai.avalon.presentation.liveCallScreen.action

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.liveCall.models.LiveCallTurnRejectReason
import com.ensodai.avalon.domain.chatEvents.liveCall.LiveCallSocketClient
import com.ensodai.avalon.presentation.liveCallScreen.LiveCallViewModel
import com.ensodai.avalon.presentation.liveCallScreen.viewState.LiveCallPhase
import kotlinx.coroutines.launch

fun LiveCallViewModel.startCallScreen() {
    viewModelScope.launch {
        liveCallUseCase.startCall(
            characterId = characterId,
            voiceName = viewState.value.selectedModel.modelName
        )
        // Запускаем proximity sensor и аудио-маршрутизацию
        proximityAudioManager.start()
    }
}

fun LiveCallViewModel.stopCallScreen() {
    liveCallUseCase.stopCall()
    // Останавливаем proximity sensor
    proximityAudioManager.stop()
}

fun LiveCallViewModel.toggleMute() {
    val current = viewState.value.isMuted
    liveCallUseCase.setMuted(!current)
    updateViewState { it.copy(isMuted = !current) }
}

fun LiveCallViewModel.updateCallStateFromPhase(phase: LiveCallSocketClient.LiveCallState.Phase) {
    val uiPhase = when (phase) {
        LiveCallSocketClient.LiveCallState.Phase.IDLE -> LiveCallPhase.IDLE
        LiveCallSocketClient.LiveCallState.Phase.CONNECTING -> LiveCallPhase.CONNECTING
        LiveCallSocketClient.LiveCallState.Phase.ACTIVE -> LiveCallPhase.ACTIVE
        LiveCallSocketClient.LiveCallState.Phase.INTERRUPTED -> LiveCallPhase.INTERRUPTED
        LiveCallSocketClient.LiveCallState.Phase.RECONNECTING -> LiveCallPhase.RECONNECTING
        LiveCallSocketClient.LiveCallState.Phase.STOPPING -> LiveCallPhase.STOPPING
        LiveCallSocketClient.LiveCallState.Phase.STOPPED -> LiveCallPhase.STOPPED
    }
    updateViewState { it.copy(callPhase = uiPhase) }
}

fun LiveCallViewModel.updateCallError(error: String?) {
    updateViewState { it.copy(error = error) }
}

fun LiveCallViewModel.updateTurnReject(reason: LiveCallTurnRejectReason?, eventCount: Int) {
    updateViewState {
        it.copy(
            turnRejected = reason,
            rejectTriggerCount = eventCount
        )
    }
}
