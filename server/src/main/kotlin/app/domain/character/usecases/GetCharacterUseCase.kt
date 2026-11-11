package app.domain.character.usecases

import org.koin.core.annotation.Factory
import app.data.repository.CharacterRepository
import com.ensodai.shared.avalon.character.models.CharacterResponse
import kotlin.uuid.Uuid

@Factory
class GetCharacterUseCase(private val characterRepository: CharacterRepository) {
    suspend fun execute(characterPublicId: Uuid, userId: Uuid): CharacterResponse {
        return characterRepository.getCharacterById(characterPublicId, userId)
    }
}
