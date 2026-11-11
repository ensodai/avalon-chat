package com.ensodai.shared.avalon.rpc.services

import com.ensodai.shared.avalon.session.models.*
import kotlinx.rpc.annotations.Rpc

@Rpc
interface SessionSupportRpcService {
    suspend fun updateChatSessionsBatch(request: ChatSessionsBatchRequest): UpdateChatSessionsResponse
    suspend fun deleteChatSessionsBatch(request: ChatSessionsDeleteBatchRequest)
    suspend fun submitReport(request: ReportRequest): ReportResponse
}
