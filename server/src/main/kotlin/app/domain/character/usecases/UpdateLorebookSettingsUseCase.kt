package app.domain.character.usecases

import app.data.repository.CharacterSpecRepository
import com.ensodai.shared.avalon.character.models.UpdateLorebookSettingsRequest
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class UpdateLorebookSettingsUseCase(
    private val characterSpecRepository: CharacterSpecRepository
) {
    suspend fun execute(request: UpdateLorebookSettingsRequest, userId: Uuid) {
        characterSpecRepository.updateLorebookSettings(request, userId)
    }
}
