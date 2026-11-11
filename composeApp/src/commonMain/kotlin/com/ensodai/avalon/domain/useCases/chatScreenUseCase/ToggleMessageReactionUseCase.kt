package com.ensodai.avalon.domain.useCases.chatScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.domain.chatEvents.LocalChatAction
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class ToggleMessageReactionUseCase(
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val chatSyncRpcClient: ChatSyncRpcClient,
    private val localChatSessionsRepository: LocalChatSessionsRepository
) {

    suspend fun toggle(
        slotId: Uuid,
        messageContainerId: Uuid,
        reaction: String?,
        characterId: Uuid,
    ) {
        val session = localChatSessionsRepository.getSessionByCharacterId(characterId)
        val activeVariantId = session?.activeChatVariantId

        if (session != null && activeVariantId != null) {
            val finalReaction = localChatHistoryRepository.toggleMessageUserReaction(
                messageContainerId = messageContainerId,
                reaction = reaction
            )
            val action = LocalChatAction.SendUserReaction(
                reaction = finalReaction,
                characterId = characterId,
                chatVariantId = activeVariantId,
                chatSessionId = session.id,
                containerId = messageContainerId,
                slotId = slotId
            )
            chatSyncRpcClient.sendAction(action)
        }
    }
}
