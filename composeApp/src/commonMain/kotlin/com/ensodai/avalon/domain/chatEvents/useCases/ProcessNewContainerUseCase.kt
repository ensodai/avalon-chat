package com.ensodai.avalon.domain.chatEvents.useCases

import com.ensodai.shared.avalon.chat.models.MessageContainerResponse
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import org.koin.core.annotation.Single

import kotlin.uuid.Uuid

@Single
class ProcessNewContainerUseCase(
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val localUserRepository: LocalUserRepository
) {

    suspend fun add(
        container: MessageContainerResponse,
        sessionId: Uuid,
        newQuota: Int
    ) {
        localUserRepository.updateUserQuota(newQuota)
        localChatHistoryRepository.addNewContainerForSlot(
            container = container,
            sessionId = sessionId
        )
    }

}
