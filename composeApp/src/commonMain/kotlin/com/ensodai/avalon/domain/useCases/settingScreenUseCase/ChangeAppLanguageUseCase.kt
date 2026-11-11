package com.ensodai.avalon.domain.useCases.settingScreenUseCase

import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import org.koin.core.annotation.Single

@Single
class ChangeAppLanguageUseCase(
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun changeLanguage(language: String) {
        appSettingsRepository.changeAppLanguage(language)
    }
}
