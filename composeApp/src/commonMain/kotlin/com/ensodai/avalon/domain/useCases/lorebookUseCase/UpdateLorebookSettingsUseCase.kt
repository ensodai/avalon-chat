package com.ensodai.avalon.domain.useCases.lorebookUseCase

import com.ensodai.shared.avalon.character.models.UpdateLorebookSettingsRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single

@Single
class UpdateLorebookSettingsUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(request: UpdateLorebookSettingsRequest) {
        characterRpcService.updateLorebookSettings(request)
    }
}
