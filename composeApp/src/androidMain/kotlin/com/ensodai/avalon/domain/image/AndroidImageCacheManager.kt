package com.ensodai.avalon.domain.image

import coil3.ImageLoader

class AndroidImageCacheManager(
    private val imageLoader: ImageLoader
) : ImageCacheManager {

    override suspend fun clearMemory() {
        imageLoader.memoryCache?.clear()
    }

    override suspend fun clearAll() {
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
    }
}
