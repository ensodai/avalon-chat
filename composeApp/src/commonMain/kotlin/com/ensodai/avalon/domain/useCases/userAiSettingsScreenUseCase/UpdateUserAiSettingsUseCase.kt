package com.ensodai.avalon.domain.useCases.userAiSettingsScreenUseCase

import com.ensodai.shared.avalon.user.models.UpdateUserAiSettingsRequest
import com.ensodai.shared.avalon.user.models.UserAiSettingsResponse
import com.ensodai.avalon.domain.repository.remote.UserAiSettingsRepository
import org.koin.core.annotation.Factory

@Factory
class UpdateUserAiSettingsUseCase(
    private val repository: UserAiSettingsRepository
) {
    suspend fun execute(request: UpdateUserAiSettingsRequest): UserAiSettingsResponse {
        return repository.updateUserAiSettings(request)
    }
}
