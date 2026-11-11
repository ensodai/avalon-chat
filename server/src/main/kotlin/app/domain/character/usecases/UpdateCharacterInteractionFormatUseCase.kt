package app.domain.character.usecases

import org.koin.core.annotation.Factory
import app.data.repository.CharacterUpdateRepository
import app.domain.utils.ServiceResult
import com.ensodai.shared.avalon.character.models.UpdateInteractionFormatRequest
import kotlin.uuid.Uuid

@Factory
class UpdateCharacterInteractionFormatUseCase(
    private val characterUpdateRepository: CharacterUpdateRepository
) {
    suspend fun execute(request: UpdateInteractionFormatRequest, userId: Uuid) {
        val result = characterUpdateRepository.updateInteractionFormat(
            userId = userId,
            charId = request.characterId,
            format = request.interactionFormat
        )
        if (result is ServiceResult.Error) {
            throw Exception(result.message ?: "Operation failed")
        }
    }
}
