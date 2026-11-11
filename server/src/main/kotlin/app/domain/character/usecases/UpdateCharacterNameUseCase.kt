package app.domain.character.usecases

import org.koin.core.annotation.Factory
import app.data.repository.CharacterUpdateRepository
import app.domain.utils.ServiceResult
import com.ensodai.shared.avalon.character.models.UpdateNameRequest
import kotlin.uuid.Uuid

@Factory
class UpdateCharacterNameUseCase(
    private val characterUpdateRepository: CharacterUpdateRepository
) {
    suspend fun execute(request: UpdateNameRequest, userId: Uuid) {
        val trimmedName = request.newName.trim()
        if (trimmedName.isBlank()) throw Exception("Name cannot be empty")
        
        val finalName = if (trimmedName.length > 100) trimmedName.take(100) else trimmedName
        val result = characterUpdateRepository.updateName(userId, request.characterId, finalName)
        
        if (result is ServiceResult.Error) {
            throw Exception(result.message ?: "Operation failed")
        }
    }
}
