package com.ensodai.avalon.data.provider

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings

class AndroidDeviceInfoProvider(private val context: Context) : DeviceInfoProvider {
    @SuppressLint("HardwareIds")
    override fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    override fun getDeviceName(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }
}
