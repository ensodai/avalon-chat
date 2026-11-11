package com.ensodai.avalon.data.repository.remote.rpc

import com.ensodai.shared.avalon.rpc.services.SessionSupportRpcService
import com.ensodai.shared.avalon.session.models.*
import com.ensodai.avalon.di.RpcConnectionManager
import kotlinx.rpc.withService

open class SessionSupportRpcServiceRemoteImpl(
    private val manager: RpcConnectionManager,
    private val executor: RpcCallExecutor
) : SessionSupportRpcService {

    open suspend fun getService(): SessionSupportRpcService = manager.getActiveClient().withService<SessionSupportRpcService>()

    override suspend fun updateChatSessionsBatch(request: ChatSessionsBatchRequest): UpdateChatSessionsResponse =
        executor.execute("updateChatSessionsBatch", getService = { getService() }) { updateChatSessionsBatch(request) }

    override suspend fun deleteChatSessionsBatch(request: ChatSessionsDeleteBatchRequest) =
        executor.execute("deleteChatSessionsBatch", getService = { getService() }) { deleteChatSessionsBatch(request) }

    override suspend fun submitReport(request: ReportRequest): ReportResponse =
        executor.execute("submitReport", getService = { getService() }) { submitReport(request) }
}
