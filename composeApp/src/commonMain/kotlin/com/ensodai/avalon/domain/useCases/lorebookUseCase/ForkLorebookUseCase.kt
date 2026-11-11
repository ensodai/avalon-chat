package com.ensodai.avalon.domain.useCases.lorebookUseCase

import com.ensodai.shared.avalon.character.models.CharacterLorebookDetailsDto
import com.ensodai.shared.avalon.character.models.ForkLorebookRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single

@Single
class ForkLorebookUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(request: ForkLorebookRequest): CharacterLorebookDetailsDto {
        return characterRpcService.forkLorebook(request)
    }
}
