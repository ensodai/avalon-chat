package com.ensodai.avalon.domain.useCases.lorebookUseCase

import com.ensodai.shared.avalon.character.models.UpdateCharacterLorebookRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single

@Single
class UpdateCharacterLorebookUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(request: UpdateCharacterLorebookRequest) {
        characterRpcService.updateCharacterLorebook(request)
    }
}
