package com.ensodai.avalon.presentation.liveCallScreen

import androidx.lifecycle.viewModelScope
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.domain.proximity.ProximityAudioManager
import com.ensodai.avalon.domain.useCases.liveCall.GetLiveCallCharacterInfoUseCase
import com.ensodai.avalon.domain.useCases.liveCall.LiveCallUseCase
import com.ensodai.avalon.domain.chatEvents.liveCall.LiveCallSocketClient
import com.ensodai.avalon.presentation.core.BaseViewModel
import com.ensodai.avalon.presentation.liveCallScreen.action.LiveCallScreenActions
import com.ensodai.avalon.presentation.liveCallScreen.action.startCallScreen
import com.ensodai.avalon.presentation.liveCallScreen.action.stopCallScreen
import com.ensodai.avalon.presentation.liveCallScreen.action.toggleMute
import com.ensodai.avalon.presentation.liveCallScreen.action.updateCallError
import com.ensodai.avalon.presentation.liveCallScreen.action.updateCallStateFromPhase
import com.ensodai.avalon.presentation.liveCallScreen.action.updateTurnReject
import com.ensodai.avalon.presentation.liveCallScreen.viewState.LiveCallPhase
import com.ensodai.avalon.presentation.liveCallScreen.viewState.LiveCallScreenViewState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
class LiveCallViewModel(
    @InjectedParam val characterId: Uuid,
    val liveCallUseCase: LiveCallUseCase,
    private val getLiveCallCharacterInfoUseCase: GetLiveCallCharacterInfoUseCase,
    internal val proximityAudioManager: ProximityAudioManager
) : BaseViewModel<LiveCallScreenViewState, LiveCallScreenActions>(
    initialState = LiveCallScreenViewState()
) {
    val userVolume: Flow<Float> get() = liveCallUseCase.userVolume
    val aiVolume: Flow<Float> get() = liveCallUseCase.aiVolume

    override val actions = LiveCallScreenActions(
        startCallScreen = { startCallScreen() },
        stopCallScreen = { stopCallScreen() },
        toggleMute = { toggleMute() },
        showModelPicker = { updateViewState { it.copy(showModelSheet = true) } },
        selectModel = { model -> updateViewState { it.copy(selectedModel = model, showModelSheet = false) } },
        hideModelSheet = { updateViewState { it.copy(showModelSheet = false) } }
    )

    private var callTimerJob: Job? = null
    private var stoppedToIdleJob: Job? = null

    init {
        observeCallState()
        loadCharacterInfo()
    }

    override fun onCleared() {
        super.onCleared()
        liveCallUseCase.disconnect()
        proximityAudioManager.stop()
        callTimerJob?.cancel()
        stoppedToIdleJob?.cancel()
    }

    private fun observeCallState() {
        liveCallUseCase.state
            .onEach { state ->
                updateCallStateFromPhase(state.phase)
                updateCallError(state.lastError)
                updateTurnReject(
                    reason = state.lastTurnRejectReason,
                    eventCount = state.turnRejectEventCount
                )

                when (state.phase) {
                    LiveCallSocketClient.LiveCallState.Phase.ACTIVE -> {
                        startCallTimer()
                        stoppedToIdleJob?.cancel()
                    }
                    LiveCallSocketClient.LiveCallState.Phase.STOPPED -> {
                        stopCallTimer()
                        stoppedToIdleJob = viewModelScope.launch {
                            delay(2000)
                            if (viewState.value.callPhase == LiveCallPhase.STOPPED) {
                                updateCallStateFromPhase(
                                    LiveCallSocketClient.LiveCallState.Phase.IDLE
                                )
                                updateViewState { it.copy(callDurationSeconds = 0) }
                            }
                        }
                    }
                    LiveCallSocketClient.LiveCallState.Phase.IDLE -> {
                        stopCallTimer()
                        stoppedToIdleJob?.cancel()
                    }
                    else -> {}
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadCharacterInfo() {
        viewModelScope.launch {
            val info = getLiveCallCharacterInfoUseCase(characterId)
            updateViewState {
                it.copy(
                    characterName = info.name,
                    characterAvatarUrl = info.avatarUrl
                )
            }
        }
    }

    private fun startCallTimer() {
        if (callTimerJob?.isActive == true) return
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                updateViewState { it.copy(callDurationSeconds = it.callDurationSeconds + 1) }
            }
        }
    }

    private fun stopCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = null
    }
}
