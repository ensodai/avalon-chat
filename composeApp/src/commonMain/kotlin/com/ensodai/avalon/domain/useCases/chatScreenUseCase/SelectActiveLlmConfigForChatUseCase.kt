package com.ensodai.avalon.domain.useCases.chatScreenUseCase

import com.ensodai.avalon.data.db.cache.AvailableLlmConfigs
import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class SelectActiveLlmConfigForChatUseCase(
    private val localChatSessionsRepository: LocalChatSessionsRepository
) {

    suspend fun select(
        characterId: Uuid,
        config: AvailableLlmConfigs
    ) {
        localChatSessionsRepository.selectActiveLlmConfig(
            characterId = characterId,
            config = config
        )
    }

}