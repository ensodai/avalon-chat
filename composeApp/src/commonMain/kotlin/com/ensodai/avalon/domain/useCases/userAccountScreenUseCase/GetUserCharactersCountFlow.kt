package com.ensodai.avalon.domain.useCases.userAccountScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetUserCharactersCountFlow(
    val localCharactersRepository: LocalCharacterRepository
) {

    fun get(): Flow<Int> {
        return localCharactersRepository.getUserCharactersCountFlow()
    }

}