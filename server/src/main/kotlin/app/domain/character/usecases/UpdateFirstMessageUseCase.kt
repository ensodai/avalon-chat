package app.domain.character.usecases

import org.koin.core.annotation.Factory
import app.data.repository.CharacterUpdateRepository
import app.domain.utils.ServiceResult
import com.ensodai.shared.avalon.character.models.UpdateFirstMessageRequest
import kotlin.uuid.Uuid

@Factory
class UpdateFirstMessageUseCase(
    private val characterUpdateRepository: CharacterUpdateRepository
) {
    suspend fun execute(request: UpdateFirstMessageRequest, userId: Uuid) {
        val trimmedMessage = request.message?.trim()?.ifEmpty { null }
        val result = characterUpdateRepository.updateFirstMessage(userId, request.characterId, trimmedMessage)
        
        if (result is ServiceResult.Error) {
            throw Exception(result.message ?: "Operation failed")
        }
    }
}
