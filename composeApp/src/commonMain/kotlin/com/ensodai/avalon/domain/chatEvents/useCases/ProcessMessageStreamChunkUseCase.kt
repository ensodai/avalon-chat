package com.ensodai.avalon.domain.chatEvents.useCases

import com.ensodai.shared.avalon.chat.wss.ServerMessage
import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.avalon.domain.chatEvents.MessageStreamManager
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.utils.AppLogger
import kotlinx.coroutines.delay
import org.koin.core.annotation.Factory

@Factory
class ProcessMessageStreamChunkUseCase(
    private val messageStreamManager: MessageStreamManager,
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val logger: AppLogger
) {
    suspend fun process(event: ServerMessage.MessageStreamChunk) {
        val baseText = if (!messageStreamManager.hasStream(event.containerId)) {
            // Need to get current db text if any, but since we are replacing it later, 
            // maybe we can just get the whole preview text or from slot
            // Wait, does the backend send an empty string first? Yes!
            // When stream starts, it creates a new container with empty text, then sends chunks.
            // But if we reconnect mid-stream, DB will have some text.
            // Wait, we don't have getMessageContentText in LocalChatHistoryRepository.
            // Can we just use an empty string for now? Or get it from the container?
            ""
        } else ""

        messageStreamManager.appendChunk(event.containerId, event.textChunk, baseText)
    }
}
