package com.ensodai.avalon.utils

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

actual fun isAppInDebugMode(): Boolean {
    @OptIn(ExperimentalNativeApi::class)
    return Platform.isDebugBinary
}
