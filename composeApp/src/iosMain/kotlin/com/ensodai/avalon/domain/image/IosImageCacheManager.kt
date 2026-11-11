package com.ensodai.avalon.domain.image

class IosImageCacheManager : ImageCacheManager {
    override suspend fun clearMemory() = Unit

    override suspend fun clearAll() = Unit
}
