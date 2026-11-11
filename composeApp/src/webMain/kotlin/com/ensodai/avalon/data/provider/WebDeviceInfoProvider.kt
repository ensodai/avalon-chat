package com.ensodai.avalon.data.provider

import kotlin.uuid.Uuid

@JsName("globalThis")
external val globalThis: WebGlobalThis

external interface WebGlobalThis {
    val localStorage: WebStorage?
    val crypto: WebCrypto?
    val navigator: WebNavigator?
}

external interface WebStorage {
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
}

external interface WebCrypto {
    fun randomUUID(): String
}

external interface WebNavigator {
    val userAgent: String?
}

class WebDeviceInfoProvider : DeviceInfoProvider {
    override fun getDeviceId(): String {
        val key = "web_device_id"
        val storage = globalThis.localStorage

        val existing = storage?.getItem(key)
        if (!existing.isNullOrBlank()) {
            return existing
        }

        val newId = runCatching {
            globalThis.crypto?.randomUUID()
        }.getOrNull() ?: Uuid.random().toString()

        runCatching {
            storage?.setItem(key, newId)
        }
        return newId

    }

    override fun getDeviceName(): String {
        val userAgent = globalThis.navigator?.userAgent.orEmpty()

        return when {
            "Android" in userAgent -> "Web Android"
            "iPhone" in userAgent || "iPad" in userAgent -> "Web iOS"
            "Windows" in userAgent -> "Web Windows"
            "Mac OS" in userAgent || "Macintosh" in userAgent -> "Web macOS"
            "Linux" in userAgent -> "Web Linux"
            else -> "Web"
        }
    }
}
