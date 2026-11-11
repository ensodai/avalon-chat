@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.ensodai.avalon.data.repository.remote.rpc

import com.ensodai.shared.avalon.chat.models.*
import com.ensodai.shared.avalon.chat.services.ChatRpcService
import com.ensodai.shared.avalon.chat.wss.ClientMessage
import com.ensodai.shared.avalon.chat.wss.RpcStreamEvent
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.avalon.di.RpcConnectionManager
import com.ensodai.avalon.utils.L
import kotlinx.coroutines.flow.*
import kotlinx.rpc.withService
import kotlin.uuid.Uuid

open class ChatRpcServiceRemoteImpl(
    private val manager: RpcConnectionManager,
    private val executor: RpcCallExecutor
) : ChatRpcService {

    open suspend fun getService(): ChatRpcService = manager.getActiveClient().withService<ChatRpcService>()

    override suspend fun getChatHistory(request: GetChatHistoryRequest): PaginatedChatResponse =
        executor.execute("getChatHistory", getService = { getService() }) { getChatHistory(request) }

    override suspend fun syncChatHistory(request: ChatSyncRequest): ChatSyncResponse =
        executor.execute("syncChatHistory", getService = { getService() }) { syncChatHistory(request) }

    override suspend fun createChatVariant(request: CreateChatVariantRequest): ChatVariantResponse =
        executor.execute("createChatVariant", getService = { getService() }) { createChatVariant(request) }

    override suspend fun deleteChatVariant(variantId: Uuid) =
        executor.execute("deleteChatVariant", getService = { getService() }) { deleteChatVariant(variantId) }

    override suspend fun updateChatVariant(request: UpdateChatVariantRequest): ChatVariantResponse =
        executor.execute("updateChatVariant", getService = { getService() }) { updateChatVariant(request) }

    override fun observeChatEvents(): Flow<ServerMessage> = flow {
        emitAll(getService().observeChatEvents())
    }

    override fun observeChatEventsV2(): Flow<RpcStreamEvent<ServerMessage>> = flow {
        L.d(TAG, "--> observeChatEventsV2 (stream)")
        emitAll(
            getService().observeChatEventsV2()
                .onEach { L.d(TAG, "  ~ observeChatEventsV2 emit=$it") }
                .onCompletion { e ->
                    if (e == null) L.d(TAG, "<-- observeChatEventsV2 completed")
                    else {
                        L.e(TAG, "<-- observeChatEventsV2 error: ${e.message}")
                        manager.notifyStreamFailure(e)
                    }
                }
        )
    }

    override fun observeTyping(): Flow<TypingState> = flow {
        emitAll(getService().observeTyping())
    }

    override fun observeTypingV2(): Flow<RpcStreamEvent<TypingState>> = flow {
        L.d(TAG, "--> observeTypingV2 (stream)")
        emitAll(
            getService().observeTypingV2()
                .onEach { L.d(TAG, "  ~ observeTypingV2 emit=$it") }
                .onCompletion { e ->
                    if (e == null) L.d(TAG, "<-- observeTypingV2 completed")
                    else {
                        L.e(TAG, "<-- observeTypingV2 error: ${e.message}")
                        manager.notifyStreamFailure(e)
                    }
                }
        )
    }

    override suspend fun sendMessage(message: ClientMessage.SendChatMessage) =
        executor.execute("sendMessage", getService = { getService() }) { sendMessage(message) }

    override suspend fun editMessage(message: ClientMessage.EditChatMessage) =
        executor.execute("editMessage", getService = { getService() }) { editMessage(message) }

    override suspend fun deleteMessages(message: ClientMessage.DeleteMessages) =
        executor.execute("deleteMessages", getService = { getService() }) { deleteMessages(message) }

    override suspend fun changeActiveContainer(message: ClientMessage.ChangeActiveContainer) =
        executor.execute("changeActiveContainer", getService = { getService() }) { changeActiveContainer(message) }

    override suspend fun toggleReaction(message: ClientMessage.SendUserReaction) =
        executor.execute("toggleReaction", getService = { getService() }) { toggleReaction(message) }

    override suspend fun forceStartGeneration(message: ClientMessage.ForceStartGenerateResponse) =
        executor.execute("forceStartGeneration", getService = { getService() }) { forceStartGeneration(message) }

    override suspend fun stopGeneration(chatSessionId: Uuid) =
        executor.execute("stopGeneration", getService = { getService() }) { stopGeneration(chatSessionId) }

    override suspend fun generateNewContainer(message: ClientMessage.GenerateNewContainer) =
        executor.execute("generateNewContainer", getService = { getService() }) { generateNewContainer(message) }

    override suspend fun markMessagesAsRead(messageContainerIds: List<Uuid>) =
        executor.execute("markMessagesAsRead", getService = { getService() }) { markMessagesAsRead(messageContainerIds) }

    override suspend fun sendOutreachFeedback(characterId: Uuid, isPositive: Boolean) =
        executor.execute("sendOutreachFeedback", getService = { getService() }) { sendOutreachFeedback(characterId, isPositive) }

    override fun requestSpeech(messageContentId: Uuid, voice: VoiceModel?): Flow<ByteArray> = flow {
        L.d(TAG, "--> requestSpeech (stream)")
        emitAll(
            getService().requestSpeech(messageContentId, voice)
                .onCompletion { e ->
                    if (e == null) L.d(TAG, "<-- requestSpeech completed")
                    else {
                        L.e(TAG, "<-- requestSpeech error: ${e.message}")
                        manager.notifyStreamFailure(e)
                    }
                }
        )
    }

    companion object {
        private const val TAG = "ChatRpc"
    }
}
