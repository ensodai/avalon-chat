package com.ensodai.avalon.domain.audio

/**
 * Захват аудио с микрофона.
 *
 * Выдаёт PCM-чанки в коллбек [onChunk].
 * Каждый чанк — сырые байты в формате, который ожидает сервер
 * (по умолчанию PCM 16kHz mono 16-bit).
 */
interface AudioCapture {

    /**
     * Начать запись.
     * @param onChunk вызывается для каждого готового PCM-чанка
     */
    fun start(onChunk: (ByteArray) -> Unit)

    /** Остановить запись и освободить ресурсы. */
    fun stop()

    /** Пауза/возобновление. true = приостановить, false = возобновить. */
    fun setMuted(muted: Boolean)

    val isRecording: Boolean
}
