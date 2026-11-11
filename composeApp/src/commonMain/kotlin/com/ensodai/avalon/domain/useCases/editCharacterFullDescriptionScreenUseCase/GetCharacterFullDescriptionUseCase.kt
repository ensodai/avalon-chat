package com.ensodai.avalon.domain.useCases.editCharacterFullDescriptionScreenUseCase

import com.ensodai.avalon.data.db.models.character.DescriptionPrivacyTuple
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GetCharacterFullDescriptionUseCase(
    private val localCharacterRepository: LocalCharacterRepository
) {

    fun get(characterId: Uuid): Flow<DescriptionPrivacyTuple?> {
        return localCharacterRepository.getCharacterFullDescriptionFlow(characterId)
    }

}