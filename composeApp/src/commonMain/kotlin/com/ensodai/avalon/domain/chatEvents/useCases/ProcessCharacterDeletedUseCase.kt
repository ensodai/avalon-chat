package com.ensodai.avalon.domain.chatEvents.useCases

import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import kotlinx.serialization.Contextual
import org.koin.core.annotation.Single

import kotlin.uuid.Uuid

@Single
class ProcessCharacterDeletedUseCase(
    private val localCharacterRepository: LocalCharacterRepository,
    private val chatSyncRpcClient: ChatSyncRpcClient,
    private val localChatSessionsRepository: LocalChatSessionsRepository
) {

    suspend fun delete(characterId: @Contextual Uuid) {

        val sessionId = localChatSessionsRepository.getSessionByCharacterId(characterId)
        val variantsIds = localChatSessionsRepository.getVariantsByCharacterId(characterId)


        localCharacterRepository.hardDeleteCharacters(listOf(characterId))

        if (sessionId != null) {
            localChatSessionsRepository.hardDeleteSessions(listOf(sessionId.id))
            chatSyncRpcClient.cancelMessagesForDeletedContexts(
                deletedSessionIds = listOf(sessionId.id),
                deletedVariantIds = variantsIds.map { it.id }
            )
        }
    }
}
