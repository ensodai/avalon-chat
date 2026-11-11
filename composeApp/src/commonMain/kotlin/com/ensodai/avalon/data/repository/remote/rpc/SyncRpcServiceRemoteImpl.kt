@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.ensodai.avalon.data.repository.remote.rpc

import com.ensodai.shared.avalon.rpc.services.SyncRpcService
import com.ensodai.shared.avalon.sync.models.SyncChangesRequest
import com.ensodai.shared.avalon.sync.models.SyncResponse
import com.ensodai.avalon.di.RpcConnectionManager
import kotlinx.rpc.withService

open class SyncRpcServiceRemoteImpl(
    private val manager: RpcConnectionManager,
    private val executor: RpcCallExecutor
) : SyncRpcService {

    open suspend fun getService(): SyncRpcService = manager.getActiveClient().withService<SyncRpcService>()

    override suspend fun syncChanges(request: SyncChangesRequest): SyncResponse =
        executor.execute("syncChanges", getService = { getService() }) { syncChanges(request) }
}
