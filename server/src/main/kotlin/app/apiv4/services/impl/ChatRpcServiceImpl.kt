package app.apiv4.services.impl

import app.apiv4.chat.ChatEventBus
import app.apiv4.infrastructure.BaseRpcService
import app.apiv4.infrastructure.RpcSessionContext
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import app.domain.chat.usecases.CreateChatVariantUseCase
import app.domain.chat.usecases.UpdateChatVariantUseCase
import app.domain.chat.usecases.SendOutreachFeedbackUseCase
import app.domain.chat.usecases.MarkMessagesAsReadUseCase
import app.domain.chat.usecases.GetChatHistoryUseCase
import app.domain.chat.usecases.RequestSpeechUseCase
import app.domain.chat.usecases.SyncChatHistoryUseCase
import app.domain.chat.usecases.DeleteChatVariantUseCase
import app.data.repository.ChatRepository
import app.domain.chat.usecases.ToggleReactionUseCase
import app.domain.chat.usecases.ChangeActiveContainerUseCase
import app.domain.chat.usecases.DeleteMessageUseCase
import app.domain.chat.usecases.EditChatMessageUseCase
import app.domain.chat.usecases.HandleChatMessageUseCase
import com.ensodai.shared.avalon.chat.wss.ClientMessage
import com.ensodai.shared.avalon.auth.models.AuthState
import com.ensodai.shared.avalon.chat.models.PaginatedChatResponse
import com.ensodai.shared.avalon.chat.models.ChatSyncResponse
import com.ensodai.shared.avalon.chat.models.ChatVariantResponse
import com.ensodai.shared.avalon.chat.models.GetChatHistoryRequest
import com.ensodai.shared.avalon.chat.models.ChatSyncRequest
import com.ensodai.shared.avalon.chat.models.CreateChatVariantRequest
import com.ensodai.shared.avalon.chat.models.UpdateChatVariantRequest
import com.ensodai.shared.avalon.chat.models.TypingState
import com.ensodai.shared.avalon.chat.wss.ServerMessage
import com.ensodai.shared.avalon.chat.wss.RpcStreamEvent
import com.ensodai.shared.avalon.chat.services.ChatRpcService
import app.utils.logger.LlmLogger
import io.ktor.server.application.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.*
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class ChatRpcServiceImpl(
    @InjectedParam private val call: ApplicationCall,
    @InjectedParam session: RpcSessionContext,
    private val chatEventBus: ChatEventBus,
    // UseCases
    private val handleChatMessageUseCase: HandleChatMessageUseCase,
    private val editChatMessageUseCase: EditChatMessageUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val changeActiveContainerUseCase: ChangeActiveContainerUseCase,
    private val toggleReactionUseCase: ToggleReactionUseCase,
    // Метаданные
    private val getChatHistoryUseCase: GetChatHistoryUseCase,
    private val syncChatHistoryUseCase: SyncChatHistoryUseCase,
    private val createChatVariantUseCase: CreateChatVariantUseCase,
    private val deleteChatVariantUseCase: DeleteChatVariantUseCase,
    private val updateChatVariantUseCase: UpdateChatVariantUseCase,
    private val sendOutreachFeedbackUseCase: SendOutreachFeedbackUseCase,
    private val markMessagesAsReadUseCase: MarkMessagesAsReadUseCase,
    private val chatRepository: ChatRepository,
    private val requestSpeechUseCase: RequestSpeechUseCase
) : BaseRpcService(session), ChatRpcService {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeChatEvents(): Flow<ServerMessage> {
        return session.observeUserId().flatMapLatest { userId ->
            if (userId != null) {
                chatEventBus.observe(userId)
                    .onStart { chatEventBus.registerOnlineUser(userId) }
                    .onCompletion { chatEventBus.deregisterOnlineUser(userId) }
            } else {
                flow {
                    awaitCancellation()
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeChatEventsV2(): Flow<RpcStreamEvent<ServerMessage>> {
        return session.observeUserId().flatMapLatest { userId ->
            if (userId != null) {
                chatEventBus.observeV2(userId)
                    .onStart { chatEventBus.registerOnlineUser(userId) }
                    .onCompletion { chatEventBus.deregisterOnlineUser(userId) }
            } else {
                flow {
                    awaitCancellation()
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeTyping(): Flow<TypingState> {
        return session.observeUserId().flatMapLatest { userId ->
            if (userId != null) {
                chatEventBus.observeTyping(userId)
            } else {
                flow {
                    awaitCancellation()
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeTypingV2(): Flow<RpcStreamEvent<TypingState>> {
        return session.observeUserId().flatMapLatest { userId ->
            if (userId != null) {
                chatEventBus.observeTypingV2(userId)
            } else {
                flow {
                    awaitCancellation()
                }
            }
        }
    }

    private suspend fun rpcSessionId(): Uuid = sessionId()

    // --- Метаданные и история ---

    override suspend fun getChatHistory(request: GetChatHistoryRequest): PaginatedChatResponse {
        ensureAuthorized()
        return getChatHistoryUseCase.execute(request, userId())
    }

    override suspend fun syncChatHistory(request: ChatSyncRequest): ChatSyncResponse {
        ensureAuthorized()
        return syncChatHistoryUseCase.execute(request, userId())
    }

    override suspend fun createChatVariant(request: CreateChatVariantRequest): ChatVariantResponse {
        ensureAuthorized()
        return createChatVariantUseCase.execute(
            sessionId = request.chatSessionId,
            userId = userId(),
            chatVariantId = request.chatVariantId
        )
    }

    override suspend fun deleteChatVariant(variantId: Uuid) {
        ensureAuthorized()
        deleteChatVariantUseCase.execute(variantId, userId())
    }

    override suspend fun updateChatVariant(request: UpdateChatVariantRequest): ChatVariantResponse {
        ensureAuthorized()
        return updateChatVariantUseCase.execute(request, userId())
    }

    // --- Реальное время и сообщения ---
    override suspend fun sendMessage(message: ClientMessage.SendChatMessage) {
        ensureAuthorized()
        handleChatMessageUseCase.start(
            userId = userId(),
            wssUserSessionId = rpcSessionId(),
            slotId = message.slotId,
            contentId = message.contentId,
            containerId = message.containerId,
            chatVariantId = message.chatVariantId,
            chatSessionId = message.chatSessionId,
            contentType = message.contentType,
            text = message.text,
            characterId = message.characterId,
            llmModelCode = message.llmModelCode,
            messageSystemInstruction = message.messageSystemInstruction
        )
    }

    override suspend fun editMessage(message: ClientMessage.EditChatMessage) {
        ensureAuthorized()
        val oldMessage = ClientMessage.EditChatMessage(
            slotId = message.slotId,
            containerId = message.containerId,
            chatVariantId = message.chatVariantId,
            chatSessionId = message.chatSessionId,
            text = message.text,
            messageSystemInstruction = message.messageSystemInstruction
        )
        editChatMessageUseCase.edit(userId(), oldMessage)
    }

    override suspend fun deleteMessages(message: ClientMessage.DeleteMessages) {
        ensureAuthorized()
        val oldMessage = ClientMessage.DeleteMessages(
            slotIds = message.slotIds,
            chatVariantId = message.chatVariantId,
            chatSessionId = message.chatSessionId
        )
        deleteMessageUseCase.delete(userId(), oldMessage)
    }

    override suspend fun changeActiveContainer(message: ClientMessage.ChangeActiveContainer) {
        ensureAuthorized()
        val oldMessage = ClientMessage.ChangeActiveContainer(
            chatVariantId = message.chatVariantId,
            chatSessionId = message.chatSessionId,
            slotId = message.slotId,
            newActiveContainerId = message.newActiveContainerId
        )
        changeActiveContainerUseCase.change(userId(), oldMessage)
    }

    override suspend fun toggleReaction(message: ClientMessage.SendUserReaction) {
        ensureAuthorized()
        
        toggleReactionUseCase.toggleReaction(
            userId = userId(),
            reaction = message.reaction,
            characterId = message.characterId,
            chatVariantId = message.chatVariantId,
            chatSessionId = message.chatSessionId,
            slotId = message.slotId,
            containerId = message.containerId,
            wssUserSessionId = rpcSessionId()
        )
    }

    override suspend fun forceStartGeneration(message: ClientMessage.ForceStartGenerateResponse) {
        ensureAuthorized()
        handleChatMessageUseCase.forceStart(
            userId = userId(),
            chatSessionId = message.chatSessionId,
            chatVariantId = message.chatVariantId,
            characterId = message.characterId,
            llmModelCode = message.llmModelCode
        )
    }

    override suspend fun stopGeneration(chatSessionId: Uuid) {
        ensureAuthorized()
        handleChatMessageUseCase.stop(chatSessionId)
    }

    override suspend fun generateNewContainer(message: ClientMessage.GenerateNewContainer) {
        ensureAuthorized()
        handleChatMessageUseCase.generateVariant(
            userId = userId(),
            chatSessionId = message.chatSessionId,
            chatVariantId = message.chatVariantId,
            characterId = message.characterId,
            slotId = message.slotId,
            llmModelCode = message.llmModelCode
        )
    }

    override suspend fun sendOutreachFeedback(characterId: Uuid, isPositive: Boolean) {
        ensureAuthorized()
        sendOutreachFeedbackUseCase.execute(userId(), characterId, isPositive)
    }

    override suspend fun markMessagesAsRead(messageContainerIds: List<Uuid>) {
        ensureAuthorized()
        markMessagesAsReadUseCase.execute(userId(), messageContainerIds)
    }

    override fun requestSpeech(messageContentId: Uuid, voice: VoiceModel?): Flow<ByteArray> {
        return flow {
            LlmLogger.inf("ChatRpcServiceImpl", "📥 RPC Call requestSpeech: contentId=$messageContentId")
            try {
                ensureAuthorized()
                val uid = userId()
                LlmLogger.inf("ChatRpcServiceImpl", "🔑 Authorized user: $uid")
                emitAll(requestSpeechUseCase.execute(uid, messageContentId, voice))
            } catch (e: Exception) {
                LlmLogger.error("ChatRpcServiceImpl", "❌ RPC requestSpeech failed: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
    }
}
