package com.ensodai.avalon

import com.ensodai.avalon.di.platformBaseUrl
import com.ensodai.avalon.di.platformLiveCallWebSocketBaseUrl
import com.ensodai.avalon.di.platformSyncWebSocketBaseUrl

class AndroidApp : AvalonApplication() {
    override fun onCreate() {
        platformBaseUrl = BuildConfig.BASE_URL
        platformSyncWebSocketBaseUrl = BuildConfig.SYNC_WS_URL
        platformLiveCallWebSocketBaseUrl = BuildConfig.LIVE_CALL_WS_URL
        
        android.util.Log.d("AndroidApp", "BASE_URL set to: ${platformBaseUrl}")
        
        super.onCreate()
    }
}
