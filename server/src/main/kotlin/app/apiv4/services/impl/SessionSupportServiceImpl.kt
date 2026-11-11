package app.apiv4.services.impl

import app.domain.report.usecases.SubmitReportUseCase
import app.domain.session.usecases.DeleteChatSessionsBatchUseCase
import app.domain.session.usecases.UpdateChatSessionsBatchUseCase
import com.ensodai.shared.avalon.rpc.services.SessionSupportRpcService
import com.ensodai.shared.avalon.session.models.ChatSessionsBatchRequest
import com.ensodai.shared.avalon.session.models.ChatSessionsDeleteBatchRequest
import com.ensodai.shared.avalon.session.models.ReportRequest
import com.ensodai.shared.avalon.session.models.ReportResponse
import com.ensodai.shared.avalon.session.models.UpdateChatSessionsResponse
import com.ensodai.shared.avalon.chat.models.BatchUpdateChatsRequest
import com.ensodai.shared.avalon.chat.models.BatchDeleteChatsRequest
import com.ensodai.shared.avalon.chat.models.ChatUpdateItem
import app.apiv4.infrastructure.BaseRpcService
import app.apiv4.infrastructure.RpcSessionContext
import io.ktor.server.application.ApplicationCall
import org.koin.core.component.KoinComponent
import kotlin.uuid.Uuid
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
class SessionSupportServiceImpl(
    @InjectedParam private val call: ApplicationCall,
    @InjectedParam sessionContext: RpcSessionContext,
    private val updateChatSessionsBatchUseCase: UpdateChatSessionsBatchUseCase,
    private val deleteChatSessionsBatchUseCase: DeleteChatSessionsBatchUseCase,
    private val submitReportUseCase: SubmitReportUseCase
) : BaseRpcService(sessionContext), SessionSupportRpcService, KoinComponent {

    private suspend fun getSessionIdInternal(): Uuid? {
        return try { sessionId() } catch (e: Exception) { null }
    }

    override suspend fun updateChatSessionsBatch(request: ChatSessionsBatchRequest): UpdateChatSessionsResponse {
        ensureAuthorized()
        
        val batchRequest = BatchUpdateChatsRequest(
            items = request.items.map { 
                ChatUpdateItem(id = it.id, isArchived = it.isArchived)
            }
        )
        
        val result = updateChatSessionsBatchUseCase.execute(
            batchRequest, 
            userId(), 
            getSessionIdInternal()
        )
        
        return UpdateChatSessionsResponse(updatedResult = result.updatedResult)
    }

    override suspend fun deleteChatSessionsBatch(request: ChatSessionsDeleteBatchRequest) {
        ensureAuthorized()
        
        val batchRequest = BatchDeleteChatsRequest(ids = request.sessionIds)
        
        deleteChatSessionsBatchUseCase.execute(
            batchRequest, 
            userId(), 
            getSessionIdInternal()
        )
    }

    override suspend fun submitReport(request: ReportRequest): ReportResponse {
        ensureAuthorized()
        val resultMessage = submitReportUseCase.execute(request, userId())
        return ReportResponse(message = resultMessage)
    }
}
