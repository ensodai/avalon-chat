@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package app.domain.character.usecases

import app.data.repository.CharacterRepository
import com.ensodai.shared.avalon.character.models.CharacterEnergyResponse
import org.koin.core.annotation.Factory
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Factory
class GetCharactersEnergyUseCase(private val characterRepository: CharacterRepository) {
    suspend fun execute(userId: Uuid): List<CharacterEnergyResponse> {
        return characterRepository.getCharactersEnergy(userId)
    }
}
