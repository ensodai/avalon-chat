package app.domain.character.usecases

import org.koin.core.annotation.Factory
import app.data.repository.CharacterUpdateRepository
import app.domain.utils.ServiceResult
import com.ensodai.shared.avalon.character.models.UpdateVoiceRequest
import kotlin.uuid.Uuid

@Factory
class UpdateCharacterVoiceUseCase(
    private val characterUpdateRepository: CharacterUpdateRepository
) {
    suspend fun execute(request: UpdateVoiceRequest, userId: Uuid) {
        val result = characterUpdateRepository.updateVoice(userId, request.characterId, request.voice)
        
        if (result is ServiceResult.Error) {
            throw Exception(result.message ?: "Operation failed")
        }
    }
}
