package com.ensodai.avalon.domain.useCases.archiveScreenUseCase

import com.ensodai.avalon.data.db.models.ChatSessionPreview
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetArchivedChatsUseCase(private val localChatSessionsRepository: LocalChatSessionsRepository) {

    fun get(): Flow<List<ChatSessionPreview>> {
        return localChatSessionsRepository.getChatSessionsPagingData(isArchived = true)
    }
}

