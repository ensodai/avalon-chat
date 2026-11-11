package com.ensodai.avalon.di

import android.content.Context
import coil3.ImageLoader
import coil3.imageLoader
import com.ensodai.avalon.domain.image.AndroidImageCacheManager
import com.ensodai.avalon.domain.image.ImageCacheManager
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class AndroidImageLoaderModule {

    @Single
    fun provideImageLoader(context: Context): ImageLoader {
        return context.imageLoader
    }

    @Single
    fun provideImageCacheManager(imageLoader: ImageLoader): ImageCacheManager {
        return AndroidImageCacheManager(imageLoader)
    }
}
