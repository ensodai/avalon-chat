package app.domain.character.usecases

import app.data.repository.CharacterRepository
import com.ensodai.shared.avalon.character.models.UpdateCharacterEventsRequest
import org.koin.core.annotation.Factory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Factory
class UpdateCharacterEventsUseCase(
    private val characterRepository: CharacterRepository
) {
    suspend operator fun invoke(request: UpdateCharacterEventsRequest, userId: Uuid) {
        val isOwner = characterRepository.belongsToUser(request.characterId, userId)
        if (!isOwner) {
            throw IllegalArgumentException("You are not the owner of this character")
        }
        
        val levels = request.events.map { it.requiredLevel }
        if (levels.size != levels.toSet().size) {
            throw IllegalArgumentException("Каждому уровню отношений должно соответствовать ровно одно событие")
        }
        
        characterRepository.replaceCharacterEvents(request.characterId, request.events)
    }
}
