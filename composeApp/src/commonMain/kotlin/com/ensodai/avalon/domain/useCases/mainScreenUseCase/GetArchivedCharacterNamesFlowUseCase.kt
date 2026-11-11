package com.ensodai.avalon.domain.useCases.mainScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalChatSessionsRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetArchivedCharacterNamesFlowUseCase(
    private val localChatSessionsRepository: LocalChatSessionsRepository
) {

    fun get(): Flow<List<String>> {
        return localChatSessionsRepository.getArchivedChatSessionsNames()
    }

}
