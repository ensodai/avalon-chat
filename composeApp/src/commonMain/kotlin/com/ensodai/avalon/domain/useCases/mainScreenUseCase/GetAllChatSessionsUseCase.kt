package com.ensodai.avalon.domain.useCases.mainScreenUseCase

import com.ensodai.avalon.data.db.models.ChatSessionPreview
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetAllChatSessionsUseCase(private val localChatSessionsRepository: LocalChatSessionsRepository) {

    fun getAllChats(): Flow<List<ChatSessionPreview>> {
        return localChatSessionsRepository.getChatSessionsPagingData(isArchived = false)
    }
}
