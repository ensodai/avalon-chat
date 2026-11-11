package com.ensodai.avalon.domain.useCases.liveCall

import com.ensodai.avalon.di.ApplicationScope
import com.ensodai.avalon.domain.audio.AudioCapture
import com.ensodai.avalon.domain.audio.AudioPlayback
import com.ensodai.avalon.domain.proximity.ProximityAudioManager
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.domain.chatEvents.liveCall.LiveCallSocketClient
import com.ensodai.shared.avalon.liveCall.models.LiveCallServerEvent
import com.ensodai.avalon.utils.IODispatcher
import com.ensodai.avalon.utils.L
import com.ensodai.avalon.utils.currentTimeMillis
import com.ensodai.avalon.utils.getUTCTimeNow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.math.sqrt
import kotlin.uuid.Uuid

/**
 * Оркестратор live-звонка.
 * 
 * Упрощенная версия на базе Kotlin RPC и прямых байтовых потоков.
 */
@Single
class LiveCallUseCase(
    private val liveCallSocketClient: LiveCallSocketClient,
    private val audioCapture: AudioCapture,
    private val audioPlayback: AudioPlayback,
    private val proximityAudioManager: ProximityAudioManager,
    private val localChatSessionsRepository: LocalChatSessionsRepository,
    @ApplicationScope private val applicationScope: CoroutineScope
) {

    /** Поток серверных событий звонка. */
    val serverEvents: SharedFlow<LiveCallServerEvent>
        get() = liveCallSocketClient.incomingMessages

    /** Текущее состояние звонка. */
    val state get() = liveCallSocketClient.state

    val userVolume: StateFlow<Float> get() = _userVolume
    val aiVolume: StateFlow<Float> get() = _aiVolume

    private val _userVolume = MutableStateFlow(0f)
    private val _aiVolume = MutableStateFlow(0f)
    private var suppressMicUntilMs = 0L
    private var previousNearState: Boolean? = null

    private companion object {
        const val AI_SPEAKING_TIMEOUT_MS = 500L
        const val SPEAKER_ROUTE_MIC_SUPPRESSION_MS = 2_500L
    }

    init {
        proximityAudioManager.isNear
            .onEach { isNear ->
                val wasNear = previousNearState
                previousNearState = isNear

                if (wasNear == true && !isNear) {
                    suppressMicUntilMs = currentTimeMillis() + SPEAKER_ROUTE_MIC_SUPPRESSION_MS
                    L.d("LiveCallUseCase", "Mic suppression scheduled after near->far reroute")
                }
            }
            .launchIn(applicationScope)
    }

    fun disconnect() {
        stopCallSync()
        liveCallSocketClient.disconnect()
    }

    private fun stopCallSync() {
        stopAudioCapture()
        stopAudioPlayback()
        stopAudioPlaybackCollector()
        aiSpeakingTimeoutJob?.cancel()
        aiSpeakingTimeoutJob = null
        _userVolume.value = 0f
        _aiVolume.value = 0f
        liveCallSocketClient.disconnect()
    }

    suspend fun startCall(
        characterId: Uuid,
        model: String? = null,
        voiceName: String = "Aoede",
        languageCode: String = "ru-RU"
    ) {
        val session = localChatSessionsRepository.getSessionByCharacterId(characterId)

        val sessionId: Uuid
        val variantId: Uuid

        if (session == null) {
            sessionId = Uuid.random()
            variantId = Uuid.random()
            localChatSessionsRepository.createChatSessionAndChatVariant(
                chatSessionId = sessionId,
                chatVariantId = variantId,
                characterId = characterId,
                createdAt = getUTCTimeNow()
            )
        } else if (session.activeChatVariantId == null) {
            sessionId = session.id
            variantId = Uuid.random()
            localChatSessionsRepository.createLocalChatVariantAndSetAsActive(
                sessionId = sessionId,
                newVariantId = variantId,
                createdAt = getUTCTimeNow()
            )
        } else {
            sessionId = session.id
            variantId = session.activeChatVariantId
        }

        liveCallSocketClient.startCall(
            characterId = characterId,
            chatSessionId = sessionId,
            chatVariantId = variantId,
            model = model,
            voiceName = voiceName,
            languageCode = languageCode
        )

        startAudioPlaybackCollector()
        startAudioCapture()
    }

    fun stopCall() {
        stopAudioCapture()
        stopAudioPlayback()
        stopAudioPlaybackCollector()
        aiSpeakingTimeoutJob?.cancel()
        aiSpeakingTimeoutJob = null
        _userVolume.value = 0f
        _aiVolume.value = 0f
        applicationScope.launch(IODispatcher) {
            liveCallSocketClient.stopCall()
        }
    }

    fun setMuted(muted: Boolean) {
        audioCapture.setMuted(muted)
    }

    private var audioCaptureJob: Job? = null
    private var playbackCollectorJob: Job? = null

    private fun startAudioCapture() {
        stopAudioCapture()

        audioCaptureJob = applicationScope.launch(IODispatcher) {
            liveCallSocketClient.state.collect { state ->
                when (state.phase) {
                    LiveCallSocketClient.LiveCallState.Phase.ACTIVE -> {
                        if (!audioCapture.isRecording) {
                            audioCapture.start { pcmData ->
                                if (shouldSuppressMicrophoneUpload()) {
                                    _userVolume.value = 0f
                                    return@start
                                }
                                _userVolume.value = computeRms(pcmData)
                                applicationScope.launch(IODispatcher) {
                                    liveCallSocketClient.sendAudioChunk(pcmData)
                                }
                            }
                        }
                    }
                    LiveCallSocketClient.LiveCallState.Phase.INTERRUPTED -> {
                        audioPlayback.clear()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun computeRms(pcmData: ByteArray): Float {
        if (pcmData.isEmpty()) return 0f
        var sumOfSquares = 0.0
        val sampleCount = pcmData.size / 2
        for (i in 0 until sampleCount) {
            val low = pcmData[i * 2].toInt() and 0xFF
            val high = pcmData[i * 2 + 1].toInt()
            val sample = (high shl 8) or low
            val normalized = sample / 32768.0
            sumOfSquares += normalized * normalized
        }
        val rms = sqrt(sumOfSquares / sampleCount)
        return rms.toFloat().coerceIn(0f, 1f)
    }

    private fun stopAudioCapture() {
        audioCaptureJob?.cancel()
        audioCaptureJob = null
        suppressMicUntilMs = 0L
        if (audioCapture.isRecording) audioCapture.stop()
    }

    private fun shouldSuppressMicrophoneUpload(): Boolean {
        if (currentTimeMillis() >= suppressMicUntilMs) return false
        if (proximityAudioManager.isNear.value) return false
        return true
    }

    private fun startAudioPlaybackCollector() {
        stopAudioPlaybackCollector()

        playbackCollectorJob = liveCallSocketClient.incomingMessages
            .onEach { event ->
                if (event is LiveCallServerEvent.AudioChunk) {
                    audioPlayback.enqueue(event.audio)
                    scheduleAiVolumeTimeout()
                }
            }
            .launchIn(applicationScope)

        applicationScope.launch {
            audioPlayback.volumeFlow.collect { volume -> _aiVolume.value = volume }
        }
    }

    private var aiSpeakingTimeoutJob: Job? = null

    private fun scheduleAiVolumeTimeout() {
        aiSpeakingTimeoutJob?.cancel()
        aiSpeakingTimeoutJob = applicationScope.launch {
            delay(AI_SPEAKING_TIMEOUT_MS)
            _aiVolume.value = 0f
        }
    }

    private fun stopAudioPlaybackCollector() {
        playbackCollectorJob?.cancel()
        playbackCollectorJob = null
    }

    private fun stopAudioPlayback() {
        if (audioPlayback.isPlaying) audioPlayback.stop()
    }
}
