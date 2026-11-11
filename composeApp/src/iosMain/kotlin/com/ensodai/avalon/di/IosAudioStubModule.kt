package com.ensodai.avalon.di

import com.ensodai.avalon.data.createProximityAudioManager
import com.ensodai.avalon.domain.audio.AudioCapture
import com.ensodai.avalon.domain.audio.AudioPlayback
import com.ensodai.avalon.domain.audio.TtsPlayer
import com.ensodai.avalon.domain.audio.TtsPlayerState
import com.ensodai.avalon.domain.audio.TtsCacheManager
import com.ensodai.avalon.domain.proximity.ProximityAudioManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.dsl.module
import kotlin.uuid.Uuid

/**
 * Stub-реализации аудио для iOS.
 * Подключить в IosAppModule когда он появится, или добавить в iosPlatformModule:
 *
 * val iosPlatformModule = module {
 *     includes(iosAudioStubModule)
 *     // ...
 * }
 */
val iosAudioStubModule = module {
    single<ProximityAudioManager> {
        createProximityAudioManager()
    }

    single<AudioCapture> {
        object : AudioCapture {
            override fun start(onChunk: (ByteArray) -> Unit) {}
            override fun stop() {}
            override fun setMuted(muted: Boolean) {}
            override val isRecording: Boolean = false
        }
    }

    single<AudioPlayback> {
        object : AudioPlayback {
            override fun enqueue(pcmData: ByteArray) {}
            override fun clear() {}
            override fun stop() {}
            override val isPlaying: Boolean = false
            override val volumeFlow: StateFlow<Float> = MutableStateFlow(0f)
        }
    }

    single<TtsPlayer> {
        object : TtsPlayer {
            override val state: StateFlow<TtsPlayerState> = MutableStateFlow(TtsPlayerState.Idle)
            override val currentContentId: StateFlow<Uuid?> = MutableStateFlow(null)
            override fun startLoading(contentId: Uuid) {}
            override fun startStreaming(contentId: Uuid) {}
            override fun playChunk(chunk: ByteArray) {}
            override fun stopStreaming() {}
            override fun play(contentId: Uuid, url: String) {}
            override fun pause() {}
            override fun resume() {}
            override fun stop() {}
        }
    }

    single<TtsCacheManager> {
        object : TtsCacheManager {
            override fun saveTtsAudio(contentId: Uuid, bytes: ByteArray): String = ""
            override fun getTtsAudioPath(contentId: Uuid): String? = null
            override fun hasTtsAudio(contentId: Uuid): Boolean = false
            override fun clearCache() {}
        }
    }
}

