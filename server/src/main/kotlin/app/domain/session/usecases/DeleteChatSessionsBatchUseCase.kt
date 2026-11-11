package app.domain.session.usecases

import org.koin.core.annotation.Factory
import com.ensodai.shared.avalon.chat.models.BatchDeleteChatsRequest
import app.data.repository.UserRepositoryApp
import app.service.chat.MessageNotifier
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import kotlin.uuid.Uuid

@Factory
class DeleteChatSessionsBatchUseCase(
    private val userRepository: UserRepositoryApp,
    private val messageNotifier: MessageNotifier
) {
    suspend fun execute(request: BatchDeleteChatsRequest, userId: Uuid, sessionId: Uuid?) {
        val uuids = request.ids
        if (uuids.isNotEmpty()) {
            userRepository.softDeleteChatSessionsBatch(userId, uuids)
        }
        
        messageNotifier.notifyUserWithMessage(
            userId = userId,
            message = ServerMessage.SyncTrigger(entity = "chats"),
            excludeSessionId = sessionId
        )
    }
}
