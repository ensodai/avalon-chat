package com.ensodai.avalon.domain.chatEvents

import org.koin.core.annotation.Single
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.uuid.Uuid

data class StreamState(
    val text: String,
    val isFinished: Boolean = false
)

@Single
class MessageStreamManager {
    private val _activeStreams = MutableStateFlow<Map<Uuid, StreamState>>(emptyMap())
    val activeStreams = _activeStreams.asStateFlow()

    fun hasStream(containerId: Uuid): Boolean = _activeStreams.value.containsKey(containerId)

    fun appendChunk(containerId: Uuid, chunk: String, baseText: String) {
        _activeStreams.update { currentMap ->
            val currentState = currentMap[containerId]
            if (currentState == null) {
                currentMap + (containerId to StreamState(text = baseText + chunk))
            } else {
                currentMap + (containerId to currentState.copy(text = currentState.text + chunk))
            }
        }
    }
    


    fun markStreamFinished(containerId: Uuid) {
        _activeStreams.update { currentMap ->
            val currentState = currentMap[containerId]
            if (currentState != null) {
                currentMap + (containerId to currentState.copy(isFinished = true))
            } else {
                currentMap
            }
        }
    }

    fun finishStream(containerId: Uuid) {
        _activeStreams.update { currentMap ->
            currentMap - containerId
        }
    }

    fun clearAll() {
        _activeStreams.update { emptyMap() }
    }
}
