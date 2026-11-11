package com.ensodai.avalon.domain.useCases.lorebookUseCase

import com.ensodai.shared.avalon.character.models.CharacterLorebookDetailsDto
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GetCharacterLorebookUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(characterId: Uuid): CharacterLorebookDetailsDto? {
        return characterRpcService.getCharacterLorebook(characterId)
    }
}
