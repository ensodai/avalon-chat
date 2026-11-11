package com.ensodai.avalon.domain.useCases.lorebookUseCase

import com.ensodai.shared.avalon.character.models.CharacterLorebookDetailsDto
import com.ensodai.shared.avalon.character.models.CreateStandaloneLorebookRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single

@Single
class CreateStandaloneLorebookUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(request: CreateStandaloneLorebookRequest): CharacterLorebookDetailsDto {
        return characterRpcService.createStandaloneLorebook(request)
    }
}
