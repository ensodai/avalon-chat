package com.ensodai.avalon.domain.useCases.lorebookUseCase

import com.ensodai.shared.avalon.character.models.UserLorebookSummaryDto
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single

@Single
class GetUserLorebooksLibraryUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(): List<UserLorebookSummaryDto> {
        return characterRpcService.getUserLorebooksLibrary()
    }
}
