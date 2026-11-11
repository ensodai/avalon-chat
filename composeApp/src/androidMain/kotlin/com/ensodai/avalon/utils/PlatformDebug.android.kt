package com.ensodai.avalon.utils

import com.ensodai.avalon.config.AppBuildConfig

actual fun isAppInDebugMode(): Boolean {
    return AppBuildConfig.IS_DEBUG
}
