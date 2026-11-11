package com.ensodai.avalon.domain.chatEvents.useCases

import com.ensodai.shared.avalon.chat.wss.ServerMessage
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import org.koin.core.annotation.Single

@Single
class ProcessMessageAckUseCase(
    private val localChatHistoryRepository: LocalChatHistoryRepository
) {
    suspend fun confirm(ack: ServerMessage.MessageAck) {
        localChatHistoryRepository.confirmMessageSent(
            containerId = ack.containerId,
            serverTimestamp = ack.timestamp,
            slotId = ack.slotId
        )
    }
}
