package com.ensodai.avalon.domain.useCases.lorebookUseCase

import com.ensodai.shared.avalon.character.models.AttachLorebookRequest
import com.ensodai.shared.avalon.character.models.CharacterLorebookBindingDto
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single

@Single
class AttachLorebookUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(request: AttachLorebookRequest): CharacterLorebookBindingDto {
        return characterRpcService.attachLorebook(request)
    }
}
