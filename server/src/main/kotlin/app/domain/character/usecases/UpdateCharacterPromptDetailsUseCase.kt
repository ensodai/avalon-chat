package app.domain.character.usecases

import app.data.repository.CharacterRepository
import app.data.repository.CharacterSpecRepository
import com.ensodai.shared.avalon.character.models.UpdateCharacterPromptDetailsRequest
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class UpdateCharacterPromptDetailsUseCase(
    private val characterRepository: CharacterRepository,
    private val characterSpecRepository: CharacterSpecRepository
) {
    suspend fun execute(request: UpdateCharacterPromptDetailsRequest, userId: Uuid) {
        val character = characterRepository.getCharacterById(request.characterId, userId)
        if (!character.canEdit) {
            throw IllegalAccessException("У вас нет прав на редактирование этого персонажа")
        }

        val currentDetails = characterSpecRepository.getPromptDetails(request.characterId)
        if (currentDetails?.isImported == true) {
            throw IllegalAccessException("Импортированная карточка стороннего автора защищена от изменений")
        }

        characterSpecRepository.updatePromptDetails(request)
    }

    suspend fun updateScenario(characterId: Uuid, scenario: String?, userId: Uuid) {
        val character = characterRepository.getCharacterById(characterId, userId)
        if (!character.canEdit) {
            throw IllegalAccessException("У вас нет прав на редактирование этого персонажа")
        }

        val currentDetails = characterSpecRepository.getPromptDetails(characterId)
        if (currentDetails?.isImported == true) {
            throw IllegalAccessException("Импортированная карточка стороннего автора защищена от изменений")
        }

        characterSpecRepository.updateScenario(characterId, scenario)
    }

    suspend fun updateMesExample(characterId: Uuid, mesExample: String?, userId: Uuid) {
        val character = characterRepository.getCharacterById(characterId, userId)
        if (!character.canEdit) {
            throw IllegalAccessException("У вас нет прав на редактирование этого персонажа")
        }

        val currentDetails = characterSpecRepository.getPromptDetails(characterId)
        if (currentDetails?.isImported == true) {
            throw IllegalAccessException("Импортированная карточка стороннего автора защищена от изменений")
        }

        characterSpecRepository.updateMesExample(characterId, mesExample)
    }
}
