package com.ensodai.avalon.domain.useCases.lorebookUseCase

import com.ensodai.shared.avalon.character.models.DetachLorebookRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single

@Single
class DetachLorebookUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(request: DetachLorebookRequest) {
        characterRpcService.detachLorebook(request)
    }
}
