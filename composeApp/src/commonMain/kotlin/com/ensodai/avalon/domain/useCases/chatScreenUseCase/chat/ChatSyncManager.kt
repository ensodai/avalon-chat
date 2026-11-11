package com.ensodai.avalon.domain.useCases.chatScreenUseCase.chat

import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.shared.avalon.chat.services.ChatRpcService
import com.ensodai.shared.avalon.chat.models.ChatSyncRequest
import com.ensodai.shared.avalon.chat.models.ChatSyncResponse
import com.ensodai.avalon.domain.useCases.commonUseCase.ChatSyncMonitor
import com.ensodai.avalon.utils.AppLogger
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class ChatSyncManager(
    private val chatRpcService: ChatRpcService,
    private val localRepository: LocalChatHistoryRepository,
    private val chatSyncMonitor: ChatSyncMonitor,
    private val logger: AppLogger
) {
    suspend fun syncOnReconnect(chatSessionId: Uuid, variantId: Uuid) {
        logger.d("ChatSyncManager", "Запуск синхронизации для варианта $variantId")
        chatSyncMonitor.setSyncing(true)
        try {
            val since = localRepository.getLastServerUpdateTime(variantId)

            runCatching {
                chatRpcService.syncChatHistory(
                    ChatSyncRequest(
                        chatSessionId = chatSessionId,
                        variantId = variantId,
                        since = since
                    )
                )
            }.onSuccess { response ->
                if (response.isGapTooLarge) {
                    handleHardReset(variantId, response)
                } else {
                    handleSoftSync(variantId, response)
                }
            }.onFailure { error ->
                logger.e("ChatSyncManager", "Ошибка синхронизации: ${error.stackTraceToString()}")
            }
        } finally {
            chatSyncMonitor.setSyncing(false) // ← финиш, даже при ошибке
        }
    }

    private suspend fun handleHardReset(
        variantId: Uuid,
        response: ChatSyncResponse
    ) {
        logger.w("ChatSyncManager", "Hard Reset для варианта $variantId")

        val hasUnsynced = localRepository.variantHasUnsyncedMessages(variantId)

        if (!hasUnsynced) {
            // Атомарно: сносим старое и сохраняем новое в одной транзакции
            localRepository.clearAndSaveChatHistory(variantId, response.upserted)
        } else {
            // Есть оффлайн-сообщения — не сносим, просто мержим свежее поверх
            logger.w("ChatSyncManager", "Есть unsynced сообщения — пропускаем clearHistory")
            localRepository.saveChatHistoryResponse(variantId, response.upserted)
        }
    }

    private suspend fun handleSoftSync(
        variantId: Uuid,
        response: ChatSyncResponse
    ) {
        if (response.deletedSlotIds.isNotEmpty()) {
            localRepository.hardDeleteSlotWithMessagesBySlotIds(response.deletedSlotIds)
        }
        if (response.upserted.isNotEmpty()) {
            localRepository.saveChatHistoryResponse(variantId, response.upserted)
        }
    }
}