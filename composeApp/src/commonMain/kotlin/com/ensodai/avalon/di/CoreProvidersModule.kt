package com.ensodai.avalon.di

import com.ensodai.avalon.data.ResourceWrapper
import com.ensodai.avalon.navigation.DeepLinkManager
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.KermitLogger
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class CoreProvidersModule {

    @Single
    fun provideAppLogger(): AppLogger = KermitLogger()

    @Single
    fun provideResourceWrapper(): ResourceWrapper = ResourceWrapper()

    @Single
    fun provideDeepLinkManager(): DeepLinkManager = DeepLinkManager()
}
