package com.ensodai.avalon.domain.useCases.chatScreenUseCase.chat

import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.services.ChatRpcService
import com.ensodai.shared.avalon.chat.services.MessageContentCodec
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import com.ensodai.avalon.domain.chatEvents.RpcConnectionState
import com.ensodai.avalon.domain.chatEvents.MessageStreamManager
import com.ensodai.avalon.domain.models.message.ChatHeaderInfo
import com.ensodai.avalon.domain.models.message.ChatItem
import com.ensodai.avalon.domain.models.message.MessageItem
import com.ensodai.avalon.domain.models.message.WelcomeCardItem
import com.ensodai.avalon.domain.models.message.toChatMessageModel
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class GetChatStreamUseCase(
    private val localChatSessionsRepository: LocalChatSessionsRepository,
    private val localCharacterRepository: LocalCharacterRepository,
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val chatSyncManager: ChatSyncManager,
    private val chatSyncRpcClient: ChatSyncRpcClient,
    private val chatRpcService: ChatRpcService,
    private val messageStreamManager: MessageStreamManager,
    private val logger: AppLogger
) {
    private val useCaseScope = CoroutineScope(IODispatcher + SupervisorJob())
    private var currentPaginator: ChatPaginator? = null
    private var reconnectJob: Job? = null

    val isLoadingMore: StateFlow<Boolean>
        get() = currentPaginator?.isLoadingMore ?: emptyLoadingState

    suspend fun loadMore() {
        logger.d("GetChatStreamUseCase", "loadMore:")
        currentPaginator?.loadMore()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun get(characterId: Uuid): Flow<ChatStreamResult> {
        val headerInfoFlow = localCharacterRepository
            .getChatDetailsFlow(characterId)
            .distinctUntilChanged()

        return combine(
            localChatSessionsRepository.getActiveChatVariantFlow(characterId)
                .distinctUntilChanged(),
            localChatSessionsRepository.getSessionByCharacterIdFlow(characterId)
                .distinctUntilChanged()
        ) { variantId, sessionId -> variantId to sessionId }
            .distinctUntilChanged()
            .map { (variantId, sessionId) ->
                logger.d("GetChatStreamUseCase", "variantId $variantId sessionId $sessionId")
                currentPaginator?.cancel()
                currentPaginator = null
                reconnectJob?.cancel()
                reconnectJob = null

                if (variantId == null || sessionId == null) {
                    return@map buildEmptyResult(headerInfoFlow)
                }

                val paginator = ChatPaginator(
                    variantId = variantId,
                    chatSessionId = sessionId,
                    chatRpcService = chatRpcService,
                    localChatHistoryRepository = localChatHistoryRepository,
                    logger = logger
                )
                currentPaginator = paginator
                observeReconnect(paginator)
                paginator.initialize()

                val uiItemsFlow = combine(
                    paginator.messagesFlow,
                    headerInfoFlow,
                    messageStreamManager.activeStreams
                ) { slots, headerInfo, activeStreams ->
                    buildList {
                        val mappedSlots = slots.map { slotEntity ->
                            var slotModel = slotEntity.toChatMessageModel()
                            
                            if (activeStreams.isNotEmpty()) {
                                slotModel = slotModel.copy(
                                    containers = slotModel.containers.map { container ->
                                        val streamState = activeStreams[container.id]
                                        if (streamState != null) {
                                            val dbText = (container.content.firstOrNull { it.type == com.ensodai.shared.avalon.chat.models.AvalonMessageContentType.TEXT }?.payload as? com.ensodai.shared.avalon.chat.models.TextPayload)?.text.orEmpty()
                                            
                                            if (streamState.isFinished && dbText.length >= streamState.text.length) {
                                                useCaseScope.launch {
                                                    messageStreamManager.finishStream(container.id)
                                                }
                                                container.copy(isStreaming = false)
                                            } else {
                                                container.copy(
                                                    isStreaming = !streamState.isFinished,
                                                    content = container.content.map { contentItem ->
                                                        if (contentItem.type == com.ensodai.shared.avalon.chat.models.AvalonMessageContentType.TEXT) {
                                                            contentItem.copy(payload = com.ensodai.shared.avalon.chat.models.TextPayload(text = streamState.text))
                                                        } else contentItem
                                                    }
                                                )
                                            }
                                        } else {
                                            container.copy(isStreaming = false)
                                        }
                                    }
                                )
                            }
                            
                            MessageItem(slotModel)
                        }
                        addAll(mappedSlots)
                        addHeaderItems(headerInfo)
                    }
                }.flowOn(IODispatcher)

                ChatStreamResult(uiItemsFlow = uiItemsFlow)
            }
            .onCompletion {
                currentPaginator?.cancel()
                reconnectJob?.cancel()
                useCaseScope.cancel()
            }
    }

    private fun observeReconnect(paginator: ChatPaginator) {
        reconnectJob?.cancel()
        reconnectJob = useCaseScope.launch {
            var wasConnected = true // Игнорируем стартовое состояние, так как оно уже синхронизируется при старте
            chatSyncRpcClient.connectionState.collect { state ->
                val isConnected = state is RpcConnectionState.Connected
                if (isConnected && !wasConnected) {
                    chatSyncManager.syncOnReconnect(
                        chatSessionId = paginator.chatSessionId,
                        variantId = paginator.variantId,
                    )
                }
                wasConnected = isConnected
            }
        }
    }

    private fun buildEmptyResult(headerInfoFlow: Flow<ChatHeaderInfo?>): ChatStreamResult {
        return ChatStreamResult(
            uiItemsFlow = headerInfoFlow.map { headerInfo ->
                buildList {
                    addHeaderItems(headerInfo)
                }
            }
        )
    }

    private fun MutableList<ChatItem>.addHeaderItems(
        headerInfo: ChatHeaderInfo?
    ) {
        headerInfo?.welcomeInfo?.let { add(WelcomeCardItem(it)) }
    }

    fun cancel() {
        currentPaginator?.cancel()
        reconnectJob?.cancel()
        useCaseScope.cancel()
    }

    companion object {
        private val emptyLoadingState = MutableStateFlow(false)
    }
}
