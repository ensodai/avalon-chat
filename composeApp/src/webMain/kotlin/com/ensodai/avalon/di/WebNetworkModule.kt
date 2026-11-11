package com.ensodai.avalon.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class WebNetworkModule {

    @Single
    fun provideHttpClientEngine(): HttpClientEngine = Js.create()
}
