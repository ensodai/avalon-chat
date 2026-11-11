package com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class ActivateChatVariantUseCase(
    private val localCharacterRepository: LocalCharacterRepository,
    private val localUserRepository: LocalUserRepository,
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val localChatSessionsRepository: LocalChatSessionsRepository
) {

    suspend fun activate(
        characterId: Uuid,
        chatVariantId: Uuid
    ) {
        val chatSession = localChatSessionsRepository.getSessionByCharacterId(characterId) ?: return

        localChatSessionsRepository.setChatVariantAsActive(
            characterId = characterId,
            chatSessionId = chatSession.id,
            chatVariantId = chatVariantId
        )
    }
}
