package com.ensodai.avalon.domain.chatEvents.useCases

import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import org.koin.core.annotation.Single

import kotlin.time.Instant
import kotlin.uuid.Uuid

@Single
class ProcessMessageAckErrorUseCase(
    private val localChatHistoryRepository: LocalChatHistoryRepository
) {
    suspend fun confirm(
        containerId: Uuid,
        serverTimestamp: Instant,
        slotId: Uuid
    ) {
        localChatHistoryRepository.confirmMessageSentError(
            containerId = containerId,
            serverTimestamp = serverTimestamp,
            slotId = slotId
        )
    }
}
