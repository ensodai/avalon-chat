package com.ensodai.avalon.domain.useCases.userAccountScreenUseCase

import com.ensodai.avalon.domain.repository.local.LocalUserRepository
import com.ensodai.shared.avalon.persona.models.SelectPersonaRequest
import com.ensodai.shared.avalon.rpc.services.PersonaRpcService
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class SelectPersonaUseCase(
    private val personaRpcService: PersonaRpcService,
    private val localUserRepository: LocalUserRepository
) {

    suspend fun selectLocal(id: Uuid) {
        val currentPersona = localUserRepository.getSelectedPersona()
        val personaToSelect = if (currentPersona?.id == id) null else id
        
        localUserRepository.selectPersona(personaToSelect)
        runCatching {
            personaRpcService.selectPersona(SelectPersonaRequest(personaId = personaToSelect))
        }
    }
}
