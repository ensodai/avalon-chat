package com.ensodai.avalon.domain.useCases.settingScreenUseCase

import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetFlowModeUseCase(
    private val appSettingsRepository: AppSettingsRepository
) {
    fun get(): Flow<Boolean> {
        return appSettingsRepository.getFlowMode()
    }
}