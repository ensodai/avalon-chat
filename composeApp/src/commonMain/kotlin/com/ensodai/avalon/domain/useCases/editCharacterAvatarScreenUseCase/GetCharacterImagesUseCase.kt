package com.ensodai.avalon.domain.useCases.editCharacterAvatarScreenUseCase

import com.ensodai.avalon.data.db.models.character.CharacterVisualsResult
import com.ensodai.avalon.domain.repository.local.LocalCharacterRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GetCharacterImagesUseCase(
    private val localCharacterRepository: LocalCharacterRepository
) {

    fun get(characterId: Uuid): Flow<CharacterVisualsResult> {
        return localCharacterRepository.getCharacterImagesFlow(characterId)
    }

}