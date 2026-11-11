package com.ensodai.avalon.domain.chatEvents

import com.ensodai.shared.avalon.chat.models.*
import com.ensodai.shared.avalon.chat.wss.*
import com.ensodai.shared.avalon.chat.services.ChatRpcService
import com.ensodai.avalon.di.ApplicationScope
import com.ensodai.avalon.di.RpcConnectionManager
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.data.db.models.SyncAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds
import com.ensodai.avalon.domain.messageSender.TypingStatusManager
import com.ensodai.avalon.domain.auth.AuthSessionManager
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.CancellationException

/**
 * Клиент для синхронизации чата через Kotlin RPC.
 * Использует локальную очередь действий [MessageQueueManager] для обеспечения
 * надежности и оффлайн-работы.
 */
@Single
class ChatSyncRpcClient(
    private val chatRpcService: ChatRpcService,
    private val rpcConnectionManager: RpcConnectionManager,
    private val typingStatusManager: TypingStatusManager,
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val authSessionManager: AuthSessionManager,
    @ApplicationScope private val scope: CoroutineScope,
    private val logger: AppLogger
) {
    private val messageQueueManager = MessageQueueManager(
        messageQueueOptimizer = MessageQueueOptimizer(),
        scope = scope,
        sendAction = { action ->
            logger.i("ChatSyncRpcClient", "Queue -> RPC: ${action.optimizationKey}")
            dispatchAction(action)
        }
    )

    private val _incomingMessages = MutableSharedFlow<ServerMessage>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val incomingMessages: SharedFlow<ServerMessage> = _incomingMessages.asSharedFlow()

    private val _connectionState = MutableStateFlow<RpcConnectionState>(RpcConnectionState.Idle)
    val connectionState: StateFlow<RpcConnectionState> = _connectionState.asStateFlow()

    private val _isChannelReady = MutableStateFlow(false)
    val isChannelReady: StateFlow<Boolean> = _isChannelReady.asStateFlow()

    init {
        scope.launch {
            combine(
                isChannelReady,
                authSessionManager.isSessionAuthorized
            ) { ready, authorized ->
                ready && authorized
            }.collectLatest { isAvailable ->
                logger.i("ChatSyncRpcClient", "Setting message queue network availability: $isAvailable")
                messageQueueManager.setNetworkAvailable(isAvailable)
            }
        }
        startObservation()
    }

    private fun startObservation() {
        scope.launch {
            rpcConnectionManager.connectionState.collectLatest { state ->
                when (state) {
                    is RpcConnectionState.Connected -> {
                        logger.i("ChatSyncRpcClient", "RPC Connected -> launching event & typing observation streams...")
                        _connectionState.value = RpcConnectionState.Connecting

                        // 1. Поток событий чата (входящие сообщения)
                        launch {
                            try {
                                chatRpcService.observeChatEventsV2()
                                    .catch { e ->
                                        if (e is CancellationException) throw e
                                        logger.w("ChatSyncRpcClient", "Chat observation error: ${e.message}")
                                        rpcConnectionManager.notifyStreamFailure(e)
                                    }
                                    .collect { event ->
                                        if (event.isReadySignal) {
                                            logger.i("ChatSyncRpcClient", "RPC Event stream handshake received.")
                                            _connectionState.value = RpcConnectionState.Connected
                                            _isChannelReady.value = true
                                        } else if (event.payload != null) {
                                            _incomingMessages.emit(event.payload!!)
                                        }
                                    }
                            } catch (e: Exception) {
                                if (e is CancellationException) throw e
                                logger.e("ChatSyncRpcClient", "Chat observation loop failed: ${e.message}")
                                rpcConnectionManager.notifyStreamFailure(e)
                            }
                        }

                        // 2. Поток статуса печати персонажа
                        launch {
                            try {
                                chatRpcService.observeTypingV2()
                                    .catch { e ->
                                        if (e is CancellationException) throw e
                                        logger.w("ChatSyncRpcClient", "Typing observation error: ${e.message}")
                                        rpcConnectionManager.notifyStreamFailure(e)
                                    }
                                    .collect { wrapper ->
                                        if (wrapper.payload != null) {
                                            val event = wrapper.payload!!
                                            if (event.isTyping) {
                                                typingStatusManager.onActivityDetected(
                                                    chatId = event.chatId,
                                                    rarity = event.rarity
                                                )
                                            } else {
                                                typingStatusManager.clearTyping(event.chatId)
                                            }
                                        }
                                    }
                            } catch (e: Exception) {
                                if (e is CancellationException) throw e
                                logger.e("ChatSyncRpcClient", "Typing observation loop failed: ${e.message}")
                                rpcConnectionManager.notifyStreamFailure(e)
                            }
                        }
                    }
                    is RpcConnectionState.Connecting,
                    is RpcConnectionState.Authenticating -> {
                        _isChannelReady.value = false
                        _connectionState.value = RpcConnectionState.Connecting
                    }
                    is RpcConnectionState.Idle -> {
                        _isChannelReady.value = false
                        _connectionState.value = RpcConnectionState.Idle
                    }
                    is RpcConnectionState.Error -> {
                        _isChannelReady.value = false
                        _connectionState.value = RpcConnectionState.Error(state.message)
                    }
                }
            }
        }
    }

    private suspend fun dispatchAction(action: LocalChatAction) {
        runCatching {
            when (action) {
                is LocalChatAction.SendChatMessage -> chatRpcService.sendMessage(
                    ClientMessage.SendChatMessage(
                        slotId = action.slotId,
                        contentId = action.contentId,
                        containerId = action.containerId,
                        chatVariantId = action.chatVariantId,
                        chatSessionId = action.chatSessionId,
                        contentType = action.contentType,
                        text = action.text,
                        characterId = action.characterId,
                        llmModelCode = action.llmModelCode,
                        messageSystemInstruction = action.messageSystemInstruction
                    )
                )
                is LocalChatAction.EditChatMessage -> {
                    chatRpcService.editMessage(
                        ClientMessage.EditChatMessage(
                            slotId = action.slotId,
                            containerId = action.containerId,
                            chatVariantId = action.chatVariantId,
                            chatSessionId = action.chatSessionId,
                            text = action.text,
                            messageSystemInstruction = action.messageSystemInstruction
                        )
                    )
                    localChatHistoryRepository.updateMessageTextContentByContainerId(
                        messageContainerId = action.containerId,
                        newMessage = action.text,
                        syncAction = SyncAction.NONE,
                        updatedAt = null
                    )
                    if (action.messageSystemInstruction != null) {
                        localChatHistoryRepository.updateMessageSystemInstructionContentByContainerId(
                            messageContainerId = action.containerId,
                            newSystemInstruction = action.messageSystemInstruction,
                            syncAction = SyncAction.NONE,
                            updatedAt = null
                        )
                    }
                }
                is LocalChatAction.DeleteMessages -> {
                    chatRpcService.deleteMessages(
                        ClientMessage.DeleteMessages(
                            slotIds = action.slotIds,
                            chatVariantId = action.chatVariantId,
                            chatSessionId = action.chatSessionId
                        )
                    )
                    localChatHistoryRepository.hardDeleteSlotWithMessagesBySlotIds(
                        slotIds = action.slotIds
                    )
                }
                is LocalChatAction.ChangeActiveContainer -> {
                    chatRpcService.changeActiveContainer(
                        ClientMessage.ChangeActiveContainer(
                            chatVariantId = action.chatVariantId,
                            chatSessionId = action.chatSessionId,
                            slotId = action.slotId,
                            newActiveContainerId = action.newActiveContainerId
                        )
                    )
                    localChatHistoryRepository.updateSlotsSyncAction(
                        ids = listOf(action.slotId),
                        action = SyncAction.NONE
                    )
                }
                is LocalChatAction.SendUserReaction -> chatRpcService.toggleReaction(
                    ClientMessage.SendUserReaction(
                        reaction = action.reaction,
                        characterId = action.characterId,
                        chatVariantId = action.chatVariantId,
                        chatSessionId = action.chatSessionId,
                        slotId = action.slotId,
                        containerId = action.containerId
                    )
                )
                is LocalChatAction.ForceStartGenerateResponse -> chatRpcService.forceStartGeneration(
                    ClientMessage.ForceStartGenerateResponse(
                        chatSessionId = action.chatSessionId,
                        chatVariantId = action.chatVariantId,
                        characterId = action.characterId,
                        llmModelCode = action.llmModelCode
                    )
                )
                is LocalChatAction.GenerateNewContainer -> chatRpcService.generateNewContainer(
                    ClientMessage.GenerateNewContainer(
                        chatSessionId = action.chatSessionId,
                        chatVariantId = action.chatVariantId,
                        characterId = action.characterId,
                        slotId = action.slotId,
                        llmModelCode = action.llmModelCode
                    )
                )
                is LocalChatAction.StopGenerateResponse -> chatRpcService.stopGeneration(action.chatSessionId)
            }
        }.onFailure { e ->
            logger.e("ChatSyncRpcClient", "Failed to dispatch RPC action: ${e.message}")
            throw e
        }
    }


    fun sendAction(action: LocalChatAction) {
        logger.d("ChatSyncRpcClient", "Enqueue Action: ${action::class.simpleName}")
        scope.launch {
            messageQueueManager.enqueueAction(action)
        }
    }

    suspend fun enqueueActionDirectly(action: LocalChatAction) {
        messageQueueManager.enqueueAction(action)
    }


    fun cleanup() {
        scope.launch {
            messageQueueManager.stop()
        }
        _isChannelReady.value = false
        _connectionState.value = RpcConnectionState.Idle
    }

    suspend fun cancelMessagesForDeletedContexts(
        deletedSessionIds: List<Uuid>,
        deletedVariantIds: List<Uuid>
    ) {
        messageQueueManager.purgeMessagesForDeletedContexts(deletedSessionIds, deletedVariantIds)
    }
}

