package com.ensodai.avalon.domain.useCases.chatScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class UpdateChatDraftUseCase(
    private val localChatSessionsRepository: LocalChatSessionsRepository
) {
    suspend fun execute(characterId: Uuid, draftMessage: String?) {
        val sessionId = localChatSessionsRepository.getSessionByCharacterId(characterId)?.id
            ?: return
        
        val validatedDraft = draftMessage?.trim()?.takeIf { it.isNotBlank() }
        
        localChatSessionsRepository.updateDraftMessage(sessionId, validatedDraft)
    }
}
