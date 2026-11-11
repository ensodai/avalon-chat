package com.ensodai.avalon.domain.useCases.userAiSettingsScreenUseCase

import com.ensodai.shared.avalon.user.models.UserAiSettingsResponse
import com.ensodai.avalon.domain.repository.remote.UserAiSettingsRepository
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Factory

@Factory
class GetUserAiSettingsUseCase(
    private val repository: UserAiSettingsRepository
) {
    suspend fun execute(): UserAiSettingsResponse {
        return repository.getUserAiSettings()
    }

    fun observe(): StateFlow<UserAiSettingsResponse?> {
        return repository.settingsFlow
    }
}
