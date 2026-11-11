package app.domain.character.usecases

import org.koin.core.annotation.Factory
import app.data.repository.CharacterUpdateRepository
import app.domain.utils.ServiceResult
import com.ensodai.shared.avalon.character.models.UpdateDescriptionRequest
import kotlin.uuid.Uuid

@Factory
class UpdateCharacterDescriptionUseCase(
    private val characterUpdateRepository: CharacterUpdateRepository
) {
    suspend fun execute(request: UpdateDescriptionRequest, userId: Uuid) {
        val trimmedDesc = request.description.trim()
        val finalDesc = if (trimmedDesc.length > 500) trimmedDesc.take(500) else trimmedDesc
        val result = characterUpdateRepository.updateDescription(userId, request.characterId, finalDesc)
        
        if (result is ServiceResult.Error) {
            throw Exception(result.message ?: "Operation failed")
        }
    }
}
