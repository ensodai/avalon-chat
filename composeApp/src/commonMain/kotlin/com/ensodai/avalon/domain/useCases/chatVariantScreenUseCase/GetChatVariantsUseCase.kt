package com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase

import com.ensodai.avalon.data.db.dto.ChatVariant
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import com.ensodai.avalon.utils.AppLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GetChatVariantsUseCase(
    private val localChatSessionsRepository: LocalChatSessionsRepository,
    private val localChatHistoryRepository: LocalChatHistoryRepository,
    private val logger: AppLogger
) {

    suspend fun get(characterId: Uuid?): Flow<List<ChatVariant>> {
        if (characterId == null) return emptyFlow()
        val sessionId = localChatSessionsRepository.getSessionByCharacterId(characterId)

        logger.d("GetChatVariantsUseCase", "sessionId: ${sessionId?.id}")

        return localChatHistoryRepository.getChatVariants(sessionId?.id)

    }
}
