package com.ensodai.avalon.domain.chatEvents.useCases

import com.ensodai.shared.avalon.chat.wss.ServerMessage
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.domain.chatEvents.MessageStreamManager
import org.koin.core.annotation.Single

@Single
class ProcessMessageUpdatedUseCase(
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val messageStreamManager: MessageStreamManager,
) {
    suspend fun update(event: ServerMessage.MessageUpdated) {
        localChatHistoryRepository.processMessageUpdateEvent(event)
        messageStreamManager.markStreamFinished(event.containerId)
    }
}
