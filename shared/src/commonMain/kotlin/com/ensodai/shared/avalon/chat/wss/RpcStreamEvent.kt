package com.ensodai.shared.avalon.chat.wss

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RpcStreamEvent<T>(
    @SerialName("is_ready_signal")
    val isReadySignal: Boolean = false,
    val payload: T? = null
)
