package com.ensodai.avalon.domain.useCases.mainScreenUseCase

import com.ensodai.shared.avalon.character.models.CharacterPreviewResponse
import com.ensodai.shared.avalon.character.models.PaginatedResponse
import com.ensodai.shared.avalon.rpc.services.CharacterRpcService
import org.koin.core.annotation.Factory

@Factory
class GetCharactersByCategoryUseCase(
    private val characterRpcService: CharacterRpcService
) {
    suspend operator fun invoke(category: String, page: Int): Result<PaginatedResponse<CharacterPreviewResponse>> {
        return runCatching {
            characterRpcService.getCharacters(category = category, page = page)
        }
    }
}
