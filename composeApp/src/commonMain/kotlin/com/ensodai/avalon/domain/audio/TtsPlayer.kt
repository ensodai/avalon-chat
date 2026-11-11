package com.ensodai.avalon.domain.audio

import kotlinx.coroutines.flow.StateFlow
import kotlin.uuid.Uuid

sealed interface TtsPlayerState {
    object Idle : TtsPlayerState
    object Loading : TtsPlayerState
    data class Playing(
        val progress: Float,
        val currentAmplitude: Float = 0f,
        val isStreaming: Boolean = false,
        val lastUpdated: Long = 0L
    ) : TtsPlayerState
    data class Paused(val progress: Float) : TtsPlayerState
}

interface TtsPlayer {
    val state: StateFlow<TtsPlayerState>
    val currentContentId: StateFlow<Uuid?>

    fun startLoading(contentId: Uuid)
    fun startStreaming(contentId: Uuid)
    fun playChunk(chunk: ByteArray)
    fun stopStreaming()
    fun play(contentId: Uuid, url: String)
    fun pause()
    fun resume()
    fun stop()
}
