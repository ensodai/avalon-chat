package com.ensodai.avalon.domain.useCases.characterOutreachSettingsUseCase

import com.ensodai.shared.avalon.character.models.UpdateWarmOutreachSettingsRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single

@Single
class UpdateWarmOutreachSettingsUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(request: UpdateWarmOutreachSettingsRequest) {
        characterRpcService.updateWarmOutreachSettings(request)
    }
}
