package com.ensodai.avalon.domain.image

interface ImageCacheManager {
    suspend fun clearMemory()
    suspend fun clearAll()
}
