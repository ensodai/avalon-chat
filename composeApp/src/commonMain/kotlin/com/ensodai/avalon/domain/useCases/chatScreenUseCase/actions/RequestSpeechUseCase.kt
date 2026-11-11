package com.ensodai.avalon.domain.useCases.chatScreenUseCase.actions

import com.ensodai.shared.avalon.chat.services.ChatRpcService
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.domain.audio.TtsPlayer
import com.ensodai.avalon.domain.audio.TtsCacheManager
import com.ensodai.avalon.utils.L
import kotlinx.coroutines.flow.collect
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class RequestSpeechUseCase(
    private val chatRpcService: ChatRpcService,
    private val ttsPlayer: TtsPlayer,
    private val ttsCacheManager: TtsCacheManager
) {
    suspend fun execute(messageContentId: Uuid, voice: VoiceModel?) {
        L.d(TAG, "execute: messageContentId=$messageContentId")

        // 1. Проверяем, есть ли озвучка в локальном кэше
        if (ttsCacheManager.hasTtsAudio(messageContentId)) {
            val localPath = ttsCacheManager.getTtsAudioPath(messageContentId)
            if (localPath != null) {
                L.d(TAG, "🔊 Found in cache. Playing locally: path=$localPath")
                ttsPlayer.play(messageContentId, localPath)
                return
            }
        }

        // 2. Если в кэше нет — шлем RPC-запрос и стримим чанки
        L.d(TAG, "🌐 Not in cache. Requesting speech from server via RPC...")
        ttsPlayer.startStreaming(messageContentId)
        try {
            val chunks = mutableListOf<ByteArray>()
            chatRpcService.requestSpeech(messageContentId, voice).collect { chunk ->
                chunks.add(chunk)
                ttsPlayer.playChunk(chunk)
            }

            ttsPlayer.stopStreaming()

            val totalSize = chunks.sumOf { it.size }
            if (totalSize > 0) {
                val allBytes = ByteArray(totalSize)
                var offset = 0
                for (chunk in chunks) {
                    chunk.copyInto(allBytes, destinationOffset = offset)
                    offset += chunk.size
                }

                // Генерируем 44-байтный WAV заголовок
                val wavHeader = createWavHeader(allBytes.size.toLong())
                val wavBytes = wavHeader + allBytes

                // 3. Сохраняем в кэш
                val savedPath = ttsCacheManager.saveTtsAudio(messageContentId, wavBytes)
                L.d(TAG, "💾 Saved TTS audio to cache: path=$savedPath, size=${wavBytes.size} bytes")
            } else {
                L.e(TAG, "❌ Received empty audio flow from server")
                ttsPlayer.stop()
            }
        } catch (e: Exception) {
            L.e(TAG, "❌ Failed to request or save speech: ${e.message}")
            ttsPlayer.stop()
            throw e
        }
    }

    private fun createWavHeader(pcmSize: Long, sampleRate: Int = 24000, bitsPerSample: Int = 16, channels: Int = 1): ByteArray {
        val headerSize = 44
        val totalDataLen = pcmSize + 36
        val byteRate = (sampleRate * channels * bitsPerSample) / 8
        val blockAlign = (channels * bitsPerSample) / 8

        val header = ByteArray(headerSize)
        
        // RIFF header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        
        // Total length
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        
        // WAVE
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        
        // fmt subchunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        
        // fmt subchunk size (16 for PCM)
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        
        // Audio format (1 for PCM)
        header[20] = 1
        header[21] = 0
        
        // Channels
        header[22] = channels.toByte()
        header[23] = 0
        
        // Sample rate
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        
        // Byte rate
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        
        // Block align
        header[32] = blockAlign.toByte()
        header[33] = 0
        
        // Bits per sample
        header[34] = bitsPerSample.toByte()
        header[35] = 0
        
        // data subchunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        
        // data subchunk size (pcmSize)
        header[40] = (pcmSize and 0xff).toByte()
        header[41] = ((pcmSize shr 8) and 0xff).toByte()
        header[42] = ((pcmSize shr 16) and 0xff).toByte()
        header[43] = ((pcmSize shr 24) and 0xff).toByte()
        
        return header
    }

    companion object {
        private const val TAG = "RequestSpeechUseCase"
    }
}
