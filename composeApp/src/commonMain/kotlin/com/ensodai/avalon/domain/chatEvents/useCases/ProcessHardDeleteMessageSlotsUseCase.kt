package com.ensodai.avalon.domain.chatEvents.useCases

import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import org.koin.core.annotation.Single

import kotlin.uuid.Uuid

@Single
class ProcessHardDeleteMessageSlotsUseCase(private val localChatHistoryRepository: LocalChatHistoryRepository) {

    suspend fun delete(slotIds: List<Uuid>) {
        localChatHistoryRepository.hardDeleteSlotWithMessagesBySlotIds(slotIds)
    }

}
