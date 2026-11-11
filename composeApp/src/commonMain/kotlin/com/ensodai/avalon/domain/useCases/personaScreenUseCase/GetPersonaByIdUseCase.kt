package com.ensodai.avalon.domain.useCases.personaScreenUseCase

import com.ensodai.avalon.domain.models.PersonaInfo
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GetPersonaByIdUseCase(
    private val localUserRepository: LocalUserRepository
) {
    suspend fun get(id: Uuid): PersonaInfo? {
        return localUserRepository.getPersonaById(id)

    }
}