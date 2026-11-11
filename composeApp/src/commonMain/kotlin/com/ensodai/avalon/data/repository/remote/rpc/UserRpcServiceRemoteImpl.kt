@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package com.ensodai.avalon.data.repository.remote.rpc

import com.ensodai.shared.avalon.gifts.models.LocationResultResponse
import com.ensodai.shared.avalon.rpc.services.UserRpcService
import com.ensodai.shared.avalon.user.models.*
import com.ensodai.avalon.di.RpcConnectionManager
import kotlinx.rpc.withService

open class UserRpcServiceRemoteImpl(
    private val manager: RpcConnectionManager,
    private val executor: RpcCallExecutor
) : UserRpcService {

    open suspend fun getService(): UserRpcService = manager.getActiveClient().withService<UserRpcService>()

    override suspend fun getUserAccountInfo(): UserAccountInfoResponse =
        executor.execute("getUserAccountInfo", getService = { getService() }) { getUserAccountInfo() }

    override suspend fun checkNicknameAvailability(request: CheckNicknameRequest): AvailableResultResponse =
        executor.execute("checkNicknameAvailability", getService = { getService() }) { checkNicknameAvailability(request) }

    override suspend fun updateUserNickname(request: UpdateNicknameRequest) =
        executor.execute("updateUserNickname", getService = { getService() }) { updateUserNickname(request) }

    override suspend fun updateUserOutreach(request: UpdateUserOutreachRequest) =
        executor.execute("updateUserOutreach", getService = { getService() }) { updateUserOutreach(request) }

    override suspend fun getUserLocation(): LocationResultResponse =
        executor.execute("getUserLocation", getService = { getService() }) { getUserLocation() }

    override suspend fun getUserAiSettings(): UserAiSettingsResponse =
        executor.execute("getUserAiSettings", getService = { getService() }) { getUserAiSettings() }

    override suspend fun updateUserAiSettings(request: UpdateUserAiSettingsRequest): UserAiSettingsResponse =
        executor.execute("updateUserAiSettings", getService = { getService() }) { updateUserAiSettings(request) }

    override suspend fun testUserAiConnection(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse =
        executor.execute("testUserAiConnection", getService = { getService() }) { testUserAiConnection(request) }

    override suspend fun testChatModel(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse =
        executor.execute("testChatModel", getService = { getService() }) { testChatModel(request) }

    override suspend fun testTtsModel(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse =
        executor.execute("testTtsModel", getService = { getService() }) { testTtsModel(request) }

    override suspend fun testImageModel(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse =
        executor.execute("testImageModel", getService = { getService() }) { testImageModel(request) }

    override suspend fun getAvailableAiModels(request: GetAiCatalogModelsRequest): GetAiCatalogModelsResponse =
        executor.execute("getAvailableAiModels", getService = { getService() }) { getAvailableAiModels(request) }

    override suspend fun updateUserFlowMode(request: UpdateUserFlowModeRequest) =
        executor.execute("updateUserFlowMode", getService = { getService() }) { updateUserFlowMode(request) }
}
