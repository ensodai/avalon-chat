package com.ensodai.avalon.domain.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.ensodai.avalon.utils.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.sqrt

/**
 * Android-реализация воспроизведения PCM-аудио через AudioTrack.
 *
 * Формат: PCM 24kHz mono 16-bit — стандартный output Gemini Live API.
 * Чанки ставятся в очередь и воспроизводятся последовательно.
 */
class AndroidAudioPlayback(
    private val appLogger: AppLogger
) : AudioPlayback {

    companion object {
        private const val SAMPLE_RATE = 24000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    }

    private val lock = Any()
    private var audioTrack: AudioTrack? = null
    private val queue = LinkedBlockingQueue<ByteArray>()
    private val playing = AtomicBoolean(false)

    override var isPlaying: Boolean = false
        private set

    private val _volumeFlow = MutableStateFlow(0f)
    override val volumeFlow: StateFlow<Float> get() = _volumeFlow

    override fun enqueue(pcmData: ByteArray) {
        if (pcmData.isEmpty()) return

        queue.offer(pcmData)

        if (!playing.get()) {
            startPlayback()
        }
    }

    override fun clear() {
        queue.clear()
        synchronized(lock) {
            try {
                audioTrack?.flush()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    override fun stop() {
        playing.set(false)
        queue.clear()
        synchronized(lock) {
            audioTrack?.let { track ->
                try {
                    track.stop()
                    track.release()
                } catch (e: Exception) {
                    // ignore
                }
            }
            audioTrack = null
            isPlaying = false
        }
        _volumeFlow.value = 0f
    }

    private fun startPlayback() {
        if (playing.getAndSet(true)) return

        val bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, ENCODING)
        if (bufferSize <= 0) {
            playing.set(false)
            return
        }

        val track = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(CHANNEL_CONFIG)
                        .setEncoding(ENCODING)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (e: Exception) {
            appLogger.e("AndroidAudioPlayback", "Failed to create AudioTrack: $e")
            playing.set(false)
            return
        }

        synchronized(lock) {
            if (!playing.get()) {
                track.release()
                return
            }
            audioTrack = track
            isPlaying = true
            try {
                track.play()
            } catch (e: Exception) {
                appLogger.e("AndroidAudioPlayback", "Failed to start AudioTrack: $e")
                track.release()
                audioTrack = null
                isPlaying = false
                playing.set(false)
                return
            }
        }

        Thread {
            try {
                while (playing.get()) {
                    val chunk = queue.poll() ?: break
                    _volumeFlow.value = computeRms(chunk)
                    val result = track.write(chunk, 0, chunk.size, AudioTrack.WRITE_BLOCKING)
                    if (result < 0) {
                        appLogger.e("AndroidAudioPlayback", "Write error: $result")
                        break
                    }
                }
            } catch (e: Exception) {
                appLogger.e("AndroidAudioPlayback", "Playback error: $e")
            } finally {
                synchronized(lock) {
                    try {
                        track.stop()
                        track.release()
                    } catch (e: Exception) {
                        // ignore
                    }
                    if (audioTrack === track) {
                        audioTrack = null
                        isPlaying = false
                        playing.set(false)
                    }
                }
                _volumeFlow.value = 0f
            }
        }.start()
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
        return sqrt(sumOfSquares / sampleCount).toFloat().coerceIn(0f, 1f)
    }
}
