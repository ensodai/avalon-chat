package com.ensodai.avalon.domain.useCases.userCharacterScreenUseCase

import com.ensodai.avalon.data.db.dto.CharacterWithShortDetails
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetUserCharactersPagingDataUseCase(
    private val localCharactersRepository: LocalCharacterRepository
) {

    operator fun invoke(): Flow<List<CharacterWithShortDetails>> {
        return localCharactersRepository.getUserCharactersPagingData()
    }
}