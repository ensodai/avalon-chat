package com.ensodai.avalon.domain.sessionWorker

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

class AndroidSessionPlatformBridge(
    context: Context,
    private val sessionWorker: SessionWorker
) : DefaultLifecycleObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            sessionWorker.onNetworkAvailable()
        }
    }

    private var started = false

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun start(lifecycle: Lifecycle) {
        if (started) return
        started = true

        lifecycle.addObserver(this)

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    override fun onStart(owner: LifecycleOwner) {
        sessionWorker.onAppForeground()
    }
}
