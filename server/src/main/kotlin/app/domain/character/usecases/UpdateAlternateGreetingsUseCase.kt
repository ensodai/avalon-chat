package app.domain.character.usecases

import app.data.repository.CharacterRepository
import app.data.repository.CharacterSpecRepository
import com.ensodai.shared.avalon.character.models.UpdateAlternateGreetingsRequest
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class UpdateAlternateGreetingsUseCase(
    private val characterRepository: CharacterRepository,
    private val characterSpecRepository: CharacterSpecRepository
) {
    suspend fun execute(request: UpdateAlternateGreetingsRequest, userId: Uuid) {
        val character = characterRepository.getCharacterById(request.characterId, userId)
        if (!character.canEdit) {
            throw IllegalAccessException("У вас нет прав на редактирование этого персонажа")
        }

        characterSpecRepository.updateAlternateGreetings(request)
    }
}
