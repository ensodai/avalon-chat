package com.ensodai.avalon.domain.chatEvents.useCases

import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import org.koin.core.annotation.Single

import kotlin.uuid.Uuid

@Single
class ProcessMessageReactionUseCase(
    private val localChatHistoryRepository: LocalChatHistoryRepository,
) {

    suspend fun setUserReaction(
        containerId: Uuid,
        reaction: String?
    ) {
        localChatHistoryRepository.updateMessageUserReaction(
            messageContainerId = containerId,
            reaction = reaction
        )
    }

    suspend fun setCharacterReaction(
        containerId: Uuid,
        reaction: String?
    ) {
        localChatHistoryRepository.updateMessageCharacterReaction(
            messageContainerId = containerId,
            reaction = reaction
        )
    }


}
