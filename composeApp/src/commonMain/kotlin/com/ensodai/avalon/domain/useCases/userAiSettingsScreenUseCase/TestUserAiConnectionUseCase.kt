package com.ensodai.avalon.domain.useCases.userAiSettingsScreenUseCase

import com.ensodai.shared.avalon.user.models.TestUserAiConnectionRequest
import com.ensodai.shared.avalon.user.models.TestUserAiConnectionResponse
import com.ensodai.avalon.domain.repository.remote.UserAiSettingsRepository
import com.ensodai.avalon.presentation.userAiSettingsScreen.viewState.ModelType
import org.koin.core.annotation.Factory

@Factory
class TestUserAiConnectionUseCase(
    private val repository: UserAiSettingsRepository
) {
    suspend fun execute(
        request: TestUserAiConnectionRequest,
        modelType: ModelType = ModelType.TEXT
    ): TestUserAiConnectionResponse {
        return when (modelType) {
            ModelType.TTS -> repository.testTtsModel(request)
            ModelType.IMAGE -> repository.testImageModel(request)
            ModelType.TEXT, ModelType.IMAGE_PROMPT, ModelType.SUMMARY -> repository.testChatModel(request)
        }
    }
}
