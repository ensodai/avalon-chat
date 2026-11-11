package com.ensodai.avalon.domain.useCases.personaScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import com.ensodai.shared.avalon.rpc.services.PersonaRpcService
import com.ensodai.avalon.utils.AppLogger
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class DeletePersonaUseCase(
    private val personaRpcService: PersonaRpcService,
    private val localUserRepository: LocalUserRepository,
    private val logger: AppLogger,
) {

    suspend fun delete(id: Uuid?): Result<Unit> {
        if (id == null) return Result.failure(Exception("id is null"))

        val result = runCatching {
            personaRpcService.deletePersona(personaId = id)
        }
        
        return result.map {
            localUserRepository.deletePersona(id)
            logger.d("DeletePersonaUseCase", "delete success for id: $id")
            Unit
        }
    }
}