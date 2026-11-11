package com.ensodai.shared.avalon.chat.services

import com.ensodai.shared.avalon.chat.models.*
import com.ensodai.shared.avalon.chat.wss.*
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc
import kotlin.uuid.Uuid

@Rpc
interface ChatRpcService  {
    // --- Метаданные и история ---
    
    suspend fun getChatHistory(request: GetChatHistoryRequest): PaginatedChatResponse
    
    suspend fun syncChatHistory(request: ChatSyncRequest): ChatSyncResponse
    
    suspend fun createChatVariant(request: CreateChatVariantRequest): ChatVariantResponse
    
    suspend fun deleteChatVariant(variantId: Uuid)
    
    suspend fun updateChatVariant(request: UpdateChatVariantRequest): ChatVariantResponse

    // --- Реальное время и сообщения ---

    /**
     * Стрим серверных событий чата (новые сообщения, обновления текста, удаления и т.д.).
     */
    fun observeChatEvents(): Flow<ServerMessage>
    fun observeChatEventsV2(): Flow<RpcStreamEvent<ServerMessage>>

    /**
     * Стрим событий тайпинга (пульсации статуса печати).
     */
    fun observeTyping(): Flow<TypingState>
    fun observeTypingV2(): Flow<RpcStreamEvent<TypingState>>

    /**
     * Отправка сообщения.
     */
    suspend fun sendMessage(message: ClientMessage.SendChatMessage)

    /**
     * Редактирование сообщения.
     */
    suspend fun editMessage(message: ClientMessage.EditChatMessage)

    /**
     * Удаление пачки сообщений (слотов).
     */
    suspend fun deleteMessages(message: ClientMessage.DeleteMessages)

    /**
     * Переключение активного контейнера в слоте.
     */
    suspend fun changeActiveContainer(message: ClientMessage.ChangeActiveContainer)

    /**
     * Отправка реакции пользователя.
     */
    suspend fun toggleReaction(message: ClientMessage.SendUserReaction)

    /**
     * Принудительный старт генерации ответа.
     */
    suspend fun forceStartGeneration(message: ClientMessage.ForceStartGenerateResponse)

    /**
     * Остановка генерации ответа.
     */
    suspend fun stopGeneration(chatSessionId: Uuid)

    /**
     * Генерация нового варианта ответа (контейнера).
     */
    suspend fun generateNewContainer(message: ClientMessage.GenerateNewContainer)

    /**
     * Отправка обратной связи (👍/👎) пользователя на приветственное сообщение от бота (Outreach).
     */
    suspend fun sendOutreachFeedback(characterId: Uuid, isPositive: Boolean)

    /**
     * Помечает указанные сообщения (контейнеры) как прочитанные для текущего пользователя.
     */
    suspend fun markMessagesAsRead(messageContainerIds: List<Uuid>)

    /**
     * Запрос озвучивания конкретного элемента контента сообщения (текста).
     */
    fun requestSpeech(messageContentId: Uuid, voice: VoiceModel?): Flow<ByteArray>
}
