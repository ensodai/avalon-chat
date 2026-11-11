package app.domain.persona.usecases

import org.koin.core.annotation.Factory
import app.data.repository.UserRepositoryApp
import com.ensodai.shared.avalon.persona.models.PersonaResponse
import kotlin.uuid.Uuid

@Factory
class GetPersonasUseCase(private val userRepository: UserRepositoryApp) {
    suspend fun execute(userId: Uuid): List<PersonaResponse> {
        return try {
            userRepository.getPersonasByUserId(userId)
        } catch (e: Exception) {
            throw Exception(e.message ?: "Failed to retrieve personas")
        }
    }
}
