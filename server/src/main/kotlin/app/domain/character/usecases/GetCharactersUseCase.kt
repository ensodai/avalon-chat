package app.domain.character.usecases

import org.koin.core.annotation.Factory
import app.data.network.models.response.paginateCharacters
import com.ensodai.shared.avalon.character.models.PaginatedResponse
import com.ensodai.shared.avalon.character.models.CharacterPreviewResponse

@Factory
class GetCharactersUseCase {
    suspend fun execute(category: String, page: Int): PaginatedResponse<CharacterPreviewResponse> {
        return paginateCharacters(page, category)
    }
}
