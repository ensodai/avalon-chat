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
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

/**
 * Stub-реализации аудио для платформ без нативной поддержки (web, iOS).
 * Подключается только в WebAppModule / IosAppModule — никогда вместе с AndroidAudioModule.
 */
@Module
class WebAudioStubModule {

    @Single
    fun provideProximityAudioStub(): ProximityAudioManager {
        return createProximityAudioManager()
    }

    @Single
    fun provideAudioCaptureStub(): AudioCapture {
        return object : AudioCapture {
            override fun start(onChunk: (ByteArray) -> Unit) {}
            override fun stop() {}
            override fun setMuted(muted: Boolean) {}
            override val isRecording: Boolean = false
        }
    }

    @Single
    fun provideAudioPlaybackStub(): AudioPlayback {
        return object : AudioPlayback {
            override fun enqueue(pcmData: ByteArray) {}
            override fun clear() {}
            override fun stop() {}
            override val isPlaying: Boolean = false
            override val volumeFlow: StateFlow<Float> = MutableStateFlow(0f)
        }
    }

    @Single
    fun provideTtsPlayerStub(): TtsPlayer {
        return object : TtsPlayer {
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

    @Single
    fun provideTtsCacheManagerStub(): TtsCacheManager {
        return object : TtsCacheManager {
            override fun saveTtsAudio(contentId: Uuid, bytes: ByteArray): String = ""
            override fun getTtsAudioPath(contentId: Uuid): String? = null
            override fun hasTtsAudio(contentId: Uuid): Boolean = false
            override fun clearCache() {}
        }
    }
}

