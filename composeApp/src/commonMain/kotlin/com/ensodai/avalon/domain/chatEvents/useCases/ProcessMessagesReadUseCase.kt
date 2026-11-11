package com.ensodai.avalon.domain.chatEvents.useCases

import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class ProcessMessagesReadUseCase(
    private val localChatHistoryRepository: LocalChatHistoryRepository
) {
    suspend fun markAsRead(containerIds: List<Uuid>) {
        localChatHistoryRepository.markMessagesAsReadLocally(containerIds)
    }
}
