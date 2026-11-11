package com.ensodai.avalon.domain.useCases.commonUseCase

import com.ensodai.avalon.domain.chatEvents.RpcConnectionState
import com.ensodai.avalon.domain.chatEvents.ChatSyncRpcClient
import com.ensodai.avalon.domain.messageSender.TypingStatusManager
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.presentation.core.ChatStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GetChatStatusUseCase(
    private val chatSyncRpcClient: ChatSyncRpcClient,
    private val typingStatusManager: TypingStatusManager,
    private val localChatSessionsRepository: LocalChatSessionsRepository
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(characterId: Uuid?): Flow<ChatStatus> {
        if (characterId == null) {
            return chatSyncRpcClient.connectionState.map { state ->
                when (state) {
                    is RpcConnectionState.Connected -> ChatStatus.Online
                    else -> ChatStatus.Connecting
                }
            }
        }

        // Получаем chatSessionId из БД по characterId, затем наблюдаем за статусом тайпинга
        val typingFlow: Flow<ChatStatus?> = localChatSessionsRepository
            .getSessionByCharacterIdFlow(characterId)
            .flatMapLatest { sessionId ->
                if (sessionId == null) {
                    flowOf(null)
                } else {
                    typingStatusManager.observeChatTypingState(sessionId).map { typingContext ->
                        if (typingContext != null) ChatStatus.Typing(typingContext.rarity) else null
                    }
                }
            }

        return combine(
            chatSyncRpcClient.connectionState,
            typingFlow
        ) { connectionState, typingStatus ->
            when {
                connectionState !is RpcConnectionState.Connected -> ChatStatus.Connecting
                typingStatus != null -> typingStatus
                else -> ChatStatus.Online
            }
        }
    }
}
