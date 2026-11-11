package app.domain.character.usecases

import org.koin.core.annotation.Factory
import app.data.repository.CharacterUpdateRepository
import app.domain.utils.ServiceResult
import com.ensodai.shared.avalon.character.models.UpdateFullDescriptionRequest
import kotlin.uuid.Uuid

@Factory
class UpdateCharacterFullDescriptionUseCase(
    private val characterUpdateRepository: CharacterUpdateRepository
) {
    suspend fun execute(request: UpdateFullDescriptionRequest, userId: Uuid) {
        val result = characterUpdateRepository.updateFullDescription(
            userId = userId,
            characterId = request.characterId,
            fullDescription = request.fullDescription,
            isPublicFullDescription = request.isPublicFullDescription
        )
        
        if (result is ServiceResult.Error) {
            throw Exception(result.message ?: "Operation failed")
        }
    }
}
