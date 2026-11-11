package com.ensodai.avalon.domain.chatEvents.liveCall.model

import com.ensodai.shared.avalon.liveCall.models.LiveCallTurnRejectReason

data class LiveCallSocketState(
    val phase: Phase = Phase.IDLE,
    val lastError: String? = null,
    val lastTurnRejectReason: LiveCallTurnRejectReason? = null,
    val turnRejectEventCount: Int = 0
) {
    enum class Phase {
        IDLE,
        CONNECTING,
        ACTIVE,
        INTERRUPTED,
        RECONNECTING,
        STOPPING,
        STOPPED,
        ERROR
    }

    companion object {
        val IDLE = LiveCallSocketState(Phase.IDLE)
        val CONNECTING = LiveCallSocketState(Phase.CONNECTING)
        val ACTIVE = LiveCallSocketState(Phase.ACTIVE)
        val INTERRUPTED = LiveCallSocketState(Phase.INTERRUPTED)
        val RECONNECTING = LiveCallSocketState(Phase.RECONNECTING)
        val STOPPING = LiveCallSocketState(Phase.STOPPING)
        val STOPPED = LiveCallSocketState(Phase.STOPPED)
        val ERROR = LiveCallSocketState(Phase.ERROR)
    }
}
