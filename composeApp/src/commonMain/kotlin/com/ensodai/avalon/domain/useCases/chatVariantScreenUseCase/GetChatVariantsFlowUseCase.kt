package com.ensodai.avalon.domain.useCases.chatVariantScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import com.ensodai.avalon.domain.repository.local.LocalChatHistoryRepository
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import org.koin.core.annotation.Single

@Single
class GetChatVariantsFlowUseCase(
    private val localCharacterRepository: LocalCharacterRepository,
    private val localUserRepository: LocalUserRepository,
    private val localChatHistoryRepository: LocalChatHistoryRepository
) {

    suspend fun get(characterLocalId: Int) {

    }
}
