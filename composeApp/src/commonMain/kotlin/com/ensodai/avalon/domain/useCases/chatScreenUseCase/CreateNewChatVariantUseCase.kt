package com.ensodai.avalon.domain.useCases.chatScreenUseCase


import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.utils.getUTCTimeNow
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid
@Single
class CreateNewChatVariantUseCase(
    private val localChatSessionsRepository: LocalChatSessionsRepository
) {

    suspend fun create(characterId: Uuid?) {
        if (characterId == null) return
        val session = localChatSessionsRepository.getSessionByCharacterId(characterId) ?: return

        val createdAt = getUTCTimeNow()
        val newVariantId = Uuid.random()

        localChatSessionsRepository.createLocalChatVariantAndSetAsActive(
            sessionId = session.id,
            createdAt = createdAt,
            newVariantId = newVariantId
        )
    }
}
