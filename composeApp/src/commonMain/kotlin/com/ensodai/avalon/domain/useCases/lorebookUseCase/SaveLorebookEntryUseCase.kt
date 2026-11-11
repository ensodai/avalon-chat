package com.ensodai.avalon.domain.useCases.lorebookUseCase

import com.ensodai.shared.avalon.character.models.CharacterLorebookEntryItemDto
import com.ensodai.shared.avalon.character.models.SaveLorebookEntryRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single

@Single
class SaveLorebookEntryUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(request: SaveLorebookEntryRequest): CharacterLorebookEntryItemDto {
        return characterRpcService.saveLorebookEntry(request)
    }
}
