package com.ensodai.avalon.di

import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module

@Module(
    includes = [
        AppModule::class,
        WebPlatformModule::class,
        WebDataModule::class,
        WebNetworkModule::class,
        WebAudioStubModule::class
    ]
)
class WebAppModule

@KoinApplication(modules = [WebAppModule::class])
object WebKoinAppConfig
