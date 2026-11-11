package com.ensodai.avalon.domain.useCases.lorebookUseCase

import com.ensodai.shared.avalon.character.models.ToggleLorebookBindingRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single

@Single
class ToggleLorebookBindingUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(request: ToggleLorebookBindingRequest) {
        characterRpcService.toggleLorebookBinding(request)
    }
}
