package com.ensodai.avalon.domain.useCases.relationshipRoadScreenUseCase

import com.ensodai.avalon.data.db.models.character.CharacterProfileTuple
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class GetLocalCharacterProfileUseCase(
    private val localCharacterRepository: LocalCharacterRepository
) {
    fun execute(characterId: Uuid): Flow<CharacterProfileTuple?> {
        return localCharacterRepository.getCharacterProfileFlow(characterId)
    }
}
