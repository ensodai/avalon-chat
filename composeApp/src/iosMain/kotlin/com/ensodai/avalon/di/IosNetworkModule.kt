package com.ensodai.avalon.di

import com.ensodai.avalon.config.AppBuildConfig
import com.ensodai.avalon.domain.model.PlatformMetadata
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import com.ensodai.avalon.data.provider.IosPushTokenProvider
import com.ensodai.avalon.data.provider.PushTokenProvider
import com.ensodai.avalon.utils.FileSaver
import com.ensodai.avalon.utils.IosFileSaver
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}

val iosPlatformModule = module {
    single<HttpClientEngine> { Darwin.create() }
    single<DataStorePathProvider> {
        object : DataStorePathProvider {
            override fun getPath(): String = documentDirectory() + "/app_preferences.preferences_pb"
        }
    }
    single<PushTokenProvider> { IosPushTokenProvider() }
    single<FileSaver> { IosFileSaver() }
    single {
        PlatformMetadata(
            versionName = AppBuildConfig.VERSION_NAME,
            versionCode = AppBuildConfig.VERSION_CODE,
            platform = "iOS"
        )
    }
}
