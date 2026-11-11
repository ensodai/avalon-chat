package com.ensodai.avalon.domain.useCases.settingScreenUseCase

import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import org.koin.core.annotation.Single

@Single
class GetSettingLocalModeFlowUseCase(private val appSettingsRepository: AppSettingsRepository) {

    fun get() = appSettingsRepository.getLocalModeFlow()

}