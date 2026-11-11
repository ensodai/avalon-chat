package com.ensodai.avalon.domain.useCases.userAccountScreenUseCase

import com.ensodai.avalon.domain.models.PersonaInfo
import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetSelectedPersonaUseCase(private val localUserRepository: LocalUserRepository) {

    suspend fun getFlow(): Flow<PersonaInfo?> {
        return localUserRepository.getSelectedPersonaFlow()
    }

    suspend fun get(): PersonaInfo? {
        return localUserRepository.getSelectedPersona()
    }

}
