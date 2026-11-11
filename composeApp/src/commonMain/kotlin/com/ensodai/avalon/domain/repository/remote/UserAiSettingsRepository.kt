package com.ensodai.avalon.domain.repository.remote

import com.ensodai.shared.avalon.user.models.TestUserAiConnectionRequest
import com.ensodai.shared.avalon.user.models.TestUserAiConnectionResponse
import com.ensodai.shared.avalon.user.models.UpdateUserAiSettingsRequest
import com.ensodai.shared.avalon.user.models.UserAiSettingsResponse
import kotlinx.coroutines.flow.StateFlow

interface UserAiSettingsRepository {
    val settingsFlow: StateFlow<UserAiSettingsResponse?>
    suspend fun getUserAiSettings(): UserAiSettingsResponse
    suspend fun updateUserAiSettings(request: UpdateUserAiSettingsRequest): UserAiSettingsResponse
    suspend fun testUserAiConnection(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse
    suspend fun testChatModel(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse
    suspend fun testTtsModel(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse
    suspend fun testImageModel(request: TestUserAiConnectionRequest): TestUserAiConnectionResponse
    suspend fun getAvailableAiModels(request: com.ensodai.shared.avalon.user.models.GetAiCatalogModelsRequest): com.ensodai.shared.avalon.user.models.GetAiCatalogModelsResponse
}
