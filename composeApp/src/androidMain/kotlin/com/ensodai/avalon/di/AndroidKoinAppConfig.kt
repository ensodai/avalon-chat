package com.ensodai.avalon.di

import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module

@Module(
    includes = [
        AppModule::class,
        AndroidDataModule::class,
        AndroidImageLoaderModule::class,
        AndroidNetworkModule::class,
        AndroidPaymentModule::class,
        AndroidAudioModule::class
    ]
)
@Configuration
class AndroidAppModule

@KoinApplication(modules = [AndroidAppModule::class])
object AndroidKoinAppConfig

