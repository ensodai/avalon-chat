package com.ensodai.avalon.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private class AndroidPlatformAppActions(
    private val activity: Activity?
) : PlatformAppActions {
    override fun openAppSettings() {
        val targetActivity = activity ?: return
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", targetActivity.packageName, null)
        }
        targetActivity.startActivity(intent)
    }

    override fun closeApp() {
        activity?.finishAffinity()
    }

    override fun reloadApp() = Unit
}

@SuppressLint("ContextCastToActivity")
@Composable
actual fun rememberPlatformAppActions(): PlatformAppActions {
    val activity = LocalContext.current as? Activity
    return remember(activity) {
        AndroidPlatformAppActions(activity)
    }
}
