package com.ensodai.avalon.domain.useCases.lorebookUseCase

import com.ensodai.shared.avalon.character.models.ToggleLorebookEntryRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single

@Single
class ToggleLorebookEntryUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(request: ToggleLorebookEntryRequest) {
        characterRpcService.toggleLorebookEntry(request)
    }
}
