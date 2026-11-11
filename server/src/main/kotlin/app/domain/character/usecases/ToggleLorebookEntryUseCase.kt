package app.domain.character.usecases

import app.data.repository.CharacterSpecRepository
import com.ensodai.shared.avalon.character.models.ToggleLorebookEntryRequest
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class ToggleLorebookEntryUseCase(
    private val characterSpecRepository: CharacterSpecRepository
) {
    suspend fun execute(request: ToggleLorebookEntryRequest, userId: Uuid) {
        characterSpecRepository.toggleLorebookEntry(
            lorebookId = request.lorebookId,
            entryId = request.entryId,
            enabled = request.enabled,
            userId = userId
        )
    }
}
