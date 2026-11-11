package app.domain.character.usecases

import app.data.repository.CharacterSpecRepository
import com.ensodai.shared.avalon.character.models.CharacterLorebookEntryItemDto
import com.ensodai.shared.avalon.character.models.SaveLorebookEntryRequest
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class SaveLorebookEntryUseCase(
    private val characterSpecRepository: CharacterSpecRepository
) {
    suspend fun execute(request: SaveLorebookEntryRequest, userId: Uuid): CharacterLorebookEntryItemDto {
        return characterSpecRepository.saveLorebookEntry(
            lorebookId = request.lorebookId,
            entry = request.entry,
            userId = userId
        )
    }
}
