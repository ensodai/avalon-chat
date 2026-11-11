@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.ensodai.shared.avalon.rpc.services

import com.ensodai.shared.avalon.persona.models.*
import kotlinx.rpc.annotations.Rpc
import kotlin.uuid.Uuid

@Rpc
interface PersonaRpcService {
    suspend fun createPersona(request: PersonaCreateRequest): PersonaResponse
    suspend fun getPersonas(): List<PersonaResponse>
    suspend fun updatePersona(request: PersonaUpdateRequest): PersonaResponse
    suspend fun deletePersona(personaId: Uuid)
    suspend fun selectPersona(request: SelectPersonaRequest)
}