private fun ClientMessage.toLocalAction(): LocalChatAction = when (this) {
    is ClientMessage.SendChatMessage -> LocalChatAction.SendChatMessage(
        slotId = slotId,
        contentId = contentId,
        containerId = containerId,
        chatVariantId = chatVariantId,
        chatSessionId = chatSessionId,
        contentType = contentType,
        text = text,
        characterId = characterId,
        llmModelCode = llmModelCode,
        messageSystemInstruction = messageSystemInstruction
    )
    is ClientMessage.EditChatMessage -> LocalChatAction.EditChatMessage(
        slotId, containerId, chatVariantId, chatSessionId, text, messageSystemInstruction
    )
    is ClientMessage.DeleteMessages -> LocalChatAction.DeleteMessages(
        slotIds, chatVariantId, chatSessionId
    )
    is ClientMessage.ChangeActiveContainer -> LocalChatAction.ChangeActiveContainer(
        chatVariantId, chatSessionId, slotId, newActiveContainerId
    )
    is ClientMessage.SendUserReaction -> LocalChatAction.SendUserReaction(
        reaction, characterId, chatVariantId, chatSessionId, slotId, containerId
    )
    is ClientMessage.ForceStartGenerateResponse -> LocalChatAction.ForceStartGenerateResponse(
        chatSessionId, chatVariantId, characterId, llmModelCode
    )
    is ClientMessage.GenerateNewContainer -> LocalChatAction.GenerateNewContainer(
        chatSessionId, chatVariantId, characterId, slotId, llmModelCode
    )
    is ClientMessage.StopGenerateResponse -> LocalChatAction.StopGenerateResponse(
        chatSessionId, chatVariantId
    )
    is ClientMessage.Authenticate -> error("Authenticate client message cannot be converted to LocalChatAction")
}
