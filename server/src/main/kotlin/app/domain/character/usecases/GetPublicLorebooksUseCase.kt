package app.domain.character.usecases

import app.data.repository.CharacterSpecRepository
import com.ensodai.shared.avalon.character.models.PaginatedResponse
import com.ensodai.shared.avalon.character.models.UserLorebookSummaryDto
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class GetPublicLorebooksUseCase(
    private val characterSpecRepository: CharacterSpecRepository
) {
    suspend fun execute(
        query: String?,
        page: Int,
        limit: Int,
        userId: Uuid
    ): PaginatedResponse<UserLorebookSummaryDto> {
        return characterSpecRepository.getPublicLorebooks(query, page, limit, userId)
    }
}
