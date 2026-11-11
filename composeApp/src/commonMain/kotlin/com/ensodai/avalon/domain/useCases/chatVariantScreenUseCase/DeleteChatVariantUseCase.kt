package com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.shared.avalon.chat.services.ChatRpcService
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class DeleteChatVariantUseCase(
    private val chatRpcService: ChatRpcService,
    private val localCharacterRepository: LocalCharacterRepository
) {
    suspend fun delete(chatVariantId: Uuid): Result<Unit> {
        return runCatching {
            chatRpcService.deleteChatVariant(variantId = chatVariantId)
        }.onSuccess {
            localCharacterRepository.deleteChatVariant(serverId = chatVariantId)
        }
    }
}