package app.domain.liveCall.models

import com.ensodai.shared.avalon.liveCall.models.LiveCallTurnCompleteReason

sealed interface LiveCallInternalEvent {
    data object ModelTurnChunkReceived : LiveCallInternalEvent

    data object InterruptedObserved : LiveCallInternalEvent

    data object WaitingForInputObserved : LiveCallInternalEvent

    data class UsageMetadataReceived(
        val totalTokens: Long?,
        val promptTokens: Long?,
        val responseTokens: Long?,
        val cachedTokens: Long?,
        val thoughtsTokens: Long?
    ) : LiveCallInternalEvent

    data class TurnCompleted(
        val reason: LiveCallTurnCompleteReason?
    ) : LiveCallInternalEvent
}
