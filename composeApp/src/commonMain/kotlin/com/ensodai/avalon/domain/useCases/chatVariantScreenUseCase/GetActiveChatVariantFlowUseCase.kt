package com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GetActiveChatVariantFlowUseCase(
    private val localChatSessionsRepository: LocalChatSessionsRepository
) {

    suspend fun get(characterID: Uuid): Flow<Uuid?> {

        val sessionId =
            localChatSessionsRepository.getSessionByCharacterId(characterID) ?: return emptyFlow()

        return localChatSessionsRepository.getActiveVariantIdBySessionIdFlow(sessionId.id)
    }
}
