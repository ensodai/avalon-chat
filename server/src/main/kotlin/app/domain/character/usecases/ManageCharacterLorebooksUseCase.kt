package app.domain.character.usecases

import app.data.repository.CharacterRepository
import app.data.repository.CharacterSpecRepository
import com.ensodai.shared.avalon.character.models.*
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class ManageCharacterLorebooksUseCase(
    private val characterRepository: CharacterRepository,
    private val characterSpecRepository: CharacterSpecRepository
) {
    suspend fun attachLorebook(request: AttachLorebookRequest, userId: Uuid): CharacterLorebookBindingDto {
        val character = characterRepository.getCharacterById(request.characterId, userId)
        if (!character.canEdit) {
            throw IllegalAccessException("У вас нет прав на редактирование этого персонажа")
        }
        return characterSpecRepository.attachLorebook(
            characterId = request.characterId,
            lorebookId = request.lorebookId,
            isEnabled = request.isEnabled,
            isPrimary = request.isPrimary,
            priority = request.priority,
            userId = userId
        )
    }

    suspend fun detachLorebook(request: DetachLorebookRequest, userId: Uuid) {
        val character = characterRepository.getCharacterById(request.characterId, userId)
        if (!character.canEdit) {
            throw IllegalAccessException("У вас нет прав на редактирование этого персонажа")
        }
        characterSpecRepository.detachLorebook(request.characterId, request.lorebookId)
    }

    suspend fun toggleLorebookBinding(request: ToggleLorebookBindingRequest, userId: Uuid) {
        val character = characterRepository.getCharacterById(request.characterId, userId)
        if (!character.canEdit) {
            throw IllegalAccessException("У вас нет прав на редактирование этого персонажа")
        }
        characterSpecRepository.toggleLorebookBinding(request.characterId, request.lorebookId, request.isEnabled)
    }

    suspend fun setPrimaryLorebook(request: SetPrimaryLorebookRequest, userId: Uuid) {
        val character = characterRepository.getCharacterById(request.characterId, userId)
        if (!character.canEdit) {
            throw IllegalAccessException("У вас нет прав на редактирование этого персонажа")
        }
        characterSpecRepository.setPrimaryLorebook(request.characterId, request.lorebookId)
    }

    suspend fun createStandaloneLorebook(request: CreateStandaloneLorebookRequest, userId: Uuid): CharacterLorebookDetailsDto {
        if (request.attachToCharacterId != null) {
            val character = characterRepository.getCharacterById(request.attachToCharacterId!!, userId)
            if (!character.canEdit) {
                throw IllegalAccessException("У вас нет прав на редактирование этого персонажа")
            }
        }
        return characterSpecRepository.createStandaloneLorebook(userId, request)
    }

    suspend fun forkLorebook(request: ForkLorebookRequest, userId: Uuid): CharacterLorebookDetailsDto {
        if (request.attachToCharacterId != null) {
            val character = characterRepository.getCharacterById(request.attachToCharacterId!!, userId)
            if (!character.canEdit) {
                throw IllegalAccessException("У вас нет прав на редактирование этого персонажа")
            }
        }
        return characterSpecRepository.forkLorebook(request.lorebookId, userId, request.attachToCharacterId)
    }

    suspend fun deleteLorebook(lorebookId: Uuid, userId: Uuid) {
        characterSpecRepository.deleteLorebook(lorebookId, userId)
    }

    suspend fun saveLorebookToLibrary(lorebookId: Uuid, userId: Uuid) {
        characterSpecRepository.saveLorebookToLibrary(lorebookId, userId)
    }

    suspend fun removeLorebookFromLibrary(lorebookId: Uuid, userId: Uuid) {
        characterSpecRepository.removeLorebookFromLibrary(lorebookId, userId)
    }

    suspend fun attachUserLorebook(request: AttachUserCharacterLorebookRequest, userId: Uuid): CharacterLorebookBindingDto {
        return characterSpecRepository.attachUserLorebook(request.characterId, request.lorebookId, userId)
    }

    suspend fun detachUserLorebook(request: DetachUserCharacterLorebookRequest, userId: Uuid) {
        characterSpecRepository.detachUserLorebook(request.characterId, request.lorebookId, userId)
    }
}
