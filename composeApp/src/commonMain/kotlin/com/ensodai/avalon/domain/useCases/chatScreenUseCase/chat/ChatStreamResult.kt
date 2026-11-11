package com.ensodai.avalon.domain.useCases.chatScreenUseCase.chat

import com.ensodai.avalon.domain.models.message.ChatItem
import kotlinx.coroutines.flow.Flow

data class ChatStreamResult(
    val uiItemsFlow: Flow<List<ChatItem>>
)
