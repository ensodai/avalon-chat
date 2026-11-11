package com.ensodai.avalon.domain.useCases.lorebookUseCase

import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class RemoveLorebookFromLibraryUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend fun execute(lorebookId: Uuid) {
        characterRpcService.removeLorebookFromLibrary(lorebookId)
    }
}
