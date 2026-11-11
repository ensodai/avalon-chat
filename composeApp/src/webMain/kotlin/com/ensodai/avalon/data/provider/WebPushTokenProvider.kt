package com.ensodai.avalon.data.provider

class WebPushTokenProvider : PushTokenProvider {
    override suspend fun getPushToken(): String? = null
}
