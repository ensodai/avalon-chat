package com.ensodai.avalon.data.repository.remote

import com.ensodai.shared.avalon.rpc.services.UserRpcService
import com.ensodai.shared.avalon.user.models.TestUserAiConnectionRequest
import com.ensodai.shared.avalon.user.models.TestUserAiConnectionResponse
import com.ensodai.shared.avalon.user.models.UpdateUserAiSettingsRequest
import com.ensodai.shared.avalon.user.models.UserAiSettingsResponse
import com.ensodai.avalon.domain.repository.remote.UserAiSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserAiSettingsRepositoryImpl(
    private val userRpcService: UserRpcService
) : UserAiSettingsRepository {

    private val _settingsFlow = MutableStateFlow<UserAiSettingsResponse?>(null)
    override val settingsFlow: StateFlow<UserAiSettingsResponse?> = _settingsFlow.asStateFlow()

    override suspend fun getUserAiSettings(): UserAiSettingsResponse {
        val result = userRpcService.getUserAiSettings()
        _settingsFlow.value = result
        return result
    }

    override suspend fun updateUserAiSettings(request: UpdateUserAiSettingsRequest): UserAiSettingsResponse {
        val result = userRpcService.updateUserAiSettings(request)
        _settingsFlow.value = result
        return result
    }

    override suspend fun testUserAiConnection(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse {
        return userRpcService.testUserAiConnection(request)
    }

    override suspend fun testChatModel(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse {
        return userRpcService.testChatModel(request)
    }

    override suspend fun testTtsModel(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse {
        return userRpcService.testTtsModel(request)
    }

    override suspend fun testImageModel(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse {
        return userRpcService.testImageModel(request)
    }

    private val _catalogCache = mutableMapOf<Pair<com.ensodai.shared.avalon.user.models.AiProviderType, com.ensodai.shared.avalon.user.models.AiModelCategory>, List<com.ensodai.shared.avalon.user.models.AiModelCatalogItem>>()

    override suspend fun getAvailableAiModels(request: com.ensodai.shared.avalon.user.models.GetAiCatalogModelsRequest): com.ensodai.shared.avalon.user.models.GetAiCatalogModelsResponse {
        val cached = _catalogCache[request.provider to request.category]
        if (cached != null && cached.isNotEmpty()) {
            return com.ensodai.shared.avalon.user.models.GetAiCatalogModelsResponse(
                provider = request.provider,
                category = request.category,
                models = cached
            )
        }
        val response = userRpcService.getAvailableAiModels(request)
        _catalogCache[request.provider to request.category] = response.models
        return response
    }
}
