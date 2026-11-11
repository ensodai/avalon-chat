package app.domain.persona.usecases

import org.koin.core.annotation.Factory
import app.data.repository.UserRepositoryApp
import kotlin.uuid.Uuid

@Factory
class DeletePersonaUseCase(private val userRepository: UserRepositoryApp) {
    suspend fun execute(personaId: Uuid, userId: Uuid) {
        val isDeleted = userRepository.deletePersona(
            userId = userId,
            personaId = personaId
        )

        if (!isDeleted) {
            throw Exception("Persona not found or you don't have access")
        }
    }
}
