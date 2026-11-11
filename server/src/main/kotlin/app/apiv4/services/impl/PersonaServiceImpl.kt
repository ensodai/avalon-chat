@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package app.apiv4.services.impl

import app.domain.persona.usecases.CreatePersonaUseCase
import app.domain.persona.usecases.GetPersonasUseCase
import app.domain.persona.usecases.UpdatePersonaUseCase
import app.domain.persona.usecases.DeletePersonaUseCase
import app.domain.persona.usecases.SelectPersonaUseCase
import com.ensodai.shared.avalon.persona.models.PersonaCreateRequest
import com.ensodai.shared.avalon.persona.models.PersonaUpdateRequest
import com.ensodai.shared.avalon.persona.models.PersonaResponse
import com.ensodai.shared.avalon.persona.models.SelectPersonaRequest
import com.ensodai.shared.avalon.rpc.services.PersonaRpcService
import app.apiv4.infrastructure.BaseRpcService
import app.apiv4.infrastructure.RpcSessionContext
import io.ktor.server.application.ApplicationCall
import org.koin.core.component.KoinComponent
import kotlin.uuid.Uuid
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

@Factory
class PersonaServiceImpl(
    @InjectedParam private val call: ApplicationCall,
    @InjectedParam sessionContext: RpcSessionContext,
    private val createPersonaUseCase: CreatePersonaUseCase,
    private val getPersonasUseCase: GetPersonasUseCase,
    private val updatePersonaUseCase: UpdatePersonaUseCase,
    private val deletePersonaUseCase: DeletePersonaUseCase,
    private val selectPersonaUseCase: SelectPersonaUseCase
) : BaseRpcService(sessionContext), PersonaRpcService, KoinComponent {

    override suspend fun createPersona(request: PersonaCreateRequest): PersonaResponse {
        ensureAuthorized()
        return createPersonaUseCase.execute(request, userId())
    }

    override suspend fun getPersonas(): List<PersonaResponse> {
        ensureAuthorized()
        return getPersonasUseCase.execute(userId())
    }

    override suspend fun updatePersona(request: PersonaUpdateRequest): PersonaResponse {
        ensureAuthorized()
        return updatePersonaUseCase.execute(request, userId())
    }

    override suspend fun deletePersona(personaId: Uuid) {
        ensureAuthorized()
        deletePersonaUseCase.execute(personaId, userId())
    }

    override suspend fun selectPersona(request: SelectPersonaRequest) {
        ensureAuthorized()
        selectPersonaUseCase.execute(request, userId())
    }
}
