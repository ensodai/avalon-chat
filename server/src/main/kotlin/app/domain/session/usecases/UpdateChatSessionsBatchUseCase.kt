package app.domain.session.usecases

import org.koin.core.annotation.Factory
import com.ensodai.shared.avalon.chat.models.UpdateChatSessionsResponse
import com.ensodai.shared.avalon.chat.models.BatchUpdateChatsRequest
import app.data.repository.UserRepositoryApp
import app.service.chat.MessageNotifier
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import kotlin.uuid.Uuid

@Factory
class UpdateChatSessionsBatchUseCase(
    private val userRepository: UserRepositoryApp,
    private val messageNotifier: MessageNotifier
) {
    suspend fun execute(request: BatchUpdateChatsRequest, userId: Uuid, sessionId: Uuid?): UpdateChatSessionsResponse {
        val updatedResult = userRepository.updateChatSessionsBatch(userId, request.items)
        
        messageNotifier.notifyUserWithMessage(
            userId = userId,
            message = ServerMessage.SyncTrigger(entity = "chats"),
            excludeSessionId = sessionId
        )

        return UpdateChatSessionsResponse(updatedResult)
    }
}
