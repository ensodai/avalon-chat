package com.ensodai.avalon.domain.audio

import kotlinx.coroutines.flow.StateFlow

/**
 * Воспроизведение PCM-аудио.
 *
 * Принимает сырые PCM-байты от сервера и проигрывает их.
 * Ожидается формат: PCM 24kHz mono 16-bit (совпадает с output Gemini).
 */
interface AudioPlayback {

    /** Поставить PCM-данные в очередь воспроизведения. */
    fun enqueue(pcmData: ByteArray)

    /** Очистить очередь ожидающих чанков, не останавливая текущее воспроизведение. */
    fun clear()

    /** Остановить воспроизведение и очистить очередь. */
    fun stop()

    val isPlaying: Boolean

    /**
     * Громкость воспроизводимого аудио (0.0 — 1.0), RMS от последнего чанка.
     * Сбрасывается в 0 когда воспроизведение останавливается.
     */
    val volumeFlow: StateFlow<Float>
}
