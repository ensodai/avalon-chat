package app.apiv4.services.impl

import app.domain.syncmanager.SyncDataRequest
import app.domain.syncmanager.SyncManager
import com.ensodai.shared.avalon.rpc.services.SyncRpcService
import com.ensodai.shared.avalon.sync.models.SyncChangesRequest
import com.ensodai.shared.avalon.sync.models.SyncResponse
import app.apiv4.infrastructure.BaseRpcService
import app.apiv4.infrastructure.RpcSessionContext
import org.koin.core.component.KoinComponent
import kotlin.time.Instant
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
class SyncServiceImpl(
    @InjectedParam sessionContext: RpcSessionContext,
    private val syncManager: SyncManager
) : BaseRpcService(sessionContext), SyncRpcService, KoinComponent {

    override suspend fun syncChanges(request: SyncChangesRequest): SyncResponse {
        ensureAuthorized()
        
        val sinceTimestamp = request.since ?: Instant.fromEpochSeconds(0)

        val syncRequest = SyncDataRequest(userId(), sinceTimestamp)
        return syncManager.execute(syncRequest)
    }
}
