package com.ensodai.avalon.presentation.liveCallScreen.viewState

import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.shared.avalon.liveCall.models.LiveCallTurnRejectReason
import com.ensodai.avalon.presentation.core.BaseViewState

enum class LiveCallPhase {
    IDLE,
    CONNECTING,
    ACTIVE,
    INTERRUPTED,
    RECONNECTING,
    STOPPING,
    STOPPED
}

data class LiveCallScreenViewState(
    val isMuted: Boolean = false,
    val callPhase: LiveCallPhase = LiveCallPhase.IDLE,
    val error: String? = null,
    val permissionDenied: Boolean = false,
    val characterName: String? = null,
    val characterAvatarUrl: String? = null,
    val callDurationSeconds: Long = 0,
    val turnRejected: LiveCallTurnRejectReason? = null,
    val rejectTriggerCount: Int = 0,
    val selectedModel: VoiceModel = VoiceModel.AOEDE,
    val showModelSheet: Boolean = false
) : BaseViewState() {
    val isCallActive: Boolean
        get() = callPhase == LiveCallPhase.ACTIVE || callPhase == LiveCallPhase.INTERRUPTED

    val isTurnRejected: Boolean
        get() = turnRejected != null
}
