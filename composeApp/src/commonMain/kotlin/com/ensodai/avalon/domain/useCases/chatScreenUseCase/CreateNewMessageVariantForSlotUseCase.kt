package com.ensodai.avalon.domain.useCases.chatScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.domain.chatEvents.LocalChatAction
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import com.ensodai.avalon.utils.AppLogger
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class CreateNewMessageVariantForSlotUseCase(
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val chatSyncRpcClient: ChatSyncRpcClient,
    private val localChatSessionsRepository: LocalChatSessionsRepository,
    private val logger: AppLogger,
) {
    suspend fun create(
        characterId: Uuid?,
        slotId: Uuid,
        llmModelCode: String?
    ) {
        logger.d("CreateNewMessageVariant", "characterId $characterId slotId $slotId")
        if (characterId == null) return
        val session = localChatSessionsRepository.getSessionByCharacterId(characterId) ?: return
        logger.d("CreateNewMessageVariant", "sessionId ${session.id}")
        
        val variantId = session.activeChatVariantId ?: return
        
        logger.d("CreateNewMessageVariant", "variantId $variantId")
        val action = LocalChatAction.GenerateNewContainer(
            slotId = slotId,
            chatVariantId = variantId,
            chatSessionId = session.id,
            characterId = characterId,
            llmModelCode = llmModelCode
        )
        logger.d("CreateNewMessageVariant", "Action created: ${action::class.simpleName}")
        chatSyncRpcClient.sendAction(action)
    }
}
