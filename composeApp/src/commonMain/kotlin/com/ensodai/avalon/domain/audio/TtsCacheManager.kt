package com.ensodai.avalon.domain.audio

import kotlin.uuid.Uuid

interface TtsCacheManager {
    fun saveTtsAudio(contentId: Uuid, bytes: ByteArray): String
    fun getTtsAudioPath(contentId: Uuid): String?
    fun hasTtsAudio(contentId: Uuid): Boolean
    fun clearCache()
}
