package com.ensodai.avalon.domain.useCases.lorebookUseCase

import com.ensodai.shared.avalon.character.models.CharacterLorebookBindingDto
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GetCharacterLorebooksUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(characterId: Uuid): List<CharacterLorebookBindingDto> {
        return characterRpcService.getCharacterLorebooks(characterId)
    }
}
