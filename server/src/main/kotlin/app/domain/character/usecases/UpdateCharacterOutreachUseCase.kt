package app.domain.character.usecases

import org.koin.core.annotation.Factory
import app.data.repository.CharacterUpdateRepository
import app.domain.utils.ServiceResult
import com.ensodai.shared.avalon.character.models.UpdateCharacterOutreachRequest
import kotlin.uuid.Uuid

@Factory
class UpdateCharacterOutreachUseCase(
    private val characterUpdateRepository: CharacterUpdateRepository
) {
    suspend fun execute(request: UpdateCharacterOutreachRequest, userId: Uuid) {
        val result = characterUpdateRepository.updateOutreach(userId, request.characterId, request.isOutreachEnabled)
        
        if (result is ServiceResult.Error) {
            throw Exception(result.message ?: "Operation failed")
        }
    }
}
