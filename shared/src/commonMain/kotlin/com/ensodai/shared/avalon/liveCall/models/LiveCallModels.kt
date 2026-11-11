@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.ensodai.shared.avalon.liveCall.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * Конфигурация звонка.
 */
@Serializable
data class LiveCallConfig(
    val model: String? = null,
    val voiceName: String = "Aoede",
    val languageCode: String = "ru-RU"
)

/**
 * Причины завершения звонка.
 */
@Serializable
enum class LiveCallStopReason {
    @SerialName("user_requested")
    USER_REQUESTED,

    @SerialName("insufficient_energy")
    INSUFFICIENT_ENERGY,

    @SerialName("auth_revoked")
    AUTH_REVOKED,

    @SerialName("session_expired")
    SESSION_EXPIRED,

    @SerialName("internal_error")
    INTERNAL_ERROR
}

/**
 * Причины отклонения звонка.
 */
@Serializable
enum class LiveCallRejectReason {
    @SerialName("insufficient_energy")
    INSUFFICIENT_ENERGY,

    @SerialName("chat_not_found")
    CHAT_NOT_FOUND,

    @SerialName("not_owner")
    NOT_OWNER
}

/**
 * Результаты завершения хода ИИ.
 */
@Serializable
enum class LiveCallTurnCompleteReason {
    @SerialName("unspecified")
    UNSPECIFIED,

    @SerialName("malformed_function_call")
    MALFORMED_FUNCTION_CALL,

    @SerialName("response_rejected")
    RESPONSE_REJECTED,

    @SerialName("need_more_input")
    NEED_MORE_INPUT
}

@Serializable
enum class LiveCallTurnRejectReason {
    @SerialName("response_rejected")
    RESPONSE_REJECTED
}

/**
 * События от сервера во время звонка.
 */
@Serializable
sealed class LiveCallServerEvent {
    
    @Serializable
    @SerialName("call_started")
    data class CallStarted(
        @Contextual
        val chatVariantId: Uuid,
        val model: String
    ) : LiveCallServerEvent()

    @Serializable
    @SerialName("call_rejected")
    data class CallRejected(
        val reason: LiveCallRejectReason,
        val message: String? = null,
        val requiredCost: Int? = null,
        val dailyEnergy: Int? = null,
        val reserveEnergy: Int? = null
    ) : LiveCallServerEvent()

    @Serializable
    @SerialName("call_stopped")
    data class CallStopped(
        val reason: LiveCallStopReason,
        val message: String? = null,
        val requiredCost: Int? = null,
        val dailyEnergy: Int? = null,
        val reserveEnergy: Int? = null
    ) : LiveCallServerEvent()

    @Serializable
    @SerialName("audio_chunk")
    data class AudioChunk(
        val audio: ByteArray
    ) : LiveCallServerEvent()

    @Serializable
    @SerialName("voice_activity")
    data class VoiceActivity(
        val activity: String
    ) : LiveCallServerEvent()

    @Serializable
    @SerialName("interrupted")
    data object Interrupted : LiveCallServerEvent()

    @Serializable
    @SerialName("waiting_for_input")
    data object WaitingForInput : LiveCallServerEvent()

    @Serializable
    @SerialName("turn_complete")
    data class TurnComplete(
        val reason: LiveCallTurnCompleteReason? = null
    ) : LiveCallServerEvent()

    @Serializable
    @SerialName("turn_rejected")
    data class TurnRejected(
        val reason: LiveCallTurnRejectReason
    ) : LiveCallServerEvent()

    @Serializable
    @SerialName("error")
    data class Error(
        val message: String
    ) : LiveCallServerEvent()
}
