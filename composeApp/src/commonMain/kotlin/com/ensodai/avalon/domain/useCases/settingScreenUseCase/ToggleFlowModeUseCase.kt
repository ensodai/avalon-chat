package com.ensodai.avalon.domain.useCases.settingScreenUseCase

import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import org.koin.core.annotation.Single

@Single
class ToggleFlowModeUseCase(
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend fun toggle() {
        appSettingsRepository.toggleFlowMode()
    }
}