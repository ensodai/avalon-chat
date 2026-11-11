package com.ensodai.shared.avalon.rpc.services

import kotlinx.serialization.Serializable
import kotlinx.rpc.annotations.Rpc

@Serializable
data class CheckAppVersionRequest(
    val versionCode: Long,
    val versionName: String,
    val platform: String
)

@Serializable
data class AppVersionCheckResponse(
    val isUpdateRequired: Boolean,
    val minVersionCode: Long,
    val actualAppVersion: String,
    val updateUrl: String = ""
)

@Rpc
interface AppVersionRpcService {
    suspend fun checkVersion(request: CheckAppVersionRequest): AppVersionCheckResponse
}
