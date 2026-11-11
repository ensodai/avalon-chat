package com.ensodai.avalon.domain.useCases.settingScreenUseCase

import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetLanguageSettingsUseCase(private val appSettingsRepository: AppSettingsRepository) {

    suspend fun get(): Flow<String?> {
        return appSettingsRepository.getLanguageSettings()
    }
}
