package app.domain.character.usecases

import app.data.repository.CharacterSpecRepository
import com.ensodai.shared.avalon.character.models.DeleteLorebookEntryRequest
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class DeleteLorebookEntryUseCase(
    private val characterSpecRepository: CharacterSpecRepository
) {
    suspend fun execute(request: DeleteLorebookEntryRequest, userId: Uuid) {
        characterSpecRepository.deleteLorebookEntry(
            lorebookId = request.lorebookId,
            entryId = request.entryId,
            userId = userId
        )
    }
}
