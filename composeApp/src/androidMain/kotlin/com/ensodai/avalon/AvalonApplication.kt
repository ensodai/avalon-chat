package com.ensodai.avalon

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ProcessLifecycleOwner
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.ensodai.avalon.data.repository.local.DatastoreManager
import com.ensodai.avalon.di.AndroidKoinAppConfig
import com.ensodai.avalon.domain.paymentManager.PaymentManager
import com.ensodai.avalon.domain.sessionWorker.AndroidSessionPlatformBridge
import com.ensodai.avalon.domain.sessionWorker.SessionWorker
import com.ensodai.avalon.locale.AppLocaleManager
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.IODispatcher
import com.ensodai.avalon.utils.L
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.plugin.module.dsl.startKoin
import kotlin.system.exitProcess
import com.google.firebase.crashlytics.FirebaseCrashlytics

open class AvalonApplication : Application(), SingletonImageLoader.Factory {
    private lateinit var sessionWorker: SessionWorker
    private lateinit var sessionPlatformBridge: AndroidSessionPlatformBridge
    private lateinit var paymentManager: PaymentManager
    lateinit var datastoreManager: DatastoreManager

    private lateinit var appLogger: AppLogger

    override fun attachBaseContext(base: Context) {
        AppLocaleManager.initialize(base)
        super.attachBaseContext(AppLocaleManager.wrap(base))
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun getApplicationContext(): Context =
        AppLocaleManager.wrap(super.getApplicationContext())

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun onCreate() {
        super.onCreate()
        startKoin<AndroidKoinAppConfig> {
            androidLogger()
            androidContext(this@AvalonApplication)
        }

        val koin = GlobalContext.get()

        setupGlobalExceptionHandler()

        // Move initialization to background to avoid blocking the Main thread during database migration
        CoroutineScope(SupervisorJob() + IODispatcher).launch {
            sessionWorker = koin.get()
            sessionPlatformBridge =
                AndroidSessionPlatformBridge(this@AvalonApplication, sessionWorker)
            paymentManager = koin.get()
            datastoreManager = koin.get()
            appLogger = koin.get()

            L.init(appLogger)

            sessionWorker.start()

            // Lifecycle operations must be called on the main thread
            withContext(Dispatchers.Main) {
                sessionPlatformBridge.start(ProcessLifecycleOwner.get().lifecycle)
            }
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
            }
            .logger(DebugLogger())
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache").absolutePath.toPath())
                    .maxSizeBytes(1024 * 1024 * 512)
                    .build()
            }
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                L.e(
                    "AvalonApplication",
                    "Global Uncaught Exception detected: ${throwable.stackTraceToString()}"
                )

                val intent = Intent().apply {
                    setClassName(
                        this@AvalonApplication,
                        "com.ensodai.avalon.CrashActivity"
                    )
                    putExtra("error", throwable.stackTraceToString())
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }

                startActivity(intent)

                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(thread, throwable)
                } else {
                    android.os.Process.killProcess(android.os.Process.myPid())
                    exitProcess(2)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
