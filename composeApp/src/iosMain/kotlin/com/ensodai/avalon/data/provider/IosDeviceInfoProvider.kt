package com.ensodai.avalon.data.provider

import platform.UIKit.UIDevice

class IosDeviceInfoProvider : DeviceInfoProvider {
    override fun getDeviceId(): String {
        return UIDevice.currentDevice.identifierForVendor?.UUIDString ?: "unknown_ios_id"
    }

    override fun getDeviceName(): String {
        return "${UIDevice.currentDevice.systemName} ${UIDevice.currentDevice.systemVersion} (${UIDevice.currentDevice.model})"
    }
}
