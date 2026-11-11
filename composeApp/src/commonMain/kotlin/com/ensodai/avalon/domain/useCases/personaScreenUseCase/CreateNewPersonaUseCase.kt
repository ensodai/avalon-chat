package com.ensodai.avalon.domain.useCases.personaScreenUseCase

import com.ensodai.avalon.data.db.models.user.Persona
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import com.ensodai.shared.avalon.rpc.services.PersonaRpcService
import com.ensodai.shared.avalon.persona.models.PersonaCreateRequest
import org.koin.core.annotation.Single

@Single
class CreateNewPersonaUseCase(
    private val personaRpcService: PersonaRpcService,
    private val localUserRepository: LocalUserRepository
) {

    suspend fun create(name: String, description: String): Result<Unit> {
        val result = runCatching {
            personaRpcService.createPersona(
                PersonaCreateRequest(
                    name = name,
                    description = description
                )
            )
        }
        
        return result.map { persona ->
            localUserRepository.insertPersona(
                Persona(
                    id = persona.id,
                    name = persona.name,
                    description = persona.description,
                    createdAt = persona.createdAt
                )
            )
            Unit
        }
    }
}
