package com.ensodai.avalon.domain.useCases.chatScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.domain.chatEvents.LocalChatAction
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class DeleteMessagesUseCase(
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val localChatSessionsRepository: LocalChatSessionsRepository,
    private val chatSyncRpcClient: ChatSyncRpcClient,
) {

    suspend fun deleteMessages(slotIds: List<Uuid>, characterId: Uuid) {
        val session = localChatSessionsRepository.getSessionByCharacterId(characterId)
        val activeVariantId = session?.activeChatVariantId

        if (session != null && activeVariantId != null) {
            localChatHistoryRepository.softDeleteSlotWithMessagesBySlotIds(slotIds)

            val action = LocalChatAction.DeleteMessages(
                slotIds = slotIds,
                chatVariantId = activeVariantId,
                chatSessionId = session.id,
            )

            chatSyncRpcClient.sendAction(action)
        }
    }
}
