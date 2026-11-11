package com.ensodai.avalon.domain.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.ensodai.avalon.utils.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Android-реализация захвата аудио через AudioRecord.
 *
 * Формат: PCM 16kHz mono 16-bit — стандартный для Gemini Live API.
 * Чанки отдаются в коллбек [onChunk] каждые ~20ms.
 */
class AndroidAudioCapture(
    private val appLogger: AppLogger,
    private val scope: CoroutineScope
) : AudioCapture {

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val ENCODING = AudioFormat.ENCODING_PCM_16BIT
        // ~20ms буфер = 16000 * 2 bytes * 0.02s = 640 bytes
        private const val BUFFER_SIZE_MULTIPLIER = 0.02
    }

    private var audioRecord: AudioRecord? = null
    private var captureJob: Job? = null
    private var muted = false

    override var isRecording: Boolean = false
        private set

    @SuppressLint("MissingPermission")
    override fun start(onChunk: (ByteArray) -> Unit) {
        if (isRecording) {
            appLogger.w("AndroidAudioCapture", "Already recording, ignoring start")
            return
        }

        val minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, ENCODING)
        val desiredBuf = (SAMPLE_RATE * 2 * BUFFER_SIZE_MULTIPLIER).toInt()
        val bufferSize = desiredBuf.coerceAtLeast(minBuf)

        val record = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            ENCODING,
            bufferSize
        )

        if (record.state != AudioRecord.STATE_INITIALIZED) {
            appLogger.e("AndroidAudioCapture", "AudioRecord failed to initialize, state=${record.state}")
            record.release()
            return
        }

        audioRecord = record
        isRecording = true
        muted = false

        captureJob = scope.launch(Dispatchers.IO) {
            record.startRecording()
            val buffer = ByteArray(bufferSize)

            while (isActive && isRecording) {
                val read = record.read(buffer, 0, buffer.size)
                if (read > 0) {
                    if (!muted) {
                        onChunk(buffer.copyOf(read))
                    }
                } else if (read < 0) {
                    appLogger.e("AndroidAudioCapture", "AudioRecord read error: $read")
                    break
                }
            }
        }
    }

    override fun stop() {
        isRecording = false
        captureJob?.cancel()
        captureJob = null
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    override fun setMuted(muted: Boolean) {
        this.muted = muted
    }
}
