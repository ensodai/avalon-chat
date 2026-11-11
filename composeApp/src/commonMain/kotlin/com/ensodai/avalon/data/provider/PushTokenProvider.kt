package com.ensodai.avalon.data.provider

interface PushTokenProvider {
    suspend fun getPushToken(): String?
}
