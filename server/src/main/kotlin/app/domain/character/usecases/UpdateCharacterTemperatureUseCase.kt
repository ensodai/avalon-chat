package app.domain.character.usecases

import org.koin.core.annotation.Factory
import app.data.repository.CharacterUpdateRepository
import app.domain.utils.ServiceResult
import com.ensodai.shared.avalon.character.models.UpdateTemperatureRequest
import kotlin.uuid.Uuid

@Factory
class UpdateCharacterTemperatureUseCase(
    private val characterUpdateRepository: CharacterUpdateRepository
) {
    suspend fun execute(request: UpdateTemperatureRequest, userId: Uuid) {
        val temp = request.temperature.coerceIn(0.0f, 2.0f)
        val result = characterUpdateRepository.updateTemperature(userId, request.characterId, temp)
        
        if (result is ServiceResult.Error) {
            throw Exception(result.message ?: "Operation failed")
        }
    }
}
