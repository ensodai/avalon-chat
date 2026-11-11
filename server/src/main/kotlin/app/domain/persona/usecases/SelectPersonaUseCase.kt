package app.domain.persona.usecases

import org.koin.core.annotation.Factory
import app.data.repository.UserRepositoryApp
import com.ensodai.shared.avalon.persona.models.SelectPersonaRequest
import kotlin.uuid.Uuid

@Factory
class SelectPersonaUseCase(private val userRepository: UserRepositoryApp) {
    suspend fun execute(request: SelectPersonaRequest, userId: Uuid) {
        val personaId = request.personaId

        val isSelected = userRepository.selectPersona(
            userId = userId,
            personaId = personaId
        )

        if (!isSelected) {
            throw Exception("Persona not found or access denied")
        }
    }
}
