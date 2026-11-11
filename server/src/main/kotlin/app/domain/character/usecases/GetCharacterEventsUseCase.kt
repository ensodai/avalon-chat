package app.domain.character.usecases

import app.data.repository.CharacterRepository
import com.ensodai.shared.avalon.character.models.CharacterEventDto
import org.koin.core.annotation.Factory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import io.ktor.server.plugins.NotFoundException

@OptIn(ExperimentalUuidApi::class)
@Factory
class GetCharacterEventsUseCase(
    private val characterRepository: CharacterRepository
) {
    suspend operator fun invoke(characterId: Uuid, userId: Uuid): List<CharacterEventDto> {
        val isOwner = characterRepository.belongsToUser(characterId, userId)
        if (!isOwner) {
            throw IllegalArgumentException("You are not the owner of this character")
        }
        
        return characterRepository.getEventsByCharacterId(characterId)
    }
}
