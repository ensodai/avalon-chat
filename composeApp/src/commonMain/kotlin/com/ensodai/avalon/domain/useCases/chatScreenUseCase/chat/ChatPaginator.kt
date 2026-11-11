package com.ensodai.avalon.domain.useCases.chatScreenUseCase.chat

import com.ensodai.shared.avalon.chat.models.GetChatHistoryRequest
import com.ensodai.shared.avalon.chat.services.ChatRpcService
import com.ensodai.avalon.data.db.models.chat.SlotWithContainers
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.utils.AppLogger
import com.ensodai.avalon.utils.IODispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class ChatPaginator(
    val variantId: Uuid,
    val chatSessionId: Uuid,
    private val chatRpcService: ChatRpcService,
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val logger: AppLogger
) {
    private val paginatorScope = CoroutineScope(IODispatcher + SupervisorJob())
    private val loadMutex = Mutex()

    private val _currentLimit = MutableStateFlow(PAGE_SIZE)

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isEndReached = MutableStateFlow(false)
    val isEndReached: StateFlow<Boolean> = _isEndReached.asStateFlow()

    private var nextCursor: Instant? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val messagesFlow: Flow<List<SlotWithContainers>> = _currentLimit
        .flatMapLatest { limit -> localChatHistoryRepository.getChatHistoryFlow(variantId, limit) }
        .flowOn(IODispatcher)

    fun initialize() {
        paginatorScope.launch {
            loadMutex.withLock {
                val count = localChatHistoryRepository.getSlotsCount(variantId)
                logger.d("ChatPaginator", "Слотов в БД: $count, variantId: $variantId")

                fetchFromNetwork(cursor = null)
            }
        }
    }

    suspend fun loadMore() {
        logger.d("ChatPaginator", "loadMore:")
        if (_isEndReached.value) return         // быстрая проверка без лока
        if (!loadMutex.tryLock()) return        // уже выполняется — выходим

        try {
            if (_isEndReached.value) return     // повторная проверка внутри лока (могло измениться)
            val targetLimit = _currentLimit.value + PAGE_SIZE
            logger.d("ChatPaginator", "targetLimit $targetLimit")
            _currentLimit.value = targetLimit

            val totalInDb = localChatHistoryRepository.getSlotsCount(variantId)
            logger.d("ChatPaginator", "totalInDb $totalInDb")
            if (totalInDb < targetLimit) {
                logger.d("ChatPaginator", "totalInDb < targetLimit true")
                val cursor = nextCursor ?: localChatHistoryRepository.getOldestSlot(variantId)?.createdAt
                logger.d("ChatPaginator", "cursor $cursor")
                fetchFromNetwork(cursor = cursor)
            }
        } finally {
            logger.d("ChatPaginator", "finally loadMutex.unlock")
            loadMutex.unlock()
        }
    }

    private suspend fun fetchFromNetwork(cursor: Instant?) {
        logger.d("ChatPaginator", "fetchFromNetwork cursor $cursor")
        _isLoadingMore.value = true
        try {
            runCatching {
                chatRpcService.getChatHistory(
                    GetChatHistoryRequest(
                        chatSessionId = chatSessionId,
                        variantId = variantId,
                        cursor = cursor,
                        limit = PAGE_SIZE
                    )
                )
            }.onSuccess { response ->
                nextCursor = response.nextCursor
                _isEndReached.value = response.nextCursor == null
                logger.d(
                    "ChatPaginator",
                    "fetch success items=${response.items.size}, nextCursor=${response.nextCursor}, isEndReached=${_isEndReached.value}"
                )

                if (response.items.isEmpty()) {
                    logger.d("ChatPaginator", "Пустой ответ сервера")
                } else {
                    localChatHistoryRepository.saveChatHistoryResponse(variantId, response.items)
                }
            }.onFailure { error ->
                logger.e("ChatPaginator", "Ошибка пагинации: ${error.stackTraceToString()}")
            }
        } finally {
            _isLoadingMore.value = false
        }
    }

    fun cancel() {
        logger.d("ChatPaginator", "Отменён для варианта $variantId")
        paginatorScope.cancel()
    }

    companion object {
        private const val PAGE_SIZE = 50
    }
}
