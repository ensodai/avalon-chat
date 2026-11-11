package app.domain.character.usecases

import org.koin.core.annotation.Factory
import com.ensodai.shared.avalon.character.models.CharacterPreviewResponse
import com.ensodai.shared.avalon.character.models.PaginatedResponse
import app.data.repository.CharacterRepository

@Factory
class SearchCharacterUseCase(private val characterRepository: CharacterRepository) {
    suspend fun execute(query: String, page: Int): PaginatedResponse<CharacterPreviewResponse> {
        if (query.isBlank()) {
            return PaginatedResponse(
                page = 1,
                totalPages = 0,
                items = emptyList(),
                limit = 10
            )
        }

        return characterRepository.searchCharacters(query, page, 10)
    }
}
