package com.ensodai.avalon.domain.useCases.editTemperatureScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GetTemperatureUseCase(
    private val localCharacterRepository: LocalCharacterRepository,
) {

    fun get(characterId: Uuid): Flow<Float?> {
        return localCharacterRepository.getCharacterTemperatureFlow(characterId)
    }

}