package com.ensodai.avalon.domain.chatEvents.useCases

import com.ensodai.shared.avalon.chat.wss.ServerMessage
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import org.koin.core.annotation.Single

@Single
class ProcessMessageContentAddedUseCase(
    private val localChatHistoryRepository: LocalChatHistoryRepository
) {
    suspend fun add(event: ServerMessage.MessageContentAdded) {
        localChatHistoryRepository.addMessageContent(
            containerId = event.containerId,
            contentId = event.contentId,
            type = event.contentType,
            content = event.content,
            orderIndex = event.orderIndex
        )
    }
}
