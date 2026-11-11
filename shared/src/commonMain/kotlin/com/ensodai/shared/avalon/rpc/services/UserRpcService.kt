@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)
package com.ensodai.shared.avalon.rpc.services

import com.ensodai.shared.avalon.user.models.*
import com.ensodai.shared.avalon.gifts.models.LocationResultResponse
import kotlinx.rpc.annotations.Rpc

@Rpc
interface UserRpcService {
    suspend fun getUserAccountInfo(): UserAccountInfoResponse
    suspend fun checkNicknameAvailability(request: CheckNicknameRequest): AvailableResultResponse
    suspend fun updateUserNickname(request: UpdateNicknameRequest)
    suspend fun updateUserOutreach(request: UpdateUserOutreachRequest)
    suspend fun updateUserFlowMode(request: UpdateUserFlowModeRequest)
    suspend fun getUserLocation(): LocationResultResponse
    suspend fun getUserAiSettings(): UserAiSettingsResponse
    suspend fun updateUserAiSettings(request: UpdateUserAiSettingsRequest): UserAiSettingsResponse
    suspend fun testUserAiConnection(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse
    suspend fun testChatModel(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse
    suspend fun testTtsModel(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse
    suspend fun testImageModel(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse
    suspend fun getAvailableAiModels(request: GetAiCatalogModelsRequest): GetAiCatalogModelsResponse
}
