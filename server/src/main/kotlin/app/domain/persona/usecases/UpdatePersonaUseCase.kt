package app.domain.persona.usecases

import org.koin.core.annotation.Factory
import app.data.repository.UserRepositoryApp
import com.ensodai.shared.avalon.persona.models.PersonaUpdateRequest
import com.ensodai.shared.avalon.persona.models.PersonaResponse
import kotlin.uuid.Uuid

@Factory
class UpdatePersonaUseCase(private val userRepository: UserRepositoryApp) {
    suspend fun execute(request: PersonaUpdateRequest, userId: Uuid): PersonaResponse {
        val personaId = request.personaId

        return try {
            val updatedPersona = userRepository.updatePersona(
                userId = userId,
                personaId = personaId,
                name = request.name ?: "Unnamed",
                description = request.description ?: ""
            )

            updatedPersona ?: throw Exception("Persona not found or you don't have access")
        } catch (e: Exception) {
            if (e.message?.contains("not found") == true) throw e
            throw Exception(e.message ?: "Failed to update persona")
        }
    }
}
