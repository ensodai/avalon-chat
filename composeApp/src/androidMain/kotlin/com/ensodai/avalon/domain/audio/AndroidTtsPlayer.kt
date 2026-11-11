package com.ensodai.avalon.domain.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import com.ensodai.avalon.utils.AppLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.uuid.Uuid

class AndroidTtsPlayer(
    private val appLogger: AppLogger
) : TtsPlayer {

    private val lock = Any()
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var audioTrack: AudioTrack? = null
    private var streamingChannel: Channel<ByteArray>? = null
    private var streamingJob: Job? = null
    private var totalFramesWritten = 0L
    private var lastComputedAmplitude = 0f

    private val _state = MutableStateFlow<TtsPlayerState>(TtsPlayerState.Idle)
    override val state: StateFlow<TtsPlayerState> = _state.asStateFlow()

    private val _currentContentId = MutableStateFlow<Uuid?>(null)
    override val currentContentId: StateFlow<Uuid?> = _currentContentId.asStateFlow()

    override fun startLoading(contentId: Uuid) {
        appLogger.d("AndroidTtsPlayer", "⏳ startLoading: contentId=$contentId")
        synchronized(lock) {
            stopInternal()
            _currentContentId.value = contentId
            _state.value = TtsPlayerState.Loading
        }
    }

    override fun startStreaming(contentId: Uuid) {
        appLogger.d("AndroidTtsPlayer", "🎙️ startStreaming: contentId=$contentId")
        synchronized(lock) {
            stopInternal()
            _currentContentId.value = contentId
            _state.value = TtsPlayerState.Loading
            totalFramesWritten = 0L
            lastComputedAmplitude = 0f

            val bufferSize = AudioTrack.getMinBufferSize(
                24000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            var track: AudioTrack? = null
            if (bufferSize > 0) {
                try {
                    track = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setSampleRate(24000)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .build()
                        )
                        .setBufferSizeInBytes(bufferSize)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()
                    track.play()
                    audioTrack = track
                } catch (e: Exception) {
                    appLogger.e("AndroidTtsPlayer", "Failed to init AudioTrack: ${e.message}")
                }
            }

            val channel = Channel<ByteArray>(Channel.UNLIMITED)
            streamingChannel = channel

            if (track != null) {
                startStreamingProgressTracking(contentId, track)
            }

            streamingJob = coroutineScope.launch(Dispatchers.IO) {
                for (chunk in channel) {
                    val t = audioTrack ?: break
                    
                    val amplitude = computeRms(chunk)
                    synchronized(lock) {
                        totalFramesWritten += chunk.size / 2
                        lastComputedAmplitude = amplitude
                    }

                    try {
                        t.write(chunk, 0, chunk.size, AudioTrack.WRITE_BLOCKING)
                    } catch (e: Exception) {
                        appLogger.e("AndroidTtsPlayer", "Error writing to AudioTrack: ${e.message}")
                    }
                }
                
                // Ожидаем, пока AudioTrack физически доиграет все записанные фреймы из буфера
                try {
                    val track = audioTrack
                    if (track != null) {
                        var head = track.playbackHeadPosition.toLong() and 0xFFFFFFFFL
                        var timeoutMillis = 5000L
                        while (head < totalFramesWritten && timeoutMillis > 0 && audioTrack === track) {
                            delay(100)
                            synchronized(lock) {
                                if (_state.value !is TtsPlayerState.Paused) {
                                    timeoutMillis -= 100
                                }
                            }
                            head = track.playbackHeadPosition.toLong() and 0xFFFFFFFFL
                        }
                        
                        // Принудительно выставляем 1.0 в прогресс перед выходом
                        if (audioTrack === track) {
                            val lastAmp = (_state.value as? TtsPlayerState.Playing)?.currentAmplitude ?: 0f
                            _state.value = TtsPlayerState.Playing(
                                progress = 1f,
                                currentAmplitude = lastAmp,
                                isStreaming = true,
                                lastUpdated = System.nanoTime()
                            )
                            delay(200)
                        }
                    }
                } catch (e: Exception) {
                    appLogger.e("AndroidTtsPlayer", "Error waiting for AudioTrack completion: ${e.message}")
                }

                // Когда канал закрыт, прочитаны и проиграны все чанки, освобождаем ресурсы
                synchronized(lock) {
                    if (audioTrack != null) {
                        try {
                            audioTrack?.stop()
                            audioTrack?.release()
                        } catch (e: Exception) {
                            // ignore
                        } finally {
                            audioTrack = null
                            _currentContentId.value = null
                            _state.value = TtsPlayerState.Idle
                        }
                    }
                }
            }
        }
    }

    override fun playChunk(chunk: ByteArray) {
        streamingChannel?.trySend(chunk)
    }

    override fun stopStreaming() {
        appLogger.d("AndroidTtsPlayer", "🎙️ stopStreaming")
        synchronized(lock) {
            streamingChannel?.close()
            streamingChannel = null
            streamingJob = null
        }
    }

    override fun play(contentId: Uuid, url: String) {
        appLogger.d("AndroidTtsPlayer", "▶️ play: contentId=$contentId, url=$url")
        synchronized(lock) {
            stopInternal()
            _currentContentId.value = contentId
            _state.value = TtsPlayerState.Loading

            try {
                val player = MediaPlayer().apply {
                    setDataSource(url)
                    setOnPreparedListener { mp ->
                        synchronized(lock) {
                            if (_currentContentId.value == contentId) {
                                mp.start()
                                _state.value = TtsPlayerState.Playing(0f, isStreaming = false)
                                startProgressTracking(contentId, mp)
                            } else {
                                mp.release()
                            }
                        }
                    }
                    setOnCompletionListener {
                        synchronized(lock) {
                            if (_currentContentId.value == contentId) {
                                coroutineScope.launch {
                                    _state.value = TtsPlayerState.Playing(
                                        progress = 1f,
                                        currentAmplitude = 0f,
                                        isStreaming = false,
                                        lastUpdated = System.nanoTime()
                                    )
                                    delay(200)
                                    synchronized(lock) {
                                        if (_currentContentId.value == contentId) {
                                            stopInternal()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    setOnErrorListener { _, what, extra ->
                        appLogger.e("AndroidTtsPlayer", "❌ MediaPlayer error: what=$what, extra=$extra")
                        synchronized(lock) {
                            if (_currentContentId.value == contentId) {
                                stopInternal()
                            }
                        }
                        true
                    }
                    prepareAsync()
                }
                mediaPlayer = player
            } catch (e: Exception) {
                appLogger.e("AndroidTtsPlayer", "❌ Failed to play audio: ${e.message}" )
                stopInternal()
            }
        }
    }

    override fun pause() {
        appLogger.d("AndroidTtsPlayer", "⏸️ pause")
        synchronized(lock) {
            if (audioTrack != null) {
                try {
                    audioTrack?.pause()
                    val stateValue = _state.value
                    if (stateValue is TtsPlayerState.Playing) {
                        _state.value = TtsPlayerState.Paused(stateValue.progress)
                    }
                } catch (e: Exception) {
                    appLogger.e("AndroidTtsPlayer", "❌ Failed to pause audioTrack: ${e.message}")
                }
                return
            }

            val player = mediaPlayer ?: return
            if (player.isPlaying) {
                try {
                    player.pause()
                    val progress = getCurrentProgress(player)
                    _state.value = TtsPlayerState.Paused(progress)
                    stopProgressTracking()
                } catch (e: Exception) {
                    appLogger.e("AndroidTtsPlayer", "❌ Failed to pause: ${e.message}")
                }
            }
        }
    }

    override fun resume() {
        appLogger.d("AndroidTtsPlayer", "▶️ resume")
        synchronized(lock) {
            if (audioTrack != null) {
                try {
                    audioTrack?.play()
                    val stateValue = _state.value
                    if (stateValue is TtsPlayerState.Paused) {
                        _state.value = TtsPlayerState.Playing(
                            progress = stateValue.progress,
                            isStreaming = true,
                            lastUpdated = System.nanoTime()
                        )
                    }
                } catch (e: Exception) {
                    appLogger.e("AndroidTtsPlayer", "❌ Failed to resume audioTrack: ${e.message}")
                }
                return
            }

            val player = mediaPlayer ?: return
            val contentId = _currentContentId.value ?: return
            if (!player.isPlaying) {
                try {
                    player.start()
                    _state.value = TtsPlayerState.Playing(getCurrentProgress(player), isStreaming = false)
                    startProgressTracking(contentId, player)
                } catch (e: Exception) {
                    appLogger.e("AndroidTtsPlayer", "❌ Failed to resume: ${e.message}")
                }
            }
        }
    }

    override fun stop() {
        appLogger.d("AndroidTtsPlayer", "⏹️ stop")
        synchronized(lock) {
            stopInternal()
        }
    }

    private fun stopInternal() {
        stopProgressTracking()
        streamingChannel?.close()
        streamingChannel = null
        streamingJob?.cancel()
        streamingJob = null
        
        try {
            audioTrack?.let { track ->
                track.stop()
                track.release()
            }
        } catch (e: Exception) {
            // ignore
        } finally {
            audioTrack = null
        }

        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (e: Exception) {
            // ignore
        } finally {
            mediaPlayer = null
            _currentContentId.value = null
            _state.value = TtsPlayerState.Idle
        }
    }

    private fun startProgressTracking(contentId: Uuid, player: MediaPlayer) {
        progressJob?.cancel()
        progressJob = coroutineScope.launch {
            while (isActive) {
                synchronized(lock) {
                    if (mediaPlayer === player && player.isPlaying && _currentContentId.value == contentId) {
                        _state.value = TtsPlayerState.Playing(getCurrentProgress(player), isStreaming = false)
                    } else {
                        cancel()
                    }
                }
                delay(100)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun getCurrentProgress(player: MediaPlayer): Float {
        return try {
            val duration = player.duration
            if (duration > 0) {
                player.currentPosition.toFloat() / duration.toFloat()
            } else 0f
        } catch (e: Exception) {
            0f
        }
    }

    private fun startStreamingProgressTracking(contentId: Uuid, track: AudioTrack) {
        progressJob?.cancel()
        progressJob = coroutineScope.launch {
            while (isActive) {
                var currentHead = 0L
                var totalFrames = 0L
                var amplitude = 0f
                var isChannelClosed = false
                synchronized(lock) {
                    if (audioTrack === track && _currentContentId.value == contentId) {
                        currentHead = track.playbackHeadPosition.toLong() and 0xFFFFFFFFL
                        totalFrames = totalFramesWritten
                        amplitude = lastComputedAmplitude
                        isChannelClosed = streamingChannel == null
                    } else {
                        cancel()
                    }
                }
                if (isActive && totalFrames > 0 && (currentHead > 0 || isChannelClosed)) {
                    val progress = (currentHead.toFloat() / totalFrames.toFloat()).coerceIn(0f, 1f)
                    
                    synchronized(lock) {
                        if (audioTrack === track && _currentContentId.value == contentId) {
                            if (_state.value !is TtsPlayerState.Paused) {
                                _state.value = TtsPlayerState.Playing(
                                    progress = progress,
                                    currentAmplitude = amplitude,
                                    isStreaming = true,
                                    lastUpdated = System.nanoTime()
                                )
                            }
                        }
                    }
                }
                delay(100)
            }
        }
    }

    private fun computeRms(pcmData: ByteArray): Float {
        if (pcmData.isEmpty()) return 0f
        var sum = 0.0
        val numSamples = pcmData.size / 2
        if (numSamples == 0) return 0f
        for (i in 0 until numSamples) {
            val low = pcmData[i * 2].toInt() and 0xff
            val high = pcmData[i * 2 + 1].toInt()
            val sample = ((high shl 8) or low).toShort()
            sum += sample.toDouble() * sample.toDouble()
        }
        val rms = Math.sqrt(sum / numSamples)
        // Нормализуем значение в диапазон 0f..1f. Для человеческого голоса RMS редко
        // достигает максимума, поэтому делим на 8000 для лучшей чувствительности визуализатора.
        val normalized = (rms / 8000.0).toFloat()
        return normalized.coerceIn(0f, 1f)
    }
}
