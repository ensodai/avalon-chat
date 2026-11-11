@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package com.ensodai.shared.avalon.rpc.services

import com.ensodai.shared.avalon.sync.models.SyncChangesRequest
import com.ensodai.shared.avalon.sync.models.SyncResponse
import kotlinx.rpc.annotations.Rpc

@Rpc
interface SyncRpcService {
    suspend fun syncChanges(request: SyncChangesRequest): SyncResponse
}
