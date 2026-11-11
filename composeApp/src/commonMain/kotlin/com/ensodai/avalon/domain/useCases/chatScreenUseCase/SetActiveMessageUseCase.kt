package com.ensodai.avalon.domain.useCases.chatScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.domain.chatEvents.LocalChatAction
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.getUTCTimeNow
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class SetActiveMessageUseCase(
    private val localChatSessionsRepository: LocalChatSessionsRepository,
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val chatSyncRpcClient: ChatSyncRpcClient,
    private val logger: AppLogger,
) {

    suspend fun set(
        characterId: Uuid?,
        messageSlot: Uuid,
        newActiveMessageId: Uuid,
    ) {
        logger.d(
            "SetActiveMessageUseCase",
            "characterId $characterId messageSlot $messageSlot newActiveMessageId $newActiveMessageId"
        )
        if (characterId == null) return
        val session = localChatSessionsRepository.getSessionByCharacterId(characterId) ?: return
        logger.d("SetActiveMessageUseCase", "sessionId ${session.id}")
        
        val variantId = session.activeChatVariantId ?: return
        
        logger.d(
            "SetActiveMessageUseCase",
            "messageSlot $messageSlot newActiveMessageId: $newActiveMessageId"
        )

        localChatHistoryRepository.updateActiveMessageInSlot(
            messageSlotId = messageSlot,
            newActiveContainerId = newActiveMessageId,
            updatedAt = getUTCTimeNow()
        )
        
        val action = LocalChatAction.ChangeActiveContainer(
            chatVariantId = variantId,
            chatSessionId = session.id,
            slotId = messageSlot,
            newActiveContainerId = newActiveMessageId
        )
        chatSyncRpcClient.sendAction(action)
    }
}
