@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.ensodai.avalon.data.repository.remote.rpc

import com.ensodai.shared.avalon.persona.models.*
import com.ensodai.shared.avalon.rpc.services.PersonaRpcService
import com.ensodai.avalon.di.RpcConnectionManager
import kotlinx.rpc.withService
import kotlin.uuid.Uuid

open class PersonaRpcServiceRemoteImpl(
    private val manager: RpcConnectionManager,
    private val executor: RpcCallExecutor
) : PersonaRpcService {

    open suspend fun getService(): PersonaRpcService = manager.getActiveClient().withService<PersonaRpcService>()

    override suspend fun createPersona(request: PersonaCreateRequest): PersonaResponse =
        executor.execute("createPersona", getService = { getService() }) { createPersona(request) }

    override suspend fun getPersonas(): List<PersonaResponse> =
        executor.execute("getPersonas", getService = { getService() }) { getPersonas() }

    override suspend fun updatePersona(request: PersonaUpdateRequest): PersonaResponse =
        executor.execute("updatePersona", getService = { getService() }) { updatePersona(request) }

    override suspend fun deletePersona(personaId: Uuid) =
        executor.execute("deletePersona", getService = { getService() }) { deletePersona(personaId) }

    override suspend fun selectPersona(request: SelectPersonaRequest) =
        executor.execute("selectPersona", getService = { getService() }) { selectPersona(request) }
}
