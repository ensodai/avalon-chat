package com.ensodai.avalon.domain.useCases.personaScreenUseCase

import com.ensodai.avalon.data.db.models.user.Persona
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import com.ensodai.shared.avalon.rpc.services.PersonaRpcService
import com.ensodai.shared.avalon.persona.models.PersonaUpdateRequest
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class UpdatePersonaUseCase(
    private val personaRpcService: PersonaRpcService,
    private val localUserRepository: LocalUserRepository
) {

    suspend fun update(
        personaId: Uuid?,
        name: String,
        description: String
    ): Result<Unit> {
        if (personaId == null) return Result.failure(Exception("PersonaId is null"))
        
        val result = runCatching {
            personaRpcService.updatePersona(
                PersonaUpdateRequest(
                    personaId = personaId,
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