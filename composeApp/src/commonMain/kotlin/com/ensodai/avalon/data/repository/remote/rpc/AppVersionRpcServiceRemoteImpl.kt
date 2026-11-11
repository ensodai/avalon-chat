package com.ensodai.avalon.data.repository.remote.rpc

import com.ensodai.shared.avalon.rpc.services.AppVersionCheckResponse
import com.ensodai.shared.avalon.rpc.services.AppVersionRpcService
import com.ensodai.shared.avalon.rpc.services.CheckAppVersionRequest
import com.ensodai.avalon.di.RpcConnectionManager
import kotlinx.rpc.withService

open class AppVersionRpcServiceRemoteImpl(
    private val manager: RpcConnectionManager,
    private val executor: RpcCallExecutor
) : AppVersionRpcService {

    open suspend fun getService(): AppVersionRpcService = manager.getActiveClient().withService<AppVersionRpcService>()

    override suspend fun checkVersion(request: CheckAppVersionRequest): AppVersionCheckResponse =
        executor.execute("checkVersion", getService = { getService() }) { checkVersion(request) }
}
