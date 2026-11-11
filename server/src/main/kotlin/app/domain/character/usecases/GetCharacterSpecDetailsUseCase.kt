package app.domain.character.usecases

import app.data.repository.CharacterSpecRepository
import com.ensodai.shared.avalon.character.models.CharacterAlternateGreetingDto
import com.ensodai.shared.avalon.character.models.CharacterLorebookBindingDto
import com.ensodai.shared.avalon.character.models.CharacterLorebookDetailsDto
import com.ensodai.shared.avalon.character.models.CharacterPromptDetailsDto
import com.ensodai.shared.avalon.character.models.UserLorebookSummaryDto
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class GetCharacterSpecDetailsUseCase(
    private val characterSpecRepository: CharacterSpecRepository
) {
    suspend fun getPromptDetails(characterId: Uuid): CharacterPromptDetailsDto? {
        return characterSpecRepository.getPromptDetails(characterId)
    }

    suspend fun getAlternateGreetings(characterId: Uuid): List<CharacterAlternateGreetingDto> {
        return characterSpecRepository.getAlternateGreetings(characterId)
    }

    suspend fun getLorebook(characterId: Uuid, userId: Uuid? = null): CharacterLorebookDetailsDto? {
        return characterSpecRepository.getLorebook(characterId, userId)
    }

    suspend fun getCharacterLorebooks(characterId: Uuid, userId: Uuid): List<CharacterLorebookBindingDto> {
        return characterSpecRepository.getCharacterLorebooks(characterId, userId)
    }

    suspend fun getLorebookById(lorebookId: Uuid, userId: Uuid): CharacterLorebookDetailsDto? {
        return characterSpecRepository.getLorebookById(lorebookId, userId)
    }

    suspend fun getUserLorebooksLibrary(userId: Uuid): List<UserLorebookSummaryDto> {
        return characterSpecRepository.getUserLorebooksLibrary(userId)
    }
}
