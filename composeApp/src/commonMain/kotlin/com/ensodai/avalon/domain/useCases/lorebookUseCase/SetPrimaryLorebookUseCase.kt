package com.ensodai.avalon.domain.useCases.lorebookUseCase

import com.ensodai.shared.avalon.character.models.SetPrimaryLorebookRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single

@Single
class SetPrimaryLorebookUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(request: SetPrimaryLorebookRequest) {
        characterRpcService.setPrimaryLorebook(request)
    }
}
