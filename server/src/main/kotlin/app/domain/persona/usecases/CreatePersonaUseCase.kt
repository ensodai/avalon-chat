package app.domain.persona.usecases

import org.koin.core.annotation.Factory
import app.data.repository.UserRepositoryApp
import com.ensodai.shared.avalon.persona.models.PersonaCreateRequest
import com.ensodai.shared.avalon.persona.models.PersonaResponse
import kotlin.uuid.Uuid

@Factory
class CreatePersonaUseCase(private val userRepository: UserRepositoryApp) {
    suspend fun execute(request: PersonaCreateRequest, userId: Uuid): PersonaResponse {
        return try {
            userRepository.createPersona(
                userId = userId,
                name = request.name ?: "Unnamed",
                description = request.description ?: ""
            )
        } catch (e: Exception) {
            throw Exception(e.message ?: "Failed to create persona")
        }
    }
}
