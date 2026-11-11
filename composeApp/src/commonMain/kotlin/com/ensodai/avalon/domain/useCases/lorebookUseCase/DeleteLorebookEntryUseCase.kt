package com.ensodai.avalon.domain.useCases.lorebookUseCase

import com.ensodai.shared.avalon.character.models.DeleteLorebookEntryRequest
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single

@Single
class DeleteLorebookEntryUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(request: DeleteLorebookEntryRequest) {
        characterRpcService.deleteLorebookEntry(request)
    }
}
