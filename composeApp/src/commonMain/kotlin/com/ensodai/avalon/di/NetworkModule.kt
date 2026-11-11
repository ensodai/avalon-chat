package com.ensodai.avalon.di

import com.ensodai.avalon.config.AppBuildConfig
import com.ensodai.avalon.data.network.interceptors.InstantSerializer
import com.ensodai.avalon.data.network.plugins.AppVersionPlugin
import com.ensodai.avalon.data.network.plugins.ServerTimePlugin
import com.ensodai.avalon.data.network.plugins.VersionCheckPlugin
import com.ensodai.avalon.data.repository.local.DatastoreManager
import com.ensodai.avalon.domain.global.GlobalEventBus
import com.ensodai.avalon.domain.model.PlatformMetadata
import com.ensodai.avalon.domain.sessionWorker.ServerTimeManager
import com.ensodai.avalon.utils.L
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.koin.core.annotation.Module
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single
import kotlinx.rpc.krpc.ktor.client.installKrpc

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class KtorHttpClient

@Module
class NetworkProvidersModule {

    @Single
    fun provideJsonParser(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = false
            encodeDefaults = false
            explicitNulls = true
            serializersModule = SerializersModule {
                contextual(InstantSerializer)
            }
            classDiscriminator = "type"
        }
    }

    @Single
    fun provideMutex(): Mutex = Mutex()

    @Single
    @KtorHttpClient
    fun provideKtorHttpClient(
        engine: HttpClientEngine,
        json: Json,
        metadata: PlatformMetadata,
        serverTimeManager: ServerTimeManager,
        eventBus: GlobalEventBus,
        @ApplicationScope appScope: CoroutineScope,
        datastoreManager: DatastoreManager
    ): HttpClient {
        return HttpClient(engine) {
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 60_000
            }
            install(WebSockets) {
                pingIntervalMillis = 20_000
            }
            installKrpc()
            // install(AppVersionPlugin) { this.metadata = metadata } // Выпилено: версия передается через RPC
            install(ServerTimePlugin) { this.serverTimeManager = serverTimeManager }
            install(VersionCheckPlugin) {
                this.eventBus = eventBus
                this.appScope = appScope
                this.json = json
            }
            install(Logging) {
                level = if (AppBuildConfig.IS_DEBUG) LogLevel.INFO else LogLevel.NONE
                logger = object : Logger {
                    override fun log(message: String) {
                        L.d("Ktor", message)
                    }
                }
            }
            defaultRequest { 
                url(platformBaseUrl) 
            }
            expectSuccess = true
        }
    }
}