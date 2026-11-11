package com.ensodai.avalon.domain.audio

import android.app.Application
import java.io.File
import kotlin.uuid.Uuid

class AndroidTtsCacheManager(
    private val application: Application
) : TtsCacheManager {

    private val cacheDir: File
        get() = File(application.cacheDir, "tts_audio").apply {
            if (!exists()) {
                mkdirs()
            }
        }

    override fun saveTtsAudio(contentId: Uuid, bytes: ByteArray): String {
        val file = File(cacheDir, "tts_$contentId.wav")
        file.writeBytes(bytes)
        return file.absolutePath
    }

    override fun getTtsAudioPath(contentId: Uuid): String? {
        val file = File(cacheDir, "tts_$contentId.wav")
        return if (file.exists()) file.absolutePath else null
    }

    override fun hasTtsAudio(contentId: Uuid): Boolean {
        return File(cacheDir, "tts_$contentId.wav").exists()
    }

    override fun clearCache() {
        try {
            cacheDir.deleteRecursively()
        } catch (e: Exception) {
            // ignore
        }
    }
}
