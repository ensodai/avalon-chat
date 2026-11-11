package com.ensodai.avalon.domain.chatEvents.useCases

import com.ensodai.shared.avalon.chat.wss.ServerMessage
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import org.koin.core.annotation.Single

@Single
class ProcessActiveContainerChangedUseCase(
    private val localChatHistoryRepository: LocalChatHistoryRepository,
) {

    suspend fun update(event: ServerMessage.ActiveContainerChanged) {

        localChatHistoryRepository.updateActiveMessageFromRPCEvent(event)
    }
}
