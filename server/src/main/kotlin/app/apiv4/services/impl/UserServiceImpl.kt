package app.apiv4.services.impl

import app.apiv4.infrastructure.BaseRpcService
import app.apiv4.infrastructure.RpcSessionContext
import app.domain.system.usecases.GetUserLocationUseCase
import app.domain.user.usecases.CheckNicknameAvailabilityUseCase
import app.domain.user.usecases.GetUserAccountInfoUseCase
import app.domain.user.usecases.GetUserAiSettingsUseCase
import app.domain.user.usecases.TestUserAiConnectionUseCase
import app.domain.user.usecases.UpdateUserAiSettingsUseCase
import app.domain.user.usecases.UpdateUserFlowModeUseCase
import app.domain.user.usecases.UpdateUserNicknameUseCase
import app.domain.user.usecases.UpdateUserOutreachUseCase
import com.ensodai.shared.avalon.gifts.models.LocationResultResponse
import com.ensodai.shared.avalon.rpc.services.UserRpcService
import com.ensodai.shared.avalon.user.models.AvailableResultResponse
import com.ensodai.shared.avalon.user.models.CheckNicknameRequest
import com.ensodai.shared.avalon.user.models.TestUserAiConnectionRequest
import com.ensodai.shared.avalon.user.models.TestUserAiConnectionResponse
import com.ensodai.shared.avalon.user.models.UpdateNicknameRequest
import com.ensodai.shared.avalon.user.models.UpdateUserAiSettingsRequest
import com.ensodai.shared.avalon.user.models.UpdateUserFlowModeRequest
import com.ensodai.shared.avalon.user.models.UpdateUserOutreachRequest
import com.ensodai.shared.avalon.user.models.UserAccountInfoResponse
import com.ensodai.shared.avalon.user.models.UserAiSettingsResponse
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.origin
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.component.KoinComponent

@Factory
class UserServiceImpl(
    @InjectedParam private val call: ApplicationCall,
    @InjectedParam sessionContext: RpcSessionContext,
    private val getUserAccountInfoUseCase: GetUserAccountInfoUseCase,
    private val checkNicknameAvailabilityUseCase: CheckNicknameAvailabilityUseCase,
    private val updateUserNicknameUseCase: UpdateUserNicknameUseCase,
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val updateUserOutreachUseCase: UpdateUserOutreachUseCase,
    private val updateUserFlowModeUseCase: UpdateUserFlowModeUseCase,
    private val getUserAiSettingsUseCase: GetUserAiSettingsUseCase,
    private val updateUserAiSettingsUseCase: UpdateUserAiSettingsUseCase,
    private val testUserAiConnectionUseCase: TestUserAiConnectionUseCase,
    private val aiModelCatalogService: app.service.ai.AiModelCatalogService
) : BaseRpcService(sessionContext), UserRpcService, KoinComponent {

    override suspend fun getUserAccountInfo(): UserAccountInfoResponse {
        ensureAuthorized()
        return getUserAccountInfoUseCase.execute(userId())
    }

    override suspend fun checkNicknameAvailability(request: CheckNicknameRequest): AvailableResultResponse {
        ensureAuthorized()
        return checkNicknameAvailabilityUseCase.execute(request.nickname)
    }

    override suspend fun updateUserNickname(request: UpdateNicknameRequest) {
        ensureAuthorized()
        updateUserNicknameUseCase.execute(request, userId())
    }

    override suspend fun updateUserOutreach(request: UpdateUserOutreachRequest) {
        ensureAuthorized()
        updateUserOutreachUseCase.execute(request, userId())
    }

    override suspend fun updateUserFlowMode(request: UpdateUserFlowModeRequest) {
        ensureAuthorized()
        updateUserFlowModeUseCase.execute(request, userId())
    }

    override suspend fun getUserLocation(): LocationResultResponse {
        ensureAuthorized()
        val ip = call.request.origin.remoteHost
        return getUserLocationUseCase.execute(ip)
    }

    override suspend fun getUserAiSettings(): UserAiSettingsResponse {
        ensureAuthorized()
        return getUserAiSettingsUseCase.execute(userId())
    }

    override suspend fun updateUserAiSettings(request: UpdateUserAiSettingsRequest): UserAiSettingsResponse {
        ensureAuthorized()
        return updateUserAiSettingsUseCase.execute(request, userId())
    }

    override suspend fun testUserAiConnection(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse {
        ensureAuthorized()
        return testUserAiConnectionUseCase.execute(request)
    }

    override suspend fun testChatModel(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse {
        ensureAuthorized()
        return testUserAiConnectionUseCase.testChat(request)
    }

    override suspend fun testTtsModel(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse {
        ensureAuthorized()
        return testUserAiConnectionUseCase.testTts(request)
    }

    override suspend fun testImageModel(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse {
        ensureAuthorized()
        return testUserAiConnectionUseCase.testImage(request)
    }

    override suspend fun getAvailableAiModels(request: com.ensodai.shared.avalon.user.models.GetAiCatalogModelsRequest): com.ensodai.shared.avalon.user.models.GetAiCatalogModelsResponse {
        ensureAuthorized()
        val models = aiModelCatalogService.getModels(request.provider, request.category)
        return com.ensodai.shared.avalon.user.models.GetAiCatalogModelsResponse(
            provider = request.provider,
            category = request.category,
            models = models
        )
    }
}


