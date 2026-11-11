package com.ensodai.avalon.domain.chatEvents.useCases

import com.ensodai.shared.avalon.chat.wss.ServerMessage
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import org.koin.core.annotation.Single

@Single
class ProcessMessagesDeletedUseCase(
    private val localChatHistoryRepository: LocalChatHistoryRepository,
) {

    suspend fun deleteMessages(event: ServerMessage.MessagesDeleted) {
        localChatHistoryRepository.rpcHardDeleteSlotWithMessagesBySlotIds(event)
    }
}
