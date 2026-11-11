package com.ensodai.avalon.di

import com.ensodai.avalon.config.AppBuildConfig
import com.ensodai.avalon.domain.model.PlatformMetadata
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttpConfig
import io.ktor.client.engine.okhttp.OkHttpEngine
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import java.net.Proxy
import java.util.concurrent.TimeUnit

@Module
class AndroidNetworkModule {

    @Single
    fun provideHttpClientEngine(): HttpClientEngine = OkHttpEngine(
        OkHttpConfig().apply {
            config {
                retryOnConnectionFailure(true)
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
                proxy(Proxy.NO_PROXY) // Исключаем влияние системных прокси эмулятора
            }
        }
    )

    @Single
    fun providePlatformMetadata(): PlatformMetadata {
        return PlatformMetadata(
            versionName = AppBuildConfig.VERSION_NAME,
            versionCode = AppBuildConfig.VERSION_CODE,
            platform = "Android"
        )
    }
}
