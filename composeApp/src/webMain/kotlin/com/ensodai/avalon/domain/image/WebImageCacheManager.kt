package com.ensodai.avalon.domain.image

class WebImageCacheManager : ImageCacheManager {
    override suspend fun clearMemory() = Unit

    override suspend fun clearAll() = Unit
}
