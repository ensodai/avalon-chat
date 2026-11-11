package com.ensodai.avalon.domain.useCases.customizationScreenUseCase

import com.ensodai.avalon.domain.repository.local.AppSettingsRepository
import com.ensodai.avalon.theme.models.AvalonThemePreset
import org.koin.core.annotation.Factory

@Factory
class SelectThemeUseCase(
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend operator fun invoke(preset: AvalonThemePreset) {
        appSettingsRepository.updateThemePreset(preset)
    }
}
