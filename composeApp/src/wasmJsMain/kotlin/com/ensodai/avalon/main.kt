package com.ensodai.avalon

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import com.ensodai.avalon.config.AppBuildConfig
import com.ensodai.avalon.di.WebKoinAppConfig
import com.ensodai.avalon.domain.sessionWorker.SessionWorker
import com.ensodai.avalon.domain.sessionWorker.DatabaseHealthChecker
import com.ensodai.avalon.domain.sessionWorker.WebSessionPlatformBridge
import com.ensodai.avalon.theme.GemTheme
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.plugin.module.dsl.startKoin
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Coil initialization — ДО ComposeViewport
    SingletonImageLoader.setSafe(
        object : SingletonImageLoader.Factory {
            override fun newImageLoader(context: PlatformContext): ImageLoader {
                return ImageLoader.Builder(context)
                    .components {
                        add(object : coil3.map.Mapper<String, String> {
                            override fun map(data: String, options: coil3.request.Options): String? {
                                val uploadPrefix = "${AppBuildConfig.BASE_URL}/uploads/"
                                if (data.startsWith(uploadPrefix)) {
                                    val currentOrigin = kotlinx.browser.window.location.origin
                                    return data.replace(uploadPrefix, "$currentOrigin/uploads/")
                                }
                                return null
                            }
                        })
                    }
                    .memoryCache {
                        MemoryCache.Builder()
                            .maxSizeBytes(100_000_000) // 100MB
                            .build()
                    }
                    .diskCache(null)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .crossfade(true)
                    .build()
            }
        }
    )

    MainScope().launch {
        if (AppBuildConfig.IS_DEBUG) {
            println("WASM main started")
        }
//        val driver = initWasmDatabase()

        val koin = startKoin<WebKoinAppConfig> { }.koin

        val sessionWorker: SessionWorker = koin.get()
        val appDataBase: com.ensodai.avalon.data.db.AppDataBase = koin.get()
        val databaseHealthChecker = DatabaseHealthChecker(appDataBase)
        val sessionPlatformBridge = WebSessionPlatformBridge(sessionWorker, databaseHealthChecker)


        if (AppBuildConfig.IS_DEBUG) {
            println("WASM SessionWorker bootstrap")
        }
        sessionWorker.start()
        sessionPlatformBridge.start()

        val composeRoot = document.getElementById("compose-root") as? HTMLElement
            ?: error("Missing #compose-root container")

        ComposeViewport(
            viewportContainer = composeRoot,
            content = {
                LaunchedEffect(Unit) {
                    val loader = document.getElementById("avalon-html-loader") as? HTMLElement
                    if (loader != null) {
                        loader.style.opacity = "0"
                        delay(300) // Ждем завершения CSS анимации (transition: opacity 0.3s)
                        loader.remove()
                    }
                }
                GemTheme {
                    App()
                }
            }
        )
    }
}
