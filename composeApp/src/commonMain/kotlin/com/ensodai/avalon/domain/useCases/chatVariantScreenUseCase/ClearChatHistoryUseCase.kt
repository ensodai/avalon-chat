package com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class ClearChatHistoryUseCase(
    private val localChatHistoryRepository: LocalChatHistoryRepository
) {

    suspend fun clear(chatVariantServerId: Uuid) {
        localChatHistoryRepository.clearChatVariant(chatVariantServerId)
    }
}