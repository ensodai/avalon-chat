package com.ensodai.avalon.domain.useCases.chatScreenUseCase

import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.domain.chatEvents.LocalChatAction
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class EditMessageUseCase(
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val chatSyncRpcClient: ChatSyncRpcClient,
    private val localChatSessionsRepository: LocalChatSessionsRepository
) {

    suspend fun update(
        messageSlotId: Uuid,
        messageContainerId: Uuid,
        messageText: String,
        characterId: Uuid,
        systemInstruction: String?
    ) {
        val session = localChatSessionsRepository.getSessionByCharacterId(characterId)
        val activeVariantId = session?.activeChatVariantId

        if (session != null && activeVariantId != null) {
            localChatHistoryRepository.updateMessageTextContentByContainerId(
                messageContainerId = messageContainerId,
                newMessage = messageText,
                syncAction = SyncAction.UPDATE,
                updatedAt = null
            )
            if (systemInstruction != null) {
                localChatHistoryRepository.updateMessageSystemInstructionContentByContainerId(
                    messageContainerId = messageContainerId,
                    newSystemInstruction = systemInstruction,
                    syncAction = SyncAction.UPDATE,
                    updatedAt = null
                )
            }
            val action = LocalChatAction.EditChatMessage(
                containerId = messageContainerId,
                slotId = messageSlotId,
                chatVariantId = activeVariantId,
                chatSessionId = session.id,
                text = messageText,
                messageSystemInstruction = systemInstruction
            )
            chatSyncRpcClient.sendAction(action)
        }
    }
}
