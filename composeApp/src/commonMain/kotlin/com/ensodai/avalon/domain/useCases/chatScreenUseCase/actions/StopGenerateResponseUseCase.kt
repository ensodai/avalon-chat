package com.ensodai.avalon.domain.useCases.chatScreenUseCase.actions

import com.ensodai.shared.avalon.chat.services.ChatRpcService
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class StopGenerateResponseUseCase(
    private val chatRpcService: ChatRpcService,
    private val localChatSessionsRepository: LocalChatSessionsRepository
) {

    suspend fun stop(characterId: Uuid) {
        val session = localChatSessionsRepository.getSessionByCharacterId(characterId)

        if (session != null) {
            chatRpcService.stopGeneration(session.id)
        }
    }
}
