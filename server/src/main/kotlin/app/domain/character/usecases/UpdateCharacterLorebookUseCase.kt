package app.domain.character.usecases

import app.data.repository.CharacterSpecRepository
import com.ensodai.shared.avalon.character.models.UpdateCharacterLorebookRequest
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class UpdateCharacterLorebookUseCase(
    private val characterSpecRepository: CharacterSpecRepository
) {
    suspend fun execute(request: UpdateCharacterLorebookRequest, userId: Uuid) {
        characterSpecRepository.updateLorebook(request, userId)
    }
}
